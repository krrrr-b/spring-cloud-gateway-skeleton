package com.skeleton.webflux.common.constant.common

enum class ErrorType(val message: String) {
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    BAD_REQUEST("잘못된 요청입니다."),
    API_INTERNAL_ERROR("시스템 오류입니다."),
    API_FORBIDDEN("권한이 없습니다."),
    NOT_FOUND("존재하지 않는 정보 입니다."),
    NOT_FOUND_USER("존재하지 않는 사용자 입니다."),
    ETC_UNKNOWN_ERROR("정의 되지 않은 에러")
}
