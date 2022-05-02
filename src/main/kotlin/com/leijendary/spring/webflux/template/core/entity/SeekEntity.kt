package com.leijendary.spring.webflux.template.core.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.ReadOnlyProperty
import java.time.LocalDateTime

abstract class SeekEntity : UUIDEntity() {
    @CreatedDate
    lateinit var createdAt: LocalDateTime

    @ReadOnlyProperty
    var rowId: Long = 0
}