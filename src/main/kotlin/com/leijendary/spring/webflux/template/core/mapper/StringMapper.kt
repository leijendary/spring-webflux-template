package com.leijendary.spring.webflux.template.core.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers.getMapper
import java.util.*

@Mapper
interface StringMapper {
    companion object {
        val INSTANCE: StringMapper = getMapper(StringMapper::class.java)
    }

    fun map(uuid: UUID?): String? = uuid?.toString()
}