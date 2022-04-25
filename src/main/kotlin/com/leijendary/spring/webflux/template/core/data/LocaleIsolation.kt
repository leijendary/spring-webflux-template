package com.leijendary.spring.webflux.template.core.data

import com.leijendary.spring.webflux.template.core.model.LocaleModel

data class LocaleIsolation<T : LocaleModel>(val creates: Set<T>, val updates: Set<T>, val deletes: Set<T>)