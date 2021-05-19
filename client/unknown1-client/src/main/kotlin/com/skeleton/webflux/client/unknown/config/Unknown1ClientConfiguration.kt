package com.skeleton.webflux.client.unknown.config

import com.skeleton.webflux.client.FeignClientConfiguration
import com.skeleton.webflux.client.unknown.client.Unknown1Client
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactivefeign.spring.config.ReactiveFeignClientsConfiguration

@Configuration
@AutoConfigureBefore(ReactiveFeignClientsConfiguration::class)
class Unknown1ClientConfiguration {
    companion object {
        const val UNKNOWN1_CLIENT = "UNKNOWN1_CLIENT"
    }

    @Bean(UNKNOWN1_CLIENT)
    fun unknown1Client(): Unknown1Client {
        return FeignClientConfiguration<Unknown1Client>()
            .reactiveFeignBuilder()
            .target(Unknown1Client::class.java, "endPoint")
    }
}
