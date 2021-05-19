package com.skeleton.webflux.data.redis.component

import com.skeleton.webflux.data.redis.config.RedisConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
@Suppress("NAME_SHADOWING")
class ReactiveRedisExpireCache(
    @Qualifier(RedisConfiguration.REACTIVE_REDIS_TEMPLATE)
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val env: EnvironmentHelper,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val EXPIRED_DAYS: Long = 14
    }

    fun hasKey(key: String): Mono<Boolean> {
        return redisTemplate.hasKey(env.cachePrefix.plus(key))
            .doOnNext { logger.debug("[ReactiveRedisExpireCache#hasKey] $key value is ${if (it) "exists" else "not-exists"}") }
    }

    fun set(
        key: String,
        value: String,
    ): Mono<Boolean> {
        val key = env.cachePrefix.plus(key)
        return redisTemplate.opsForValue().set(key, value)
            .doOnNext { updateExpireTimeFor(key).subscribe() }
            .doOnNext { logger.debug("[ReactiveRedisExpireCache#set] $key set value $value") }
    }

    fun set(key: String, value: String, timeout: Duration): Mono<Boolean> {
        val key = env.cachePrefix.plus(key)
        return redisTemplate.opsForValue().set(key, value, timeout)
            .doOnNext { logger.debug("[ReactiveRedisExpireCache#set] $key set value $value, duration: $timeout") }
    }

    fun delete(key: String): Mono<Boolean> {
        val key = env.cachePrefix.plus(key)
        return redisTemplate.opsForValue().delete(key)
            .doOnNext { logger.debug("[ReactiveRedisExpireCache#delete] $key delete") }
    }

    fun getObject(key: String): Mono<String> {
        val key = env.cachePrefix.plus(key)
        return redisTemplate.hasKey(key)
            .filter { obj: Boolean -> obj }
            .flatMap {
                redisTemplate.opsForValue()[key]
                    .map { result: String -> result }
            }
            .doOnNext { updateExpireTimeFor(key).subscribe() }
            .switchIfEmpty(Mono.error(NotFoundException(ErrorType.NOT_FOUND.message)))
    }

    fun updateExpireTimeFor(key: String): Mono<Void> {
        redisTemplate.expire(key, Duration.ofDays(EXPIRED_DAYS))
            .subscribe {
                logger.debug("[ReactiveRedisExpireCache#updateExpireTimeFor] $key update expire for ${
                    Duration.ofDays(EXPIRED_DAYS)
                }")
            }

        return Mono.empty()
    }
}
