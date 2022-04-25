package com.leijendary.spring.webflux.template.core.config

import com.leijendary.spring.webflux.template.core.config.properties.*
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    AuthProperties::class,
    InfoProperties::class,
    NumberProperties::class,
    R2dbcBatchProperties::class,
    R2dbcPrimaryProperties::class,
    R2dbcReadonlyProperties::class,
    RetryProperties::class
)
class PropertiesConfiguration 