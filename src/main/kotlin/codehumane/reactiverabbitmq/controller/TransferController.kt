package codehumane.reactiverabbitmq.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Delivery
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.Sender

@RestController
class TransferController(
    private val sender: Sender,
    private val deliveryFlux: Flux<Delivery>,
    private val objectMapper: ObjectMapper,
    @Value("\${demo.rabbitmq.queue.name}") private val queueName: String,
    redisOperations: ReactiveRedisOperations<String, Long>
) {

    private val accountBalanceHashKey = "ACCOUNT:BALANCE"
    private val opsForHash = redisOperations.opsForHash<String, Long>()

    @PostMapping("/account/{number}/transfer")
    fun saveTransferred(@PathVariable("number") accountNumber: String, @RequestBody transferred: MoneyTransferred): Mono<Void> {
        require(accountNumber == transferred.account.number)

        return opsForHash
            .increment(accountBalanceHashKey, accountNumber, transferred.amount.value)
            .map { OutboundMessage("", queueName, objectMapper.writeValueAsBytes(transferred)) }
            .let { sender.send(it) }
    }

    @GetMapping("/account/{number}/balance")
    fun getBalance(@PathVariable("number") accountNumber: String): Mono<AccountBalance> {

        return opsForHash
            .get(accountBalanceHashKey, accountNumber)
            .map { AccountBalance(Account(accountNumber), Money(it)) }
    }

    @GetMapping("/account/{number}/transfer/stream")
    fun streamTransferred(@PathVariable("number") accountNumber: String): Flux<ServerSentEvent<MoneyTransferred>> {

        return deliveryFlux
            .map { objectMapper.readValue(it.body, MoneyTransferred::class.java) }
            .filter { accountNumber == it.account.number }
            .map { ServerSentEvent.builder<MoneyTransferred>().data(it).build() }
    }

}

data class Account(
    val number: String
)

data class Money(
    val value: Long
)

data class AccountBalance(
    val account: Account,
    val balance: Money
)

data class MoneyTransferred(
    val account: Account,
    val amount: Money
)
