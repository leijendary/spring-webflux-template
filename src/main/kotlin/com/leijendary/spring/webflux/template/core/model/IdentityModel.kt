package com.leijendary.spring.webflux.template.core.model

import org.springframework.data.annotation.Id

abstract class IdentityModel : AppModel() {
    @Id
    var id: Long = 0
}