package com.leijendary.spring.webflux.template.core.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.r2dbc.batch")
class R2dbcBatchProperties {
    val size: Int = 1000
}