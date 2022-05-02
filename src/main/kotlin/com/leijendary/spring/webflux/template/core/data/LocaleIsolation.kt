package com.leijendary.spring.webflux.template.core.data

import com.leijendary.spring.webflux.template.core.entity.LocaleEntity

data class LocaleIsolation<T : LocaleEntity>(val creates: Set<T>, val updates: Set<T>, val deletes: Set<T>)