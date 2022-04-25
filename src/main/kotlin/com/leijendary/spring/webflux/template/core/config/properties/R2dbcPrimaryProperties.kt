package com.leijendary.spring.webflux.template.core.config.properties

import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.r2dbc.primary")
class R2dbcPrimaryProperties : R2dbcProperties()