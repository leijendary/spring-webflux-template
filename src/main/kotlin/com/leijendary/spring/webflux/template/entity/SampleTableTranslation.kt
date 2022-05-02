package com.leijendary.spring.webflux.template.entity

import com.leijendary.spring.webflux.template.core.entity.LocaleEntity
import org.springframework.data.relational.core.mapping.Table

@Table
class SampleTableTranslation : LocaleEntity() {
    lateinit var name: String
    lateinit var description: String
}