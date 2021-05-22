package com.skeleton.gateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod

@SpringBootApplication(scanBasePackages = ["com.skeleton.gateway"])
class GatewayApplication(
    private val redisRateLimiterConfiguration: RedisRateLimiterConfiguration,
    private val ipKeyResolver: KeyResolver,

    @Value("\${internal.host}")
    private val host: String
) {
    @Bean
    fun routes(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route { r: PredicateSpec ->
                r.method(HttpMethod.GET)
                    .and()
                    .path("/**")
                    .uri(host)
            }
            .route { r: PredicateSpec ->
                r.method(HttpMethod.POST)
                    .or()
                    .method(HttpMethod.PUT)
                    .or()
                    .method(HttpMethod.DELETE)
                    .and()
                    .path("/**")
                    .filters { f: GatewayFilterSpec ->
                        f.requestRateLimiter { c: RequestRateLimiterGatewayFilterFactory.Config ->
                            c.keyResolver = ipKeyResolver
                            c.rateLimiter = redisRateLimiterConfiguration
                        }
                    }
                    .uri(host)
            }
            .build()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(GatewayApplication::class.java, *args)
}
