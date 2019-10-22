package codehumane.reactiverabbitmq.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObjectMapperConfig {

    @Bean
    fun customObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        }
    }

}