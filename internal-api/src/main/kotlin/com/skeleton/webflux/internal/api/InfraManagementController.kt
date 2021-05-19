package com.skeleton.webflux.internal.api

import com.skeleton.webflux.common.constant.config.ResilienceCircuit
import com.skeleton.webflux.internal.api.config.resilience.ResilienceConfiguration
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException

const val REQUEST_PATH_CIRCUIT_MANAGEMENT = "/circuit"

@RestController
class CircuitManagementController(
    @Qualifier(ResilienceConfiguration.COMMON_CIRCUIT_BREAKER)
    private val commonBreaker: CircuitBreaker,
) {
    @GetMapping(REQUEST_PATH_CIRCUIT_MANAGEMENT)
    fun getCircuitState(
        @RequestParam circuitType: ResilienceCircuit,
    ): CircuitBreaker {
        return when (circuitType) {
            ResilienceCircuit.COMMON -> commonBreaker
            else -> throw IllegalArgumentException()
        }
    }

    @PutMapping(REQUEST_PATH_CIRCUIT_MANAGEMENT)
    fun updateCircuitState(
        @RequestParam circuitType: ResilienceCircuit,
        @RequestParam state: CircuitBreaker.State,
    ): CircuitBreaker {
        val circuit = when (circuitType) {
            ResilienceCircuit.COMMON -> commonBreaker
            else -> throw IllegalArgumentException()
        }

        when (state) {
            CircuitBreaker.State.DISABLED -> circuit.transitionToDisabledState()
            CircuitBreaker.State.METRICS_ONLY -> circuit.transitionToMetricsOnlyState()
            CircuitBreaker.State.CLOSED -> circuit.transitionToClosedState()
            CircuitBreaker.State.OPEN -> circuit.transitionToOpenState()
            CircuitBreaker.State.FORCED_OPEN -> circuit.transitionToForcedOpenState()
            CircuitBreaker.State.HALF_OPEN -> circuit.transitionToHalfOpenState()
        }

        return circuit
    }
}
