package com.leijendary.spring.webflux.template.api.v1.data

import com.leijendary.spring.webflux.template.core.data.TranslationRequest
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class SampleTranslationRequest(
    @field:NotBlank(message = "validation.required")
    @field:Size(max = 100, message = "validation.maxLength")
    val name: String? = null,

    @field:NotBlank(message = "validation.required")
    @field:Size(max = 200, message = "validation.maxLength")
    val description: String? = null
) : TranslationRequest()