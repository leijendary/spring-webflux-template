package com.leijendary.spring.webflux.template.core.util

import com.leijendary.spring.webflux.template.core.config.properties.EmissionProperties
import com.leijendary.spring.webflux.template.core.util.SpringContext.Companion.getBean
import reactor.core.publisher.Sinks.EmitFailureHandler.busyLooping

private val emissionProperties = getBean(EmissionProperties::class)

object EmitHandler {
    fun failureHandler() = busyLooping(emissionProperties.deadline)
}