package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.model.SadhanaProgress
import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Emits the full practice snapshot whenever any count changes.
 *
 * Its single responsibility is expanding the *sparse* records held in storage into the *complete*
 * Day 1 … Day 40 list the UI needs, filling untouched days with zero. Because the list is derived
 * here, storage never has to pre-seed 40 rows and a reset is a simple delete-everything.
 */
class ObserveSadhanaProgressUseCase(
    private val repository: ChalisaCountRepository,
    private val config: SadhanaConfig,
) {
    operator fun invoke(): Flow<SadhanaProgress> =
        repository.observeRecordedDays().map { recordedDays ->
            val countsByDay = recordedDays.associate { it.day to it.count }
            SadhanaProgress(
                days = config.dayRange.map { day ->
                    DayProgress(day = day, count = countsByDay[day] ?: 0)
                },
                config = config,
            )
        }
}
