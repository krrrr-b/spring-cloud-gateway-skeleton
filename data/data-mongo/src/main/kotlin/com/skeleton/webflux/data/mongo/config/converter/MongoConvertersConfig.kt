package com.skeleton.webflux.data.mongo.config.converter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import java.time.*
import java.util.*
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneId

@Configuration
class MongoConvertersConfig {
    @Bean
    fun mongoCustomConversions(): MongoCustomConversions {
        return MongoCustomConversions(listOf(
            Converter<LocalDateTime, ZonedDateTime> {
                ZonedDateTime.ofInstant(it.toInstant(ZoneOffset.UTC), ZoneId.systemDefault())
            },
            Converter<ZonedDateTime, LocalDateTime> {
                LocalDateTime.from(ZoneOffset.UTC)
            },
            Converter<String, ZonedDateTime> {
                ZonedDateTime.parse(it)
            },
            Converter<LocalDate, Date> {
                Date.from(it.atStartOfDay().atZone(ZoneOffset.UTC).toInstant())
            },
            Converter<Date, LocalDate> {
                ZonedDateTime.ofInstant(it.toInstant(), ZoneId.systemDefault()).toLocalDate()
            },
        ))
    }
}
