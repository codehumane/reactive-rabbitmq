package codehumane.reactiverabbitmq

import com.rabbitmq.client.Delivery
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.Sender
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class SendReceiveTestApplicationRunner(
    private val sender: Sender,
    private val deliveryFlux: Flux<Delivery>,
    @Value("\${demo.rabbitmq.queue.name}") private val queueName: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(SendReceiveTestApplicationRunner::class.java)

    override fun run(args: ApplicationArguments?) {

        deliveryFlux
            .subscribe { log.info("received: ${String(it.body)}") }

        val messages = Flux
            .range(0, 10)
            .delayElements(Duration.ofMillis(100))
            .map { OutboundMessage("", queueName, "message-$it".toByteArray()) }

        sender
            .send(messages)
            .subscribe()

        TimeUnit.SECONDS.sleep(10)
    }
    
}