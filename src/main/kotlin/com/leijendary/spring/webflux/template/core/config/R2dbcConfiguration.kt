package com.leijendary.spring.webflux.template.core.config

import com.leijendary.spring.webflux.template.core.config.properties.R2dbcPrimaryProperties
import com.leijendary.spring.webflux.template.core.config.properties.R2dbcReadonlyProperties
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.ConnectionMode.READ_ONLY
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.ConnectionMode.READ_WRITE
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import java.time.Duration.ofMillis
import java.time.LocalDateTime.now
import java.util.Optional.of
import kotlin.Long.Companion.MAX_VALUE

@Configuration
@EnableR2dbcAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "dateTimeProvider")
class R2dbcConfiguration(
    private val primaryProperties: R2dbcPrimaryProperties,
    private val readonlyProperties: R2dbcReadonlyProperties
) : AbstractR2dbcConfiguration() {
    @Bean
    fun auditorAware(): ReactiveAuditorAware<String> {
        return ReactiveAuditorAware {
            ReactiveSecurityContextHolder
                .getContext()
                .mapNotNull { it.authentication.name }
        }
    }

    @Bean
    fun dateTimeProvider(): DateTimeProvider {
        return DateTimeProvider { of(now()) }
    }

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val connectionFactories = mutableMapOf(
            READ_WRITE to primaryConnectionFactory(),
            READ_ONLY to readOnlyConnectionFactory()
        )
        val clusterConnectionFactory = ClusterConnectionFactory()
        clusterConnectionFactory.setTargetConnectionFactories(connectionFactories)
        clusterConnectionFactory.setDefaultTargetConnectionFactory(connectionFactories[READ_WRITE]!!)

        return clusterConnectionFactory
    }

    private fun primaryConnectionFactory(): ConnectionPool {
        return connectionPool(primaryProperties)
    }

    private fun readOnlyConnectionFactory(): ConnectionPool {
        return connectionPool(readonlyProperties)
    }

    private fun connectionPool(properties: R2dbcProperties): ConnectionPool {
        val options = builder()
            .from(parse(properties.url))
            .option(USER, properties.username)
            .option(PASSWORD, properties.password)
            .build()
        val factory = ConnectionFactories.get(options)
        val pool = properties.pool
        val configuration = ConnectionPoolConfiguration
            .builder(factory)
            .name(properties.name)
            .initialSize(pool.initialSize)
            .maxSize(pool.maxSize)
            .maxLifeTime(ofMillis(MAX_VALUE))
            .build()

        return ConnectionPool(configuration)
    }
}