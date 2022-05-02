package com.leijendary.spring.webflux.template.core.entity

import org.springframework.data.annotation.Id

abstract class IdentityEntity : AppEntity() {
    @Id
    var id: Long = 0
}