package com.leijendary.spring.webflux.template.core.exception

import kotlin.reflect.KClass

class PathVariableBindException(val name: String, val value: String, val type: KClass<out Any>) : BadRequestException()