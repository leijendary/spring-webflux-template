package com.leijendary.spring.webflux.template

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.blockhound.BlockHound
import java.util.*
import java.util.stream.Collectors


@SpringBootApplication
class Application

fun main(args: Array<String>) {
    BlockHound
        .builder()
        .blockingMethodCallback {
            val itemList: List<StackTraceElement> = Arrays.stream(Exception(it.toString()).stackTrace)
                .filter { i -> i.toString().contains(Application::class.java.packageName) }
                .collect(Collectors.toList())

            println("Find block operation: \n${itemList}")
        }
        .install()

    runApplication<Application>(*args)
}
