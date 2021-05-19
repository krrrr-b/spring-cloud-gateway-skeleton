package com.skeleton.webflux.api.common.advice

import com.skeleton.webflux.api.common.BaseErrorResponse
import com.skeleton.webflux.common.constant.common.ErrorType
import com.skeleton.webflux.common.exception.IllegalUserArgumentException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionAdvice {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(value = [IllegalUserArgumentException::class])
    fun handleIllegalUserArgumentException(ex: IllegalUserArgumentException): ResponseEntity<BaseErrorResponse> {
        logger.error("{}", ex.toString(), ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(BaseErrorResponse(ErrorType.BAD_REQUEST, ex.message ?: ErrorType.BAD_REQUEST.message))
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleDefaultException(ex: Exception): ResponseEntity<BaseErrorResponse> {
        logger.error("{}", ex.toString(), ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseErrorResponse(ErrorType.API_INTERNAL_ERROR, ex.message ?: ErrorType.API_INTERNAL_ERROR.message))
    }
}
