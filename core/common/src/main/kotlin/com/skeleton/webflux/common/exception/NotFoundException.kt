package com.skeleton.webflux.common.exception

import lombok.Getter

@Getter
open class NotFoundException() : RuntimeException()
