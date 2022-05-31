package com.leijendary.spring.webflux.template.config

import io.r2dbc.postgresql.authentication.SASLAuthenticationHandler
import org.slf4j.LoggerFactory.getLogger
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.reactive.DefaultReactiveElasticsearchClient
import reactor.blockhound.BlockHound
import reactor.blockhound.BlockHound.Builder
import reactor.blockhound.integration.BlockHoundIntegration
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

    inner class ElasticsearchIntegration: BlockHoundIntegration {
        override fun applyTo(builder: Builder) {
            builder.allowBlockingCallsInside(
                DefaultReactiveElasticsearchClient::class.java.name,
                "doDecode"
            )
        }
    }

    @PostConstruct
    fun setup() {
        BlockHound.install(R2dbcIntegration(), ElasticsearchIntegration())

        log.info("Installed BlockHound")
    }
}