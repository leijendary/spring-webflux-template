package com.leijendary.spring.webflux.template.config

import io.r2dbc.postgresql.authentication.SASLAuthenticationHandler
import kotlinx.coroutines.debug.CoroutinesBlockHoundIntegration
import org.slf4j.LoggerFactory.getLogger
import org.springframework.context.annotation.Configuration
import reactor.blockhound.BlockHound
import reactor.blockhound.BlockHound.Builder
import reactor.blockhound.integration.BlockHoundIntegration
import reactor.blockhound.integration.ReactorIntegration
import reactor.blockhound.integration.RxJava2Integration
import javax.annotation.PostConstruct

@Configuration
class BlockHoundConfiguration {
    private val log = getLogger(javaClass)

    inner class R2dbcIntegration : BlockHoundIntegration {
        override fun applyTo(builder: Builder) {
            builder.allowBlockingCallsInside(
                SASLAuthenticationHandler::class.java.name,
                "handleAuthenticationSASL"
            )
        }
    }

    @PostConstruct
    fun setup() {
        BlockHound.install(
            CoroutinesBlockHoundIntegration(),
            ReactorIntegration(),
            RxJava2Integration(),
            R2dbcIntegration(),
        )

        log.info("Installed BlockHound")
    }
}