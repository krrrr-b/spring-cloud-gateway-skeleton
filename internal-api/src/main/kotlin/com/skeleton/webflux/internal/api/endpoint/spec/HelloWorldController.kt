package com.skeleton.webflux.internal.api.endpoint.spec

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import reactor.core.publisher.Mono

@Tag(name = "헬로 월드")
interface HelloWorldControllerSpec {
    @Operation(summary = "summary")
    fun index(): Mono<String>
}
