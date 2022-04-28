package com.leijendary.spring.webflux.template.core.model

import org.springframework.data.annotation.Id
import java.util.*

abstract class UUIDModel {
    @Id
    lateinit var id: UUID
}