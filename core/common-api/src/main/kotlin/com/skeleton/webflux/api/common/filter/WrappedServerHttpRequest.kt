package com.skeleton.webflux.api.common.filter

import com.skeleton.webflux.api.common.filter.logger.JsonTemplate
import io.netty.buffer.UnpooledByteBufAllocator
import org.apache.commons.io.IOUtils
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.NettyDataBufferFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import reactor.core.publisher.Flux
import java.io.IOException
import java.nio.charset.StandardCharsets

class WrappedServerHttpRequest(
    delegate: ServerHttpRequest,
    private var template: JsonTemplate
) : ServerHttpRequestDecorator(delegate) {
    override fun getBody(): Flux<DataBuffer> {
        return super.getBody()
            .map { bodyDataBuffer: DataBuffer -> wrapBody(bodyDataBuffer) }
    }

    private fun wrapBody(bodyDataBuffer: DataBuffer): DataBuffer {
        return try {
            val bodyDataBufferInputStream = bodyDataBuffer.asInputStream()
            val bytes: ByteArray = IOUtils.toByteArray(bodyDataBufferInputStream)
            template.request = String(bytes, StandardCharsets.UTF_8)
            val nettyDataBufferFactory =
                NettyDataBufferFactory(UnpooledByteBufAllocator(false))
            DataBufferUtils.release(bodyDataBuffer)
            nettyDataBufferFactory.wrap(bytes)
        } catch (ex: IOException) {
            throw RuntimeException("$this body wrapping has failed,", ex)
        }
    }
}
