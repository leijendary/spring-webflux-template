package com.leijendary.spring.webflux.template.core.exception

import org.springframework.http.HttpStatus.BAD_REQUEST

abstract class BadRequestException : StatusException(BAD_REQUEST)