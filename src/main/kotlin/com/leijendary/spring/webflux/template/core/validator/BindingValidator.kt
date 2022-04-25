package com.leijendary.spring.webflux.template.core.validator

import com.leijendary.spring.webflux.template.core.exception.FieldValidationException
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Validator

@Component
class BindingValidator(private val validator: Validator) {
    fun validate(body: Any) {
        val bindingResult = BeanPropertyBindingResult(body, body.javaClass.name)

        validator.validate(body, bindingResult)

        if (bindingResult.hasErrors()) {
            throw FieldValidationException(bindingResult)
        }
    }
}