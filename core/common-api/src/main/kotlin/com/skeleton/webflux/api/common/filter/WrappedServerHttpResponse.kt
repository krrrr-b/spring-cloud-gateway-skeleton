package com.skeleton.webflux.api.common.filter

import com.skeleton.webflux.api.common.filter.logger.JsonTemplate
import io.netty.buffer.UnpooledByteBufAllocator
import org.apache.commons.io.IOUtils
import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.NettyDataBufferFactory
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import reactor.core.publisher.Mono
import java.io.IOException
import java.nio.charset.StandardCharsets

class WrappedServerHttpResponse(
    delegate: ServerHttpResponse,
    private val template: JsonTemplate
) : ServerHttpResponseDecorator(delegate) {
    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        return if (body is Mono<*>) {
            super.writeWith(
                Mono.from(body)
                    .map { bodyDataBuffer: Any ->
                        wrapBody(bodyDataBuffer as DataBuffer)
                    }
            )
        } else {
            super.writeWith(body)
        }
    }

    private fun wrapBody(bodyDataBuffer: DataBuffer): DataBuffer {
        return try {
            val bodyDataBufferInputStream = bodyDataBuffer.asInputStream()
            val bytes: ByteArray = IOUtils.toByteArray(bodyDataBufferInputStream)
            template.response = String(bytes, StandardCharsets.UTF_8)
            template.responseStatus = statusCode.value()
            val nettyDataBufferFactory =
                NettyDataBufferFactory(UnpooledByteBufAllocator(false))
            DataBufferUtils.release(bodyDataBuffer)
            nettyDataBufferFactory.wrap(bytes)
        } catch (ex: IOException) {
            throw RuntimeException("$this body wrapping has failed,", ex)
        }
    }
}
