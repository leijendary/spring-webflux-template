package com.leijendary.spring.webflux.template.core.exception

import org.springframework.http.HttpStatus.NOT_FOUND

abstract class NotFoundException : StatusException(NOT_FOUND)