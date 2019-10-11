package codehumane.reactiverabbitmq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveRabbitmqApplication

fun main(args: Array<String>) {
	runApplication<ReactiveRabbitmqApplication>(*args)
}
