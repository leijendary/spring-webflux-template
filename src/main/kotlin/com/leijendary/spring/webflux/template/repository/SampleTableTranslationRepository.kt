package com.leijendary.spring.webflux.template.repository

import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.Companion.readOnlyContext
import com.leijendary.spring.webflux.template.core.model.LocaleModel
import com.leijendary.spring.webflux.template.model.SampleTableTranslation
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.*
import java.util.function.BiFunction

private val selectFields = arrayOf(
    "id",
    "reference_id",
    "name",
    "description",
    "language",
    "ordinal"
).joinToString(", ")

private val mapper = BiFunction<Row, RowMetadata, SampleTableTranslation> { row: Row, _: RowMetadata ->
    val translation = SampleTableTranslation()
    translation.id = row.get("id") as Int
    translation.referenceId = row.get("reference_id") as UUID
    translation.name = row.get("name") as String
    translation.description = row.get("description") as String
    translation.language = row.get("language") as String
    translation.ordinal = row.get("ordinal") as Int

    translation
}

@Repository
class SampleTableTranslationRepository(private val template: R2dbcEntityTemplate) {
    fun save(referenceId: UUID, translations: Set<SampleTableTranslation>): Flow<SampleTableTranslation> {
        val sql = """
            insert into sample_table_translation(reference_id, name, description, language, ordinal)
            values ($1, $2, $3, $4, $5)
        """.trimIndent()

        return template.databaseClient
            .inConnectionMany {
                val statement = it.createStatement(sql)
                    .returnGeneratedValues("id", "reference_id", "name", "description", "language", "ordinal")

                translations.forEach { translation ->
                    statement
                        .bind(0, referenceId)
                        .bind(1, translation.name)
                        .bind(2, translation.description)
                        .bind(3, translation.language)
                        .bind(4, translation.ordinal)
                        .add()
                }

                Flux.from(statement.execute())
                    .flatMap { result -> result.map(mapper) }
            }
            .asFlow()
    }

    fun get(referenceId: UUID): Flow<SampleTableTranslation> {
        val sql = """
            select $selectFields
            from sample_table_translation
            where reference_id = :referenceId
        """.trimIndent()

        return template.databaseClient
            .sql(sql)
            .bind("referenceId", referenceId)
            .map(mapper)
            .all()
            .contextWrite { readOnlyContext(it) }
            .asFlow()
    }

    suspend fun update(
        referenceId: UUID,
        oldTranslations: Set<SampleTableTranslation>,
        newTranslations: Set<SampleTableTranslation>
    ): Flow<SampleTableTranslation> {
        val result = mutableSetOf<SampleTableTranslation>()
        val isolation = LocaleModel.isolate(oldTranslations, newTranslations)

        if (isolation.creates.isNotEmpty()) {
            result += save(referenceId, isolation.creates)
                .toSet(mutableSetOf())
        }

        isolation.updates.forEach {
            result += template
                .update(it)
                .awaitSingle()
        }

        isolation.deletes.forEach {
            template
                .delete(it)
                .awaitSingle()
        }

        return result.asFlow()
    }
}