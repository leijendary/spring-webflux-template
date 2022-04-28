package com.leijendary.spring.webflux.template.model

import com.leijendary.spring.webflux.template.core.model.SeekModel
import com.leijendary.spring.webflux.template.core.model.SoftDeleteModel
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table
class SampleTable : SeekModel(), SoftDeleteModel {
    @Column(value = "column_1")
    lateinit var column1: String

    @Column(value = "column_2")
    var column2: Long = 0

    lateinit var amount: BigDecimal

    @Version
    var version = 0

    @CreatedBy
    lateinit var createdBy: String

    @LastModifiedDate
    lateinit var lastModifiedAt: LocalDateTime

    @LastModifiedBy
    lateinit var lastModifiedBy: String

    override var deletedAt: LocalDateTime? = null
    override var deletedBy: String? = null
}