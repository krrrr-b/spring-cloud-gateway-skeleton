package com.skeleton.webflux.common.util

import com.skeleton.webflux.common.constant.config.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class EnvironmentHelper(
    val environment: Environment
) {
    val activeProfile: String
        get() = environment.activeProfiles[0]

    val isLocalProfile: Boolean
        get() = environment.activeProfiles[0] == Profile.local.name

    val isDevProfile: Boolean
        get() = environment.activeProfiles[0] == Profile.dev.name

    val isAlphaProfile: Boolean
        get() = environment.activeProfiles[0] == Profile.alpha.name

    val isTest: Boolean
        get() = activeProfile == Profile.test.name

    val isStagingAndLive: Boolean
        get() = activeProfile == Profile.staging.name || activeProfile == Profile.live.name

    val cachePrefix: String
        get() {
            return when (activeProfile) {
                Profile.test.name -> Profile.test.name.plus(":")
                Profile.local.name, Profile.dev.name -> Profile.dev.name.plus(":")
                Profile.alpha.name -> Profile.alpha.name.plus(":")
                else -> ""
            }
        }
}
