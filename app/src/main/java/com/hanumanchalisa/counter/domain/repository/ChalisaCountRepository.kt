package com.hanumanchalisa.counter.domain.repository

import com.hanumanchalisa.counter.domain.model.DayProgress
import kotlinx.coroutines.flow.Flow

/**
 * Storage contract for recitation counts, owned by the domain layer.
 *
 * This is the Dependency Inversion seam: the domain declares what it needs and the data layer
 * supplies an implementation. Nothing here mentions SQLite, Room or Android, so the counts could be
 * moved to a file, a DataStore or a server without touching a single use case.
 *
 * Implementations must persist to durable storage so counts survive both app termination and a
 * device restart.
 */
interface ChalisaCountRepository {

    /**
     * Days that have a recorded count, emitting again whenever any count changes.
     *
     * Days never incremented may be absent from the emitted list; callers are responsible for
     * treating an absent day as zero. This keeps writes to only the days actually used.
     */
    fun observeRecordedDays(): Flow<List<DayProgress>>

    /** Atomically adds one to the count for [day], creating the record if it does not yet exist. */
    suspend fun incrementDay(day: Int)

    /** Atomically subtracts one from the count for [day], never going below zero. */
    suspend fun decrementDay(day: Int)

    /** Clears every recorded count, returning the whole cycle to zero. */
    suspend fun resetAll()
}
