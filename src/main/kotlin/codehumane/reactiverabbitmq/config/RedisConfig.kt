package codehumane.reactiverabbitmq.config

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
    fun redisOperations(factory: ReactiveRedisConnectionFactory): ReactiveRedisOperations<String, Long> {
        val context = RedisSerializationContext
            .newSerializationContext<String, Long>(StringRedisSerializer())
            .hashKey(StringRedisSerializer())
            .hashValue(configureJackson2JsonRedisSerializer(Long::class.java))
            .build()

        return ReactiveRedisTemplate(factory, context)
    }

    fun <T> configureJackson2JsonRedisSerializer(clazz: Class<T>): Jackson2JsonRedisSerializer<T> {
        return Jackson2JsonRedisSerializer(clazz).apply {
            setObjectMapper(objectMapper)
        }
    }

}