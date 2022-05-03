package com.leijendary.spring.webflux.template.core.extension

import java.net.URI

fun URI.fullPath(): String {
    return rawQuery?.let { "$path?$it" } ?: path
}