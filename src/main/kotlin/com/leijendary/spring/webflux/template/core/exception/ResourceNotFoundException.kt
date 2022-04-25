package com.leijendary.spring.webflux.template.core.exception

class ResourceNotFoundException(val source: List<Any>, val identifier: Any) : NotFoundException()