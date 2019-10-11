package codehumane.reactiverabbitmq

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Delivery
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.rabbitmq.RabbitFlux
import reactor.rabbitmq.ReceiverOptions
import reactor.rabbitmq.Sender
import reactor.rabbitmq.SenderOptions
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Configuration
class RabbitMqConfig(
    private val amqpAdmin: AmqpAdmin,
    private val senderConnectionMono: Mono<Connection>,
    private val receiverConnectionMono: Mono<Connection>,
    @Value("\${demo.rabbitmq.queue.name}") private val queueName: String
) {

    @PostConstruct
    fun init() {
        amqpAdmin.declareQueue(Queue(queueName))
    }

    @PreDestroy
    fun close() {
        senderConnectionMono.block()?.close()
        receiverConnectionMono.block()?.close()
    }

    @Bean
    fun sender(senderConnectionMono: Mono<Connection>): Sender {
        val senderOptions = SenderOptions()
            .connectionMono(senderConnectionMono)
            .connectionSupplier { it.newConnection("sender") }

        return RabbitFlux.createSender(senderOptions)
    }

    @Bean
    fun deliveryFlux(receiverConnectionMono: Mono<Connection>): Flux<Delivery> {
        val receiverOptions = ReceiverOptions()
            .connectionMono(receiverConnectionMono)
            .connectionSupplier { it.newConnection("receiver") }

        return RabbitFlux
            .createReceiver(receiverOptions)
            .consumeAutoAck(queueName)
    }

    @Configuration
    class ConnectionConfig {

        @Bean
        fun connectionFactory(properties: RabbitProperties): ConnectionFactory {
            return ConnectionFactory().apply {
                host = properties.host
                port = properties.port
                username = properties.username
                password = properties.password
                useNio()
            }
        }

        @Bean
        fun senderConnectionMono(connectionFactory: ConnectionFactory): Mono<Connection> {
            return Mono.fromCallable { connectionFactory.newConnection("sender") }.cache()
        }

        @Bean
        fun receiverConnectionMono(connectionFactory: ConnectionFactory): Mono<Connection> {
            return Mono.fromCallable { connectionFactory.newConnection("receiver") }.cache()
        }

    }
}