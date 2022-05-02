package com.leijendary.spring.webflux.template.api.v1.data

import com.leijendary.spring.webflux.template.core.validator.annotation.UniqueFields
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.*

data class SampleRequest(
    @field:NotBlank(message = "validation.required")
    @field:Size(max = 100, message = "validation.maxLength")
    val field1: String?,

    @field:NotNull(message = "validation.required")
    @field:Min(value = 0, message = "validation.min")
    val field2: Long?,

    @field:NotNull(message = "validation.required")
    @field:DecimalMin(value = "0.01", message = "validation.decimal.min")
    @field:DecimalMax(value = "9999999999.99", message = "validation.decimal.max")
    val amount: BigDecimal?,

    @field:Valid
    @field:UniqueFields(uniqueFields = ["name", "language", "ordinal"])
    @field:NotEmpty(message = "validation.required")
    val translations: List<SampleTranslationRequest>? = ArrayList()
)