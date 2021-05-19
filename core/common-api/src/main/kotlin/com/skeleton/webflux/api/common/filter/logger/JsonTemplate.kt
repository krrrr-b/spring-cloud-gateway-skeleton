package com.skeleton.webflux.api.common.filter.logger

class JsonTemplate {
    var requestTime: String? = null
    var hostname: String? = null
    var port: Int? = null
    var method: String? = null
    var url: String? = null
    var phase: String? = null
    var elapsedTime: Long? = null
    var request: String? = null
    var response: String? = null
    var responseStatus = 0
    var requestParams: String? = null
    var requestHeader: String? = null
}
