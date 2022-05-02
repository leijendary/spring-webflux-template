package com.leijendary.spring.webflux.template.core.entity

import org.springframework.data.annotation.Id
import java.util.*

abstract class UUIDEntity {
    @Id
    lateinit var id: UUID
}