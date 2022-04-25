package com.leijendary.spring.webflux.template.core.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.ReadOnlyProperty
import java.util.*

abstract class UUIDModel {
    @Id
    lateinit var id: UUID

    @ReadOnlyProperty
    var rowId: Long = 0
}