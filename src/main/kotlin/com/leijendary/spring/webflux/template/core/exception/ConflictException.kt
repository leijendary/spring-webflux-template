package com.leijendary.spring.webflux.template.core.exception

import org.springframework.http.HttpStatus.CONFLICT

abstract class ConflictException : StatusException(CONFLICT)