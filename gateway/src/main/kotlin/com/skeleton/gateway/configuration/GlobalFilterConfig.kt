package com.skeleton.gateway.configuration

import com.kakaopay.wakanda.portal.api.common.ApiGatewayAccount
import org.reactivestreams.Publisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.StringUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import java.time.Duration
import java.util.*
import java.util.function.Function

@Slf4j
@Configuration
class GlobalFilterConfig(
    reactiveRedisHashMapTemplate: ReactiveRedisTemplate<String?, HashMap<Long?, String?>?>,
    environmentHelper: EnvironmentHelper,
    partnerApiClient: PartnerApiClient,
    @Qualifier(OBJECT_MAPPER_NAME) objectMapper: ObjectMapper
) {
    private val reactiveRedisHashMapTemplate: ReactiveRedisTemplate<String, HashMap<Long, String>>
    private val environmentHelper: EnvironmentHelper
    private val partnerApiClient: PartnerApiClient
    private val objectMapper: ObjectMapper
    private val DELAY_DURATION = Duration.ofMillis(100)
    private val TIMEOUT_DURATION = Duration.ofMillis(10000)
    private val USER_LAST_ACCESS_DATA_EXPIRE_TIME = Duration.ofDays(2)
    @Bean
    fun accountMappingFilter(): GlobalFilter {
        val event = "GlobalFilterConfig#accountMappingFilter"
        return label@ GlobalFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val httpHeaders: HttpHeaders = exchange.getRequest().getHeaders()
            if (isHealthCheck(exchange.getRequest())) {
                return@label exchange.getResponse()
                    .writeAndFlushWith(
                        Flux.just(HEALTH_CHECK_RETURN_KEYWORD)
                            .delayElements(DELAY_DURATION)
                            .map { body: String ->
                                Mono.just<DataBuffer>(
                                    exchange.getResponse()
                                        .bufferFactory()
                                        .allocateBuffer()
                                        .write(body.toByteArray())
                                )
                            })
            }
            if (isForceAppUpdate(exchange.getRequest())) {
                return@label Mono.error(LowApplicationVersionException(UpdateType.FORCE.getDescription()))
            }
            GlobalFilterConfig.log.info("[{}] request_header: {}", event, httpHeaders)
            Flux.just(Objects.requireNonNull(httpHeaders.getFirst(PAY_API_GATEWAY_HEADER_KEY)))
                .switchIfEmpty(Mono.error(InvalidAuthenticationException("사용자 정보를 찾을 수 없습니다.")))
                .handle { account: String?, synchronousSink: SynchronousSink<Any?> ->
                    try {
                        if (StringUtils.isEmpty(account) || isNotRequiredAuthenticationRequest(exchange.getRequest())) {
                            synchronousSink.complete()
                        } else {
                            synchronousSink.next(getPayAccountId(account))
                        }
                    } catch (ex: IOException) {
                        synchronousSink.error(ex)
                    }
                }
                .flatMap { payAccountId: Any? -> findUserByPayAccountId(payAccountId) }
                .cast(Long::class.java)
                .doOnNext { userId: Long -> storeUserLastAccessTime(userId) }
                .map<ServerWebExchange>(Function<Long, ServerWebExchange> { userId: Long ->
                    val serverHttpRequest: ServerHttpRequest = exchange.getRequest()
                        .mutate()
                        .header(PORTAL_GATEWAY_HEADER_KEY, userId.toString())
                        .build()
                    exchange.mutate().request(serverHttpRequest).build()
                })
                .timeout(TIMEOUT_DURATION) // 삽입, 삭제, 수정시에만 조절하는 방식으로 변경 (f, RouteLocator)
                // .delaySequence(delayDuration)
                // .limitRate(limitRateRequestCount)
                .onBackpressureBuffer()
                .onErrorResume(Function<Throwable, Publisher<out ServerWebExchange>> { ex: Throwable ->
                    val message = java.lang.String.format(
                        "[%s] message: %s, path: %s, %s: %s",
                        event,
                        ex.message,
                        exchange.getRequest().getURI().getPath(),
                        PAY_API_GATEWAY_HEADER_KEY,
                        httpHeaders.getFirst(PAY_API_GATEWAY_HEADER_KEY)
                    )
                    GlobalFilterConfig.log.error("[GlobalFilterConfig#accountMappingFilter {}] {}", message, ex)
                    val errorMessage = errorMessage(ex.message)
                    if (ex is InvalidAuthenticationException) {
                        return@onErrorResume Mono.error(InvalidAuthenticationException(errorMessage))
                    }
                    Mono.error(InvalidArgumentsException(errorMessage))
                })
                .then<Void>(chain.filter(exchange))
        }
    }

    private fun storeUserLastAccessTime(userId: Long): Mono<String> {
        val redisKey = java.lang.String.format(
            "%s:%s-%s",
            environmentHelper.getCachePrefix(),
            PORTAL_USER_LAST_ACCESS_AT_KEY,
            LocalDate.now()
        )
        val now: String = LocalDateTime.now().format(JacksonConfig.COMMON_OBJECT_MAPPER_FORMATTER)
        reactiveRedisHashMapTemplate.opsForValue()
            .get(redisKey)
            .defaultIfEmpty(HashMap<Long, String>())
            .map<HashMap<Long, String>>(Function { dataList: HashMap<Long?, String?> ->
                dataList[userId] = now
                dataList
            })
            .flatMap<Boolean>(Function<HashMap<Long?, String?>, Mono<out Boolean>> { dataList: HashMap<Long?, String?>? ->
                reactiveRedisHashMapTemplate.opsForValue()
                    .set(redisKey, dataList, USER_LAST_ACCESS_DATA_EXPIRE_TIME)
            })
            .subscribe()
        return Mono.just(now)
    }

    private fun findUserByPayAccountId(payAccountId: Any?): Mono<Long?> {
        return partnerApiClient.findUserByPayAccountIdMono(payAccountId as Long?)
            .switchIfEmpty(Mono.error(InvalidAuthenticationException("사용자 정보를 찾을 수 없습니다.")))
            .flatMap { account ->
                GlobalFilterConfig.log.info("[GlobalFilterConfig#accountMappingFilter] account: {}", account.toString())
                Mono.just(account.getId())
            }
    }

    @Throws(IOException::class)
    private fun getPayAccountId(account: String?): Long {
        return objectMapper.readValue(account, ApiGatewayAccount::class.java)
            .getId()
    }

    private fun errorMessage(errorResponse: String?): String? {
        return try {
            objectMapper.readValue(errorResponse, BaseErrorResponse::class.java)
                .getErrorMessage()
        } catch (ex: IOException) {
            errorResponse
        }
    }

    private fun isNotRequiredAuthenticationRequest(serverHttpRequest: ServerHttpRequest): Boolean {
        return isUserRegistrationRequest(serverHttpRequest)
    }

    private fun isUserRegistrationRequest(serverHttpRequest: ServerHttpRequest): Boolean {
        val USER_REQUEST_END_POINT_PATH = "/users/registration"
        return HttpMethod.POST == serverHttpRequest.method && USER_REQUEST_END_POINT_PATH == serverHttpRequest.uri.path
    }

    private fun isSettingRegistrationRequest(serverHttpRequest: ServerHttpRequest): Boolean {
        val PUSH_SETTING_REQUEST_END_POINT_PATH = "/settings/push"
        return HttpMethod.POST == serverHttpRequest.method && PUSH_SETTING_REQUEST_END_POINT_PATH == serverHttpRequest.uri.path
    }

    private fun isForceAppUpdate(serverHttpRequest: ServerHttpRequest): Boolean {
        if (isSettingRegistrationRequest(serverHttpRequest)) {
            return false
        }

        // *important 강업이 필요한 시점부터 적용
        val isBusinessAppForceUpdate = false
        val currentBusinessAppVersion = "1.0.0"
        if (!isBusinessAppForceUpdate) {
            return false
        }
        val userAgent = serverHttpRequest.headers.getFirst(GatewayHeader.USER_AGENT_HEADER_KEY)
        val userAppVersion: String = UserAgentUtils.getAppVersion(userAgent)
        return currentBusinessAppVersion != userAppVersion
    }

    private fun isHealthCheck(serverHttpRequest: ServerHttpRequest): Boolean {
        val path = serverHttpRequest.uri.path
        return path == "/api/ping" || path == "/actuator/health" || path == "/favicon.ico"
    }

    companion object {
        private const val HEALTH_CHECK_RETURN_KEYWORD = "pong"
    }

    init {
        this.reactiveRedisHashMapTemplate = reactiveRedisHashMapTemplate
        this.environmentHelper = environmentHelper
        this.partnerApiClient = partnerApiClient
        this.objectMapper = objectMapper
    }
}