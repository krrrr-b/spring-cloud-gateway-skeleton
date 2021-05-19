package com.skeleton.webflux.internal.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {
    @Bean
    fun springShopOpenAPI(): OpenAPI {
        return OpenAPI().info(Info().title("시스템 도큐멘트️"))
    }

    @Bean
    fun internalApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("webflux-internal")
            .pathsToMatch("/api-internal/**")
            .build()
    }

    @Bean
    fun api(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("webflux")
            .pathsToMatch("/api/**")
            .build()
    }

    @Bean
    fun circuit(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("webflux-infra")
            .pathsToMatch("/infra/**")
            .build()
    }
}
