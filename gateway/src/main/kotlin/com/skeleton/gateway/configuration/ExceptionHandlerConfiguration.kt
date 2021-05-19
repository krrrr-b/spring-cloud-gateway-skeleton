package com.skeleton.gateway.configuration

import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import java.util.*

@Order(-2)
@Component
class ExceptionHandlerConfiguration(
    errorAttributes: ErrorAttributes?,
    resourceProperties: ResourceProperties?,
    applicationContext: ApplicationContext?,
    configurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(errorAttributes, resourceProperties, applicationContext) {
    protected override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route<ServerResponse>(
            RequestPredicates.all(),
            HandlerFunction<ServerResponse> { request: ServerRequest -> renderErrorResponse(request) })
    }

    private fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse?> {
        val errorPropertiesMap: Map<String, Any> = getErrorAttributes(request, false)
        val errorMessage = Optional.ofNullable(errorPropertiesMap["message"] as String?)
            .orElse("일시적인 문제로 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.")

        // *important 강제 업데이트가 필요한 경우 에러코드를 별도로 내려주기 위한 처리
        return if (isApplicationForceUpdate(errorMessage)) {
            ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(BaseErrorResponse.fail(ErrorCode.FORCE_UPDATE.name(), errorMessage))
        } else ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(BaseErrorResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.name(), errorMessage))
    }

    private fun isApplicationForceUpdate(errorMessage: String): Boolean {
        return errorMessage == UpdateType.FORCE.getDescription()
    }

    init {
        this.setMessageWriters(configurer.getWriters())
    }
}