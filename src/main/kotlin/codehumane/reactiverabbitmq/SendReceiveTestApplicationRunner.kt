package codehumane.reactiverabbitmq

import com.rabbitmq.client.Delivery
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.Sender
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Component
class SendReceiveTestApplicationRunner(
    private val sender: Sender,
    private val deliveryFlux: Flux<Delivery>,
    @Value("\${demo.rabbitmq.queue.name}") private val queueName: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(SendReceiveTestApplicationRunner::class.java)
    private val messageCount = 1_000

    override fun run(args: ApplicationArguments?) {
        calculateTime {
            waitTaskDone {
                subscribeMessages(it)
                sendMessages()
            }
        }
    }

    private fun subscribeMessages(latch: CountDownLatch) {
        deliveryFlux.subscribe {
            log.info("received: ${String(it.body)}")
            latch.countDown()
        }
    }

    private fun sendMessages(): Disposable {
        val messages = Flux
            .range(1, messageCount)
            // .delayElements(Duration.ofMillis(100))
            .map { OutboundMessage("", queueName, "message-$it".toByteArray()) }

        return sender
            .send(messages)
            .subscribe()
    }

    private fun waitTaskDone(function: (CountDownLatch) -> Unit) {
        val latch = CountDownLatch(messageCount)
        function.invoke(latch)
        latch.await(10, TimeUnit.SECONDS)
    }

    fun calculateTime(function: () -> Unit) {
        val stopWatch = StopWatch()
        stopWatch.start()

        // real function
        function.invoke()

        stopWatch.stop()
        log.info("runner took ${stopWatch.totalTimeMillis}ms")
    }
}