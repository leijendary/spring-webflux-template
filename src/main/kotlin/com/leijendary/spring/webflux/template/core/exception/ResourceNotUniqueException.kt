package com.leijendary.spring.webflux.template.core.exception

class ResourceNotUniqueException(val source: List<String>, val value: String) : ConflictException()