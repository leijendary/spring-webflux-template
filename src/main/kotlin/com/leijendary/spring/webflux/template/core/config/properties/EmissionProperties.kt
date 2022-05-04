package com.leijendary.spring.webflux.template.core.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration
import java.time.Duration.ofMinutes

@ConfigurationProperties(prefix = "emission")
class EmissionProperties {
    var deadline: Duration = ofMinutes(1)
}