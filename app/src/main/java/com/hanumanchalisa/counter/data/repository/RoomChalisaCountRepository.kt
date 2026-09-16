package com.hanumanchalisa.counter.data.repository

import com.hanumanchalisa.counter.data.local.DayCountDao
import com.hanumanchalisa.counter.data.mapper.DayCountMapper
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [ChalisaCountRepository]: the app's durable storage.
 *
 * It depends on the [DayCountDao] abstraction rather than on a concrete database class, and it is
 * itself hidden behind the domain's repository interface — so this class can be replaced (by a fake
 * in tests, or a different storage engine later) without any other file changing.
 *
 * Room runs the suspend DAO calls on its own background executor, so none of these methods block the
 * main thread.
 */
class RoomChalisaCountRepository(
    private val dao: DayCountDao,
) : ChalisaCountRepository {

    override fun observeRecordedDays(): Flow<List<DayProgress>> =
        dao.observeAll().map(DayCountMapper::toDomainList)

    override suspend fun incrementDay(day: Int) {
        dao.increment(day)
    }

    override suspend fun decrementDay(day: Int) {
        dao.decrement(day)
    }

    override suspend fun resetAll() {
        dao.deleteAll()
    }
}
