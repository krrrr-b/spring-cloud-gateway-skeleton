package com.skeleton.webflux.data.mongo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.mongodb.config.EnableMongoAuditing
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Configuration
@EnableMongoAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
// @EnableReactiveMongoAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
class MongodbAuditConfig {
    @Bean(name = ["auditingDateTimeProvider"])
    fun dateTimeProvider(): DateTimeProvider {
        return DateTimeProvider {
            Optional.of(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
        }
    }
}
