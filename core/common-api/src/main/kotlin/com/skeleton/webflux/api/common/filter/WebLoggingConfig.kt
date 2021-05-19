package com.skeleton.webflux.api.common.filter

import com.skeleton.webflux.api.common.filter.logger.JsonLogger
import com.skeleton.webflux.api.common.filter.logger.JsonTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebExchangeDecorator
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.net.InetAddress
import java.time.LocalDateTime
import java.util.stream.Stream
import javax.annotation.PostConstruct

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class WebLoggingConfig(
    @Value("\${server.port:8080}") private val port: Int,
    @Value("\${spring.profiles.active}") private val phase: String,
) : WebFilter {
    private var hostName: String? = null

    @PostConstruct
    fun init() {
        hostName = try {
            InetAddress.getLocalHost().hostName
        } catch (ex: Exception) {
            "unknown"
        }
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()
        val request = exchange.request
        val path = request.uri.path

        val template = JsonTemplate()
        template.requestTime = LocalDateTime.now().toString()
        template.hostname = hostName
        template.port = port
        template.method = request.methodValue
        template.url = path
        template.phase = phase
        template.requestParams = request.queryParams.toString()
        template.requestHeader = request.headers.toString()

        return if (path == null || isExceptRequestPath(path)) {
            chain.filter(exchange)
        } else chain.filter(
            object : ServerWebExchangeDecorator(exchange) {
                override fun getRequest(): ServerHttpRequest {
                    return WrappedServerHttpRequest(exchange.request, template)
                }

                override fun getResponse(): ServerHttpResponse {
                    return WrappedServerHttpResponse(exchange.response, template)
                }
            },
        ).doFinally {
            val endTime = System.currentTimeMillis()
            template.elapsedTime = endTime - startTime
            JsonLogger().log(template)
        }
    }

    private fun isExceptRequestPath(path: String): Boolean {
        return isHealthCheck(path) || isSwaggerOrFavicon(path)
    }

    private fun isHealthCheck(path: String): Boolean {
        return Stream.of("/api/ping", "/actuator/health")
            .anyMatch { prefix: String -> path.startsWith(prefix) }
    }

    private fun isSwaggerOrFavicon(path: String): Boolean {
        return Stream.of(
            "/health",
            "/swagger",
            "/swagger-ui.html",
            "/null/swagger-resources",
            "/null/swagger-resources/configuration/security",
            "/v2/api-docs",
            "/v3/api-docs/swagger-config",
            "/webjars/",
            "/favicon.ico"
        ).anyMatch { prefix: String -> path.startsWith(prefix) }
    }
}
