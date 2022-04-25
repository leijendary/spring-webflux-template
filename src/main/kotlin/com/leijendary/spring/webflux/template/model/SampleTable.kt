package com.leijendary.spring.webflux.template.model

import com.leijendary.spring.webflux.template.core.model.SoftDeleteModel
import com.leijendary.spring.webflux.template.core.model.UUIDModel
import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table
class SampleTable : UUIDModel(), SoftDeleteModel {
    @Column(value = "column_1")
    lateinit var column1: String

    @Column(value = "column_2")
    var column2: Long = 0

    lateinit var amount: BigDecimal

    @Version
    var version = 0

    @CreatedDate
    lateinit var createdAt: LocalDateTime

    @CreatedBy
    lateinit var createdBy: String

    @LastModifiedDate
    lateinit var lastModifiedAt: LocalDateTime

    @LastModifiedBy
    lateinit var lastModifiedBy: String

    override var deletedAt: LocalDateTime? = null
    override var deletedBy: String? = null
}