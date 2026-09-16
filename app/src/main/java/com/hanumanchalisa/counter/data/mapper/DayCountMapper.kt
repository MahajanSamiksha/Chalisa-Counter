package com.hanumanchalisa.counter.data.mapper

import com.hanumanchalisa.counter.data.local.DayCountEntity
import com.hanumanchalisa.counter.domain.model.DayProgress

/**
 * Translates between the storage row and the domain model.
 *
 * Isolating the conversion here is what lets the two types evolve separately: the database schema
 * can gain columns, or the domain model gain computed properties, without either forcing a change on
 * the other.
 */
object DayCountMapper {

    fun toDomain(entity: DayCountEntity): DayProgress =
        DayProgress(day = entity.day, count = entity.count)

    fun toDomainList(entities: List<DayCountEntity>): List<DayProgress> =
        entities.map(::toDomain)

    fun toEntity(model: DayProgress): DayCountEntity =
        DayCountEntity(day = model.day, count = model.count)
}
