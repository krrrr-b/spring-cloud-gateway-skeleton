package com.skeleton.webflux.internal.api.config.resilience

import com.skeleton.webflux.common.constant.config.ResilienceCircuit
import com.skeleton.webflux.common.exception.ApiException
import com.skeleton.webflux.common.exception.IllegalUserArgumentException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.core.registry.RegistryEventConsumer
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import io.micrometer.core.instrument.MeterRegistry
import io.netty.handler.timeout.TimeoutException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.webjars.NotFoundException
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import java.time.Duration

@Configuration
class ResilienceConfiguration {
    companion object {
        /**
         * @see ResilienceCircuit
         */
        const val COMMON_CIRCUIT_BREAKER = "commonCircuitBreaker"
        const val DEFAULT_RETRY = "defaultRetry"
    }

    private val retries: List<String> = arrayOf(DEFAULT_RETRY).toList()

    @Bean
    fun circuitBreakerRegistry(
        @Qualifier(CustomResilienceRegistryEventConsumer.CUSTOM_RESILIENCE_CIRCUIT_BREAKER_EVENT_CONSUMER)
        resilienceRegistryEventConsumer: RegistryEventConsumer<CircuitBreaker>,
    ): CircuitBreakerRegistry {
        val defaultConfig: CircuitBreakerConfig = CircuitBreakerConfig.custom()

             // @description count-based sliding window
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(ResilienceCircuit.COMMON.slidingWindowSize)
            .slowCallDurationThreshold(ResilienceCircuit.COMMON.slowCallOfDuration)
            .slowCallRateThreshold(ResilienceCircuit.COMMON.slowCallRate)
            .permittedNumberOfCallsInHalfOpenState(ResilienceCircuit.COMMON.halfOpenState)
            .minimumNumberOfCalls(ResilienceCircuit.COMMON.minimumNumberOfCalls)

            // @description 내부적으로 스레드가 모니터링하여 half-open 상태로 자동으로 전환
            .enableAutomaticTransitionFromOpenToHalfOpen()
            .failureRateThreshold(ResilienceCircuit.COMMON.failureRate)
            .recordExceptions(
                RuntimeException::class.java,
                ApiException::class.java,
                TimeoutException::class.java,
                NullPointerException::class.java
            )
            .ignoreExceptions(
                NotFoundException::class.java,
                IllegalArgumentException::class.java,
                IllegalUserArgumentException::class.java
            )
            .waitDurationInOpenState(Duration.ofMillis(ResilienceCircuit.COMMON.waitDurationInOpenStateMillis))
            .build()

        return CircuitBreakerRegistry.of(defaultConfig, resilienceRegistryEventConsumer)
    }

    @Bean
    fun retryRegistry(meterRegistry: MeterRegistry): RetryRegistry {
        val registry: RetryRegistry = RetryRegistry.of(RetryConfig.ofDefaults())
        for (r in retries) {
            registry.retry(r)
        }

        return registry
    }

    @Bean(COMMON_CIRCUIT_BREAKER)
    fun commonBreaker(
        registry: CircuitBreakerRegistry,
    ): CircuitBreaker {
        return registry.circuitBreaker(
            ResilienceCircuit.COMMON.circuitName,
            CircuitBreakerConfig.from(registry.defaultConfig)
                .build()
        )
    }
}
