package com.leijendary.spring.webflux.template.repository

import com.leijendary.spring.webflux.template.core.config.properties.R2dbcBatchProperties
import com.leijendary.spring.webflux.template.core.data.Seek
import com.leijendary.spring.webflux.template.core.data.Seekable
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.Companion.readOnlyContext
import com.leijendary.spring.webflux.template.core.factory.SeekFactory
import com.leijendary.spring.webflux.template.model.SampleTable
import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.r2dbc.core.awaitRowsUpdated
import org.springframework.r2dbc.core.bind
import org.springframework.stereotype.Repository
import reactor.core.scheduler.Schedulers.parallel
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.UUID.randomUUID
import java.util.function.Function

private val selectFields = arrayOf(
    "id",
    "row_id",
    "column_1",
    "column_2",
    "amount",
    "version",
    "created_at",
    "created_by",
    "last_modified_at",
    "last_modified_by"
).joinToString(", ")

private val mapper = Function<Row, SampleTable> {
    val sampleTable = SampleTable()
    sampleTable.id = it.get("id") as UUID
    sampleTable.rowId = it.get("row_id") as Long
    sampleTable.column1 = it.get("column_1") as String
    sampleTable.column2 = it.get("column_2") as Long
    sampleTable.amount = it.get("amount") as BigDecimal
    sampleTable.version = it.get("version") as Int
    sampleTable.createdAt = it.get("created_at") as LocalDateTime
    sampleTable.createdBy = it.get("created_by") as String
    sampleTable.lastModifiedAt = it.get("last_modified_at") as LocalDateTime
    sampleTable.lastModifiedBy = it.get("last_modified_by") as String

    sampleTable
}

@Repository
class SampleTableRepository(
    private val auditorAware: ReactiveAuditorAware<String>,
    private val dateTimeProvider: DateTimeProvider,
    private val r2dbcBatchProperties: R2dbcBatchProperties,
    private val template: R2dbcEntityTemplate
) {
    suspend fun seek(query: String, seekable: Seekable): Seek<SampleTable> {
        val nextToken = seekable.nextToken
        val seekFilter = nextToken?.let { "and (created_at, row_id) < (:createdAt, :rowId)" } ?: ""
        val limit = seekable.limit
        val sql = """
            select $selectFields
            from sample_table
            where deleted_at is null
              and (
                column_1 ilike :query
                or exists (
                    select id
                    from sample_table_translation
                    where sample_table_translation.reference_id = sample_table.id
                    and (name ilike :query or description ilike :query)
                )
              )
              $seekFilter
              order by created_at desc, row_id desc
              limit :limit
        """.trimIndent()
        var spec = template.databaseClient
            .sql(sql)
            .bind("query", "%$query%")
            .bind("limit", limit + 1)

        nextToken?.let {
            val seekToken = SeekFactory.decode(it)
            val createdAt = LocalDateTime.parse(seekToken.fields["createdAt"] as String)
            val rowId = seekToken.fields["rowId"]

            spec = spec
                .bind("createdAt", createdAt)
                .bind("rowId", rowId)
        }

        return spec
            .map(mapper)
            .all()
            .collectList()
            .map {
                SeekFactory.create(it, seekable) { value ->
                    mutableMapOf(
                        "createdAt" to value.createdAt.toString(),
                        "rowId" to value.rowId
                    )
                }
            }
            .contextWrite { readOnlyContext(it) }
            .awaitSingle()
    }

    suspend fun save(sampleTable: SampleTable): SampleTable {
        sampleTable.id = randomUUID()

        return template
            .insert(sampleTable)
            .awaitSingle()
    }

    suspend fun get(id: UUID): SampleTable? {
        val sql = """
            select $selectFields
            from sample_table
            where id = :id
              and deleted_at is null
        """.trimIndent()

        return template.databaseClient
            .sql(sql)
            .bind("id", id)
            .map(mapper)
            .first()
            .contextWrite { readOnlyContext(it) }
            .awaitSingleOrNull()
    }

    suspend fun update(sampleTable: SampleTable): SampleTable {
        return template
            .update(sampleTable)
            .awaitSingle()
    }

    suspend fun delete(id: UUID): Int {
        val sql = """
            update sample_table 
            set deleted_at = :deletedAt, 
                deleted_by = :deletedBy 
            where id = :id 
              and deleted_at is null
        """.trimIndent()

        return template.databaseClient
            .sql(sql)
            .bind("deletedAt", dateTimeProvider.now.get())
            .bind("deletedBy", auditorAware.currentAuditor.awaitSingle())
            .bind("id", id)
            .fetch()
            .awaitRowsUpdated()
    }

    suspend fun all(): Flow<List<SampleTable>> {
        val criteria = where("deleted_at").isNull
        val query = query(criteria)

        return template
            .select(SampleTable::class.java)
            .matching(query)
            .all()
            .buffer(r2dbcBatchProperties.size)
            .onBackpressureBuffer()
            .parallel()
            .runOn(parallel())
            .asFlow()
    }
}