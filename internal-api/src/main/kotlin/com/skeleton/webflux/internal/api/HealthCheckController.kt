package com.skeleton.webflux.internal.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

const val REQUEST_PATH_HEALTH_CHECK = "/health"

@RestController
class HealthCheckController {
    @GetMapping(REQUEST_PATH_HEALTH_CHECK)
    fun ping(): Mono<String> {
        return Mono.empty()
    }
}
