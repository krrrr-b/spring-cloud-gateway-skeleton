package com.skeleton.webflux.common.constant.config

import java.util.Optional

enum class Profile {
    test, local, dev, alpha, staging, live;

    companion object {
        fun findProfile(profile: String): Optional<Profile> {
            return values().toMutableList()
                .stream()
                .filter { it.name == profile }
                .findFirst()
        }

        fun isLocal(phase: String) = local.name == phase
    }
}
