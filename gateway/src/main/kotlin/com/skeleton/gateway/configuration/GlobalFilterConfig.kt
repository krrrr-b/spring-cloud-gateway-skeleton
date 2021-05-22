package com.skeleton.gateway.configuration

import org.apache.logging.log4j.util.Strings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import java.io.IOException
import java.lang.IllegalArgumentException
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeoutException

@Configuration
class GlobalFilterConfig {
    companion object {
        const val TIMEOUT_DURATION_MS = 10000L
    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun accountMappingFilter() =
        GlobalFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val httpHeaders: HttpHeaders = exchange.request.headers
            if (isHealthCheck(exchange.request)) {
                return@GlobalFilter exchange.response
                    .writeAndFlushWith(
                        Flux.just(Strings.EMPTY)
                            .map { body: String ->
                                Mono.just<DataBuffer>(
                                    exchange.response
                                        .bufferFactory()
                                        .allocateBuffer()
                                        .write(body.toByteArray())
                                )
                            })
            }

            Flux.just(Objects.requireNonNull(httpHeaders.getFirst(HttpHeaders.AUTHORIZATION)))
                .switchIfEmpty(Mono.error(IllegalArgumentException("사용자 정보를 찾을 수 없습니다.")))
                .handle { account: String?, synchronousSink: SynchronousSink<Any> ->
                    try {
                        if (StringUtils.isEmpty(account)) {
                            synchronousSink.complete()
                        } else {
                            synchronousSink.next(getPayAccountId(account))
                        }
                    } catch (ex: IOException) {
                        synchronousSink.error(ex)
                    }
                }
                .flatMap { accountId: Any? -> findUserByAccountId(accountId) }
                .cast(Long::class.java)
                .doOnNext { userId: Long -> setUserLastAccessTime(userId) }
                .map { userId: Long ->
                    val serverHttpRequest = exchange.request
                        .mutate()
                        .header(HttpHeaders.AUTHORIZATION, userId.toString())
                        .build()

                    exchange.mutate().request(serverHttpRequest).build()
                }
                .retry(2)
                .timeout(Duration.ofMillis(TIMEOUT_DURATION_MS))
                .onBackpressureBuffer()
                .doOnError { logger.error("$this ${it.message}", it) }
                .onErrorResume(TimeoutException::class.java) {
                    Mono.error(it)
                }
                .onErrorResume(Exception::class.java) {
                    Mono.error(it)
                }
                .then(chain.filter(exchange))
        }

    private fun setUserLastAccessTime(userId: Long) =
        // @todo. 유저 마지막 접속적보 글로벌 캐시 저장
        Mono.just(Strings.EMPTY)

    private fun findUserByAccountId(accountId: Any?) =
        // @todo. User Server 로 부터 유저 정보 받아오도록 함
        Mono.just(1L)

    private fun getPayAccountId(account: String?) =
        // @todo. 헤더로부터 토큰 추출 및 변환 후 Long Type 으로 반환
        account.let { it?.toLong() } ?: throw IllegalArgumentException()

    private fun isHealthCheck(serverHttpRequest: ServerHttpRequest) =
        listOf("/api/ping", "/actuator/health").contains(serverHttpRequest.uri.path)
}
