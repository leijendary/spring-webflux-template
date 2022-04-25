package com.leijendary.spring.webflux.template.core.exception

import org.springframework.validation.BindingResult

class FieldValidationException(val bindingResult: BindingResult) : BadRequestException()