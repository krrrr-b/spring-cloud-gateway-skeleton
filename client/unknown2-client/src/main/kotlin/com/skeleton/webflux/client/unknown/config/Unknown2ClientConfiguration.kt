package com.skeleton.webflux.client.unknown.config

import com.skeleton.webflux.client.FeignClientConfiguration
import com.skeleton.webflux.client.unknown.client.Unknown2Client
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactivefeign.spring.config.ReactiveFeignClientsConfiguration

@Configuration
@AutoConfigureBefore(ReactiveFeignClientsConfiguration::class)
class Unknown2ClientConfiguration {
    companion object {
        const val UNKNOWN2_CLIENT = "UNKNOWN2_CLIENT"
    }

    @Bean(UNKNOWN2_CLIENT)
    fun unknown2Client(): Unknown2Client {
        return FeignClientConfiguration<Unknown2Client>()
            .reactiveFeignBuilder()
            .target(Unknown2Client::class.java, "endPoint")
    }
}
