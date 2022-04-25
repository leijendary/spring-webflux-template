package com.leijendary.spring.webflux.template.core.model

abstract class LocaleCopy : AppModel() {
    var language: String? = null
    var ordinal: Int = 0
}