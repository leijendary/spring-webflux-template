package com.leijendary.spring.webflux.template.core.entity

import java.time.LocalDateTime

interface SoftDeleteEntity {
    var deletedAt: LocalDateTime?
    var deletedBy: String?
}