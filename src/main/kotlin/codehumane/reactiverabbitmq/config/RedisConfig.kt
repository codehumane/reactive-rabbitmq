package codehumane.reactiverabbitmq.config

import codehumane.reactiverabbitmq.entity.AccountBalance
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfig(private val objectMapper: ObjectMapper) {

    @Bean
    fun redisOperations(factory: ReactiveRedisConnectionFactory): ReactiveRedisOperations<String, AccountBalance> {
        val context = RedisSerializationContext
            .newSerializationContext<String, AccountBalance>(StringRedisSerializer())
            .hashKey(StringRedisSerializer())
            .hashValue(configureJackson2JsonRedisSerializer(AccountBalance::class.java))
            .build()

        return ReactiveRedisTemplate(factory, context)
    }

    fun <T> configureJackson2JsonRedisSerializer(t: Class<T>): Jackson2JsonRedisSerializer<T> {
        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(t)
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper)

        return jackson2JsonRedisSerializer
    }

}