package com.leijendary.spring.webflux.template.repository

import com.leijendary.spring.webflux.template.core.repository.SoftDeleteRepository
import com.leijendary.spring.webflux.template.model.SampleTable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

interface SampleTableRepository : ReactiveCrudRepository<SampleTable, UUID>, SoftDeleteRepository<SampleTable> {
    @Query(
        """
            select 
                id,
                row_id,
                column_1,
                column_2,
                amount,
                version,
                created_at,
                created_by,
                last_modified_at,
                last_modified_by
            from sample_table
            where deleted_at is null
              and (
                column_1 ilike concat('%', :query, '%')
                or exists (
                    select id
                    from sample_table_translation
                    where sample_table_translation.reference_id = sample_table.id
                    and (
                      name ilike concat('%', :query, '%') 
                      or description ilike concat('%', :query, '%')
                    )
                )
              )
              order by created_at desc, row_id desc
              limit :limit + 1
        """
    )
    fun query(query: String, limit: Int): Flux<SampleTable>

    @Query(
        """
            select 
                id,
                row_id,
                column_1,
                column_2,
                amount,
                version,
                created_at,
                created_by,
                last_modified_at,
                last_modified_by
            from sample_table
            where deleted_at is null
              and (
                column_1 ilike concat('%', :query, '%')
                or exists (
                    select id
                    from sample_table_translation
                    where sample_table_translation.reference_id = sample_table.id
                    and (
                      name ilike concat('%', :query, '%') 
                      or description ilike concat('%', :query, '%')
                    )
                )
              )
              and (created_at, row_id) < (:createdAt, :rowId)
              order by created_at desc, row_id desc
              limit :limit + 1
        """
    )
    fun seek(query: String, createdAt: LocalDateTime, rowId: Long, limit: Int): Flux<SampleTable>

    @Query(
        """
            select 
                id,
                row_id,
                column_1,
                column_2,
                amount,
                version,
                created_at,
                created_by,
                last_modified_at,
                last_modified_by
            from sample_table
            where id = :id
              and deleted_at is null
        """
    )
    fun get(id: UUID): Mono<SampleTable>

    fun findAllByDeletedAtIsNull(): Flux<SampleTable>
}