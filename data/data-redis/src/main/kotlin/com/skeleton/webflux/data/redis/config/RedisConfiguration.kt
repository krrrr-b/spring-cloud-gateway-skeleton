package com.skeleton.webflux.data.redis.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.util.stream.Collectors

@Configuration
class RedisConfiguration(
    private val redisClusters: List<String>,
    private val password: String,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        const val REACTIVE_REDIS_CONNECTION_FACTORY = "REACTIVE_REDIS_CONNECTION_FACTORY"
        const val REACTIVE_REDIS_TEMPLATE = "REACTIVE_REDIS_TEMPLATE"
    }

    @Primary
    @Bean(REACTIVE_REDIS_CONNECTION_FACTORY)
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory(redisClusterConfiguration(), poolConfig())
    }

    @Primary
    @Bean(REACTIVE_REDIS_TEMPLATE)
    fun reactiveRedisTemplate(@Qualifier(REACTIVE_REDIS_CONNECTION_FACTORY) connectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, String>(StringRedisSerializer())
            .hashKey(StringRedisSerializer())
            .hashValue(Jackson2JsonRedisSerializer(String::class.java))
            .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }

    @Bean
    fun reactiveRedisHashMapTemplate(@Qualifier(REACTIVE_REDIS_CONNECTION_FACTORY) connectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, HashMap<Long, String>> {
        val jackson2JsonRedisSerializer: Jackson2JsonRedisSerializer<*> =
            Jackson2JsonRedisSerializer(
                HashMap::class.java
            )

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper)

        return ReactiveRedisTemplate(
            connectionFactory,
            RedisSerializationContext
                .newSerializationContext<String, HashMap<Long, String>>(
                    GenericJackson2JsonRedisSerializer()
                )
                .hashKey(StringRedisSerializer())
                .hashValue(jackson2JsonRedisSerializer)
                .build()
        )
    }

    fun poolConfig(): LettuceClientConfiguration {
        return LettucePoolingClientConfiguration.builder()
            .poolConfig(GenericObjectPoolConfig<Any>())
            .build()
    }

    fun redisClusterConfiguration(): RedisClusterConfiguration {
        return RedisClusterConfiguration(redisClusters.stream().collect(Collectors.toList()))
            .also { it.setPassword(password) }
    }
}
