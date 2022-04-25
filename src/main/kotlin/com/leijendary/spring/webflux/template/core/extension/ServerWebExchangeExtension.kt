package com.leijendary.spring.webflux.template.core.extension

import org.springframework.context.i18n.LocaleContextHolder.getTimeZone
import org.springframework.web.server.ServerWebExchange
import java.util.*
import java.util.Locale.getDefault

fun ServerWebExchange.locale(): Locale {
    return this.localeContext.locale ?: getDefault()
}

fun ServerWebExchange.language(): String {
    return this.locale().language
}

fun ServerWebExchange.timeZone(): TimeZone {
    return getTimeZone(this.localeContext)
}