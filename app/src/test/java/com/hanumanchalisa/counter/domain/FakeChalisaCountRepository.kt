package com.hanumanchalisa.counter.domain

import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-in for the real repository, used to test the domain and presentation layers on the
 * JVM without a database or a device.
 *
 * This is Liskov Substitution in practice: because it honours the same contract as the Room
 * implementation — including storing only touched days and flooring counts at zero — the code under
 * test cannot tell the difference.
 */
class FakeChalisaCountRepository(
    initialCounts: Map<Int, Int> = emptyMap(),
) : ChalisaCountRepository {

    private val counts = MutableStateFlow(initialCounts)

    var resetCallCount: Int = 0
        private set

    var restoreCallCount: Int = 0
        private set

    override fun observeRecordedDays(): Flow<List<DayProgress>> =
        counts.map { snapshot ->
            snapshot.entries
                .sortedBy { it.key }
                .map { DayProgress(day = it.key, count = it.value) }
        }

    override suspend fun incrementDay(day: Int) {
        counts.value = counts.value.toMutableMap().apply {
            this[day] = (this[day] ?: 0) + 1
        }
    }

    override suspend fun decrementDay(day: Int) {
        val current = counts.value[day] ?: return
        if (current <= 0) return
        counts.value = counts.value.toMutableMap().apply {
            this[day] = current - 1
        }
    }

    override suspend fun restoreDays(days: List<DayProgress>) {
        restoreCallCount++
        counts.value = counts.value + days.associate { it.day to it.count }
    }

    override suspend fun resetAll() {
        resetCallCount++
        counts.value = emptyMap()
    }

    /** Current count for [day], for assertions. */
    fun countFor(day: Int): Int = counts.value[day] ?: 0
}
