package com.skeleton.gateway.api.common

import com.skeleton.gateway.common.constant.common.ErrorType

data class BaseErrorResponse(
    val code: ErrorType,
    val message: String
)
