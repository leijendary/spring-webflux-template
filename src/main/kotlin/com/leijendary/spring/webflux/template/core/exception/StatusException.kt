package com.leijendary.spring.webflux.template.core.exception

import org.springframework.http.HttpStatus

abstract class StatusException(val httpStatus: HttpStatus) : RuntimeException()