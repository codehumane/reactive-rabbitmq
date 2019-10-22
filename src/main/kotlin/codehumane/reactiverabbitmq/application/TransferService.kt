package codehumane.reactiverabbitmq.application

import codehumane.reactiverabbitmq.entity.Account
import codehumane.reactiverabbitmq.entity.AccountBalance
import codehumane.reactiverabbitmq.entity.Money
import codehumane.reactiverabbitmq.entity.MoneyTransferred
import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Delivery
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.Sender

@Service
class TransferService(
    private val sender: Sender,
    private val deliveryFlux: Flux<Delivery>,
    private val objectMapper: ObjectMapper,
    @Value("\${demo.rabbitmq.queue.name}") private val queueName: String,
    redisOperations: ReactiveRedisOperations<String, AccountBalance>
) {

    private val balanceValueHashKey = "BALANCE-VALUE"
    private val accountBalanceHashKey = "ACCOUNT-BALANCE"
    private val opsForHash = redisOperations.opsForHash<String, AccountBalance>()

    fun saveTransferred(accountNumber: String, transferred: MoneyTransferred): Mono<Void> {

        val outboundMessage = opsForHash
            .increment(balanceValueHashKey, accountNumber, transferred.amount.value)
            .flatMap { saveAccountBalance(accountNumber, it) }
            .map { toOutboundMessage(transferred) }

        return sender.send(outboundMessage)
    }

    private fun saveAccountBalance(accountNumber: String, it: Long): Mono<Boolean> {
        val balance = AccountBalance(
            Account(accountNumber),
            Money(it)
        )

        return opsForHash.put(accountBalanceHashKey, accountNumber, balance)
    }

    private fun toOutboundMessage(transferred: MoneyTransferred) =
        OutboundMessage("", queueName, objectMapper.writeValueAsBytes(transferred))

    fun getBalance(accountNumber: String): Mono<AccountBalance> = opsForHash.get(accountBalanceHashKey, accountNumber)

    fun getTransferred(accountNumber: String): Flux<MoneyTransferred> = deliveryFlux
        .map { objectMapper.readValue(it.body, MoneyTransferred::class.java) }
        .filter { accountNumber == it.account.number }


}