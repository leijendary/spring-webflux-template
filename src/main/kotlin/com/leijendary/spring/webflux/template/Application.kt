package com.leijendary.spring.webflux.template

import kotlinx.coroutines.debug.CoroutinesBlockHoundIntegration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.blockhound.BlockHound
import reactor.blockhound.integration.ReactorIntegration
import reactor.blockhound.integration.RxJava2Integration
import reactor.blockhound.integration.StandardOutputIntegration

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    BlockHound
        .builder()
        .with(CoroutinesBlockHoundIntegration())
        .with(StandardOutputIntegration())
        .with(ReactorIntegration())
        .with(RxJava2Integration())
        .install()

    runApplication<Application>(*args)
}
