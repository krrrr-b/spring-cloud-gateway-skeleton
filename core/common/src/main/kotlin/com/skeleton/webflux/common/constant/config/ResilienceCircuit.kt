package com.skeleton.webflux.common.constant.config

import java.time.Duration

enum class ResilienceCircuit(
    // @description 써킷 이름
    val circuitName: String = "defaultCircuitBreaker",
    val slowCallOfDuration: Duration = Duration.ofMillis(4000L),
    val slowCallRate: Float = 20f,
    val slidingWindowSize: Int = 50,
    val halfOpenState: Int = 10,
    val minimumNumberOfCalls: Int = 25,
    val failureRate: Float = 20f,

    // @description open 상태를 유지하는 지속 시간 (이후에 half-open 상태로 변경)
    val waitDurationInOpenStateMillis: Long = 60000L,
) {
    COMMON,
}
