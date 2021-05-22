package com.skeleton.gateway.api.common

import com.skeleton.gateway.common.constant.common.ErrorType

class BaseErrorResponse(
    val code: ErrorType,
    val message: String
) {
    constructor(errorType: ErrorType) : this(errorType, errorType.message)
}
