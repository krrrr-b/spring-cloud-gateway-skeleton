package com.skeleton.gateway.configuration

import lombok.extern.slf4j.Slf4j
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Component
class RedisRateLimiterConfiguration(
    private var redisTemplate: ReactiveRedisTemplate<String?, String?>,
    private var script: RedisScript<List<Long?>?>?,
    @Qualifier("defaultValidator") validator: Validator?
) : AbstractRateLimiter<RedisRateLimiter.Config?>(
    RedisRateLimiter.Config::class.java, CONFIGURATION_PROPERTY_NAME, validator
), ApplicationContextAware {
    private val initialized = AtomicBoolean(false)
    private var defaultConfig: RedisRateLimiter.Config? = null
    fun setLimiterConfig(defaultReplenishRate: Int, defaultBurstCapacity: Int) {
        defaultConfig = RedisRateLimiter.Config().setReplenishRate(defaultReplenishRate)
            .setBurstCapacity(defaultBurstCapacity)
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(context: ApplicationContext) {
        if (initialized.compareAndSet(false, true)) {
            redisTemplate = context.getBean(
                "stringReactiveRedisTemplate",
                ReactiveRedisTemplate::class.java
            )
            script = context.getBean(REDIS_SCRIPT_NAME, RedisScript::class.java)
            if (context.getBeanNamesForType(Validator::class.java).size > 0) {
                this.validator =
                    context.getBean(Validator::class.java)
            }
        }
    }

    override fun isAllowed(routeId: String, id: String): Mono<RateLimiter.Response> {
        check(initialized.get()) { "RedisRateLimiter is not initialized" }
        val routeConfig = loadConfiguration(routeId)
        val replenishRate = routeConfig.replenishRate
        val burstCapacity = routeConfig.burstCapacity
        try {
            val keys = getKeys(id)
            val scriptArgs = Arrays.asList(
                replenishRate.toString() + "",
                burstCapacity.toString() + "",
                Instant.now().epochSecond.toString() + "",
                "1"
            )
            val flux: Flux<List<Long>?> = redisTemplate.execute(script, keys, scriptArgs)
            return flux.onErrorResume { throwable: Throwable? -> Flux.just(Arrays.asList(1L, -1L)) }
                .reduce(ArrayList()) { longs: ArrayList<Long>, l: List<Long>? ->
                    longs.addAll(l!!)
                    longs
                }
                .flatMap { results: ArrayList<Long> ->
                    val allowed = results[0] == 1L
                    val tokensLeft = results[1]
                    if (tokensLeft < 1L) {
                        return@flatMap Mono.error<RateLimiter.Response>(RuntimeException("일시적인 문제로 요청을 처리할 수 없습니다. 잠시후 다시 시도해주세요."))
                    }
                    Mono.just(RateLimiter.Response(allowed, getHeaders(routeConfig, tokensLeft)))
                }
        } catch (e: Exception) {
            RedisRateLimiterConfiguration.log.error("Error determining if user allowed from redis", e)
        }
        return Mono.just(RateLimiter.Response(true, getHeaders(routeConfig, -1L)))
    }

    private fun loadConfiguration(routeId: String): RedisRateLimiter.Config {
        var routeConfig = config.getOrDefault(routeId, defaultConfig)
        if (routeConfig == null) {
            routeConfig = config[RouteDefinitionRouteLocator.DEFAULT_FILTERS]
        }
        requireNotNull(routeConfig) { "No Configuration found for route $routeId or defaultFilters" }
        return routeConfig
    }

    private fun getHeaders(config: RedisRateLimiter.Config, tokensLeft: Long): Map<String, String> {
        val headers: MutableMap<String, String> = HashMap(3)
        headers[REMAINING_HEADER] = tokensLeft.toString()
        headers[REPLENISH_RATE_HEADER] = config.replenishRate.toString()
        headers[BURST_CAPACITY_HEADER] = config.burstCapacity.toString()
        return headers
    }

    companion object {
        private const val CONFIGURATION_PROPERTY_NAME = "redis-rate-limiter"
        private const val REDIS_SCRIPT_NAME = "redisRequestRateLimiterScript"
        private const val REMAINING_HEADER = "X-RateLimit-Remaining"
        private const val REPLENISH_RATE_HEADER = "X-RateLimit-Replenish-Rate"
        private const val BURST_CAPACITY_HEADER = "X-RateLimit-Burst-Capacity"
        private fun getKeys(id: String): List<String?> {
            val prefix = "request_rate_limiter.{$id"
            val tokenKey = "$prefix}.tokens"
            val timestampKey = "$prefix}.timestamp"
            return listOf(tokenKey, timestampKey)
        }
    }

    init {
        initialized.compareAndSet(false, true)
    }
}