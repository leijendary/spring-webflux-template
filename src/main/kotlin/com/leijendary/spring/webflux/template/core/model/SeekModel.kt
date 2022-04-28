package com.leijendary.spring.webflux.template.core.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.ReadOnlyProperty
import java.time.LocalDateTime

abstract class SeekModel : UUIDModel() {
    @CreatedDate
    lateinit var createdAt: LocalDateTime

    @ReadOnlyProperty
    var rowId: Long = 0
}