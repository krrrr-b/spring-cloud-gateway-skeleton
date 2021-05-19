package com.skeleton.webflux.api.common

import com.skeleton.webflux.common.constant.common.ErrorType

data class BaseErrorResponse(
    val code: ErrorType,
    val message: String
)
