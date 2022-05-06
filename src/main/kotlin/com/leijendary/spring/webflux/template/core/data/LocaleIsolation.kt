package com.leijendary.spring.webflux.template.core.data

import com.leijendary.spring.webflux.template.core.entity.LocaleEntity

data class LocaleIsolation<T : LocaleEntity>(val creates: List<T>, val updates: List<T>, val deletes: List<T>)