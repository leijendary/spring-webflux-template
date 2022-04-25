package com.leijendary.spring.webflux.template.core.exception

import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

abstract class ServerErrorException : StatusException(INTERNAL_SERVER_ERROR)