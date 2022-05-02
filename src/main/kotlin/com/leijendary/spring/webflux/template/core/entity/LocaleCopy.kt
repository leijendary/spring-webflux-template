package com.leijendary.spring.webflux.template.core.entity

abstract class LocaleCopy : AppEntity() {
    var language: String? = null
    var ordinal: Int = 0
}