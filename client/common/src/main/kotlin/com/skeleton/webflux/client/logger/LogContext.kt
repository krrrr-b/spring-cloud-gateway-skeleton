package com.skeleton.webflux.client.logger

import feign.MethodMetadata
import feign.Target
import reactivefeign.client.ReactiveHttpRequest
import reactivefeign.client.ReactiveHttpResponse
import reactivefeign.utils.FeignUtils

data class LogContext(
    val request: ReactiveHttpRequest,
    val target: Target<*>,
    val methodMetadata: MethodMetadata,
    val feignMethodTag: String = FeignUtils.methodTag(methodMetadata),
    var response: ReactiveHttpResponse<*>? = null
)
