package com.skeleton.gateway.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.validation.Validator
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

@Configuration
class LimiterConfig {
    @Value("\${limiter.replenish_rate}")
    private val replenishRate: Int? = null

    @Value("\${limiter.burst_capacity}")
    private val burstCapacity: Int? = null
    @Bean
    @Primary
    fun ipKeyResolver(): KeyResolver {
        return KeyResolver { exchange: ServerWebExchange ->
            Mono.just(
                Objects.requireNonNull(exchange.request.remoteAddress)
                    .hostString
            )
        }
    }

    @Bean
    @Primary
    fun myRedisRateLimiter(
        redisTemplate: ReactiveRedisTemplate<String?, String?>,
        @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) redisScript: RedisScript<List<Long?>?>?,
        defaultValidator: Validator?
    ): RedisRateLimiterConfiguration {
        val partnerRedisRateLimiter = RedisRateLimiterConfiguration(redisTemplate, redisScript, defaultValidator)
        partnerRedisRateLimiter.setLimiterConfig(replenishRate!!, burstCapacity!!)
        return partnerRedisRateLimiter
    }
}