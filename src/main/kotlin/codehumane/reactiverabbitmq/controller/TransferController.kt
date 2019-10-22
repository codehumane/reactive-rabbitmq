package codehumane.reactiverabbitmq.controller

import codehumane.reactiverabbitmq.application.TransferService
import codehumane.reactiverabbitmq.entity.AccountBalance
import codehumane.reactiverabbitmq.entity.MoneyTransferred
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/account/{accountNumber}")
class TransferController(private val transferService: TransferService) {

    @PostMapping("/transfer")
    fun saveTransferred(
        @PathVariable("accountNumber") accountNumber: String,
        @RequestBody transferred: MoneyTransferred
    ): Mono<Void> = transferService.saveTransferred(accountNumber, transferred)

    @GetMapping("/balance")
    fun getBalance(@PathVariable("accountNumber") accountNumber: String): Mono<AccountBalance> =
        transferService.getBalance(accountNumber)

    @GetMapping(
        value = ["/transfer/stream"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun streamTransferred(@PathVariable("accountNumber") accountNumber: String): Flux<ServerSentEvent<MoneyTransferred>> =
        transferService
            .getTransferred(accountNumber)
            .map { ServerSentEvent.builder<MoneyTransferred>().data(it).build() }

}