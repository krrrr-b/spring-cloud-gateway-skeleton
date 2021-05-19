package com.skeleton.webflux.client.logger

import com.fasterxml.jackson.databind.ObjectMapper
import feign.MethodMetadata
import feign.Target
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactivefeign.client.ReactiveHttpRequest
import reactivefeign.client.ReactiveHttpResponse
import reactivefeign.client.log.ReactiveLoggerListener
import reactor.core.publisher.Flux

class ReactiveFeignLogger : ReactiveLoggerListener<LogContext> {
    private val isExtendedLoggingEnabled: Boolean = true
    private val logger: Logger = LoggerFactory.getLogger(ReactiveFeignLogger::class.java)

    override fun requestStarted(
        request: ReactiveHttpRequest,
        target: Target<*>,
        methodMetadata: MethodMetadata
    ): LogContext {
        return LogContext(request, target, methodMetadata)
    }

    override fun errorReceived(ex: Throwable, context: LogContext) {
        logger.error("[${context.feignMethodTag}] ${context.request.method()} ${context.request.uri()}", ex)
    }

    override fun logRequestBody() = isExtendedLoggingEnabled

    override fun bodyReceived(body: Any, context: LogContext) {
        val bodyPrefix = context.response?.body()?.formatBodyPrefix(bodyType = "response")
        val message = body.let {
            "$bodyPrefix ${ObjectMapper().writeValueAsString(it)}"
        }

        logger.debug("[{}] {}", context.feignMethodTag, message)
    }

    override fun responseReceived(response: ReactiveHttpResponse<*>?, context: LogContext) {
        context.response = response
    }

    override fun logResponseBody() = isExtendedLoggingEnabled

    override fun bodySent(body: Any, context: LogContext) {
        val bodyPrefix = context.request.body()?.formatBodyPrefix(bodyType = "request")
        val message = body.let {
            "$bodyPrefix ${ObjectMapper().writeValueAsString(it)}"
        }

        logger.debug("[{}] {}", context.feignMethodTag, message)
    }

    private fun Publisher<*>.formatBodyPrefix(bodyType: String): String =
        if (this is Flux<*>) "$bodyType body element: " else "$bodyType body:"
}
