package com.leijendary.spring.webflux.template.model

import com.leijendary.spring.webflux.template.core.model.LocaleModel
import org.springframework.data.relational.core.mapping.Table

@Table
class SampleTableTranslation : LocaleModel() {
    lateinit var name: String
    lateinit var description: String
}