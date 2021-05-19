package com.skeleton.webflux.client

import com.skeleton.webflux.client.logger.ReactiveFeignLogger
import org.springframework.cloud.openfeign.support.SpringMvcContract
import reactivefeign.ReactiveContract
import reactivefeign.ReactiveFeignBuilder
import reactivefeign.ReactiveOptions
import reactivefeign.retry.BasicReactiveRetryPolicy
import reactivefeign.retry.ReactiveRetryPolicy
import reactivefeign.webclient.WebReactiveFeign
import reactivefeign.webclient.WebReactiveOptions

class FeignClientConfiguration<T> {
    companion object {
        const val COMMON_RETRY_COUNT = 2
        const val COMMON_READ_TIMEOUT_MILLIS = 5000L
        const val COMMON_CONNECT_TIMEOUT_MILLIS = 10000L
    }

    private fun commonReactiveOptions(): ReactiveOptions {
        return WebReactiveOptions.Builder()
            .setReadTimeoutMillis(COMMON_READ_TIMEOUT_MILLIS)
            .setConnectTimeoutMillis(COMMON_CONNECT_TIMEOUT_MILLIS)
            .build()
    }

    private fun commonReactiveRetryPolicy(): ReactiveRetryPolicy {
        return BasicReactiveRetryPolicy.retry(COMMON_RETRY_COUNT)
    }

    fun reactiveFeignBuilder(): ReactiveFeignBuilder<T> {
        return WebReactiveFeign.builder<T>()
            .addLoggerListener(ReactiveFeignLogger())
            .contract(ReactiveContract(SpringMvcContract()))
            .options(commonReactiveOptions())
            .retryWhen(commonReactiveRetryPolicy())
    }
}
