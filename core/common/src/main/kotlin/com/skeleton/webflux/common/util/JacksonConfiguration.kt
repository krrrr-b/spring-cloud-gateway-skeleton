package com.skeleton.webflux.common.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import org.apache.commons.lang3.StringUtils
import kotlin.Throws

@Configuration
class JacksonConfiguration {
    companion object {
        const val COMMON_OBJECT_MAPPER_FORMAT: String = "yyyy-MM-dd HH:mm:ss"
    }

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                registerModules(
                    createJavaTimeModule(DateTimeFormatter.ofPattern(COMMON_OBJECT_MAPPER_FORMAT)),
                    createBigDecimalModule(),
                    KotlinModule(),
                    TrimModule()
                )
            }
    }

    internal class TrimModule : SimpleModule() {
        init {
            addDeserializer(String::class.java,
                object : StdScalarDeserializer<String?>(String::class.java) {
                    @Throws(IOException::class, JsonProcessingException::class)
                    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): String {
                        return StringUtils.trim(jp.valueAsString)
                    }
                })
        }
    }

    private fun createJavaTimeModule(formatter: DateTimeFormatter): JavaTimeModule {
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(
            LocalDateTime::class.java,
            object : JsonSerializer<LocalDateTime>() {
                @Throws(IOException::class)
                override fun serialize(
                    value: LocalDateTime,
                    gen: JsonGenerator,
                    serializers: SerializerProvider,
                ) {
                    gen.writeString(value.truncatedTo(ChronoUnit.NANOS).format(formatter))
                }
            }
        )

        javaTimeModule.addDeserializer(
            LocalDateTime::class.java,
            object : JsonDeserializer<LocalDateTime?>() {
                @Throws(IOException::class)
                override fun deserialize(
                    p: JsonParser,
                    ctxt: DeserializationContext,
                ): LocalDateTime? {
                    return LocalDateTime.parse(p.valueAsString, formatter)
                }
            }
        )

        return javaTimeModule
    }

    private fun createBigDecimalModule(): SimpleModule {
        val bigDecimalModule = SimpleModule()
        bigDecimalModule.addSerializer(
            BigDecimal::class.java,
            object : JsonSerializer<BigDecimal>() {
                @Throws(IOException::class)
                override fun serialize(
                    value: BigDecimal,
                    gen: JsonGenerator,
                    serializers: SerializerProvider,
                ) {
                    gen.writeString(value.stripTrailingZeros().toPlainString())
                }
            }
        )

        return bigDecimalModule
    }
}
