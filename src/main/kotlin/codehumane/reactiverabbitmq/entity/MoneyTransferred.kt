package codehumane.reactiverabbitmq.entity

data class MoneyTransferred(
    val account: Account,
    val amount: Money
)
