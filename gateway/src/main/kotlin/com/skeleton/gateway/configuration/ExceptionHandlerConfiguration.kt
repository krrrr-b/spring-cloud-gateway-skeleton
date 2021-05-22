package com.skeleton.gateway.configuration

import com.skeleton.gateway.api.common.BaseErrorResponse
import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import com.skeleton.gateway.common.constant.common.ErrorType
import java.util.*

@Order(-2)
@Component
class ExceptionHandlerConfiguration(
    private val errorAttributes: ErrorAttributes,
    private val resourceProperties: ResourceProperties,
    private val applicationContext: ApplicationContext,
    private val configurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(errorAttributes, resourceProperties, applicationContext) {
    init {
        this.setMessageWriters(configurer.writers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes) =
        RouterFunctions.route(RequestPredicates.all()) { request: ServerRequest ->
            renderErrorResponse(request)
        }

    private fun renderErrorResponse(request: ServerRequest) =
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(BaseErrorResponse(ErrorType.API_INTERNAL_ERROR))
}