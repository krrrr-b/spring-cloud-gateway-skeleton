package com.skeleton.webflux.internal.api.endpoint

import com.skeleton.webflux.internal.api.endpoint.spec.HelloWorldControllerSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

const val REQUEST_PATH_AFTER_VERIFY = "/api-internal/v1"

@RestController
@RequestMapping(value = [REQUEST_PATH_AFTER_VERIFY])
class HelloWorldController @Autowired constructor() : HelloWorldControllerSpec {
    companion object {
        const val REQUEST_PATH_HELLO_WORLD = "/hello-world"
    }

    @PostMapping(REQUEST_PATH_HELLO_WORLD)
    override fun index(): Mono<String> {
        return Mono.just("hello world!")
    }
}
