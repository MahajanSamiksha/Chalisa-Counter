package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository

/**
 * Records one more recitation for a given day.
 *
 * Guards the cycle boundary so a day outside 1..totalDays can never be written, then delegates the
 * actual write to the repository, which performs it atomically.
 */
class IncrementDayCountUseCase(
    private val repository: ChalisaCountRepository,
    private val config: SadhanaConfig,
) {
    suspend operator fun invoke(day: Int) {
        require(config.isValidDay(day)) {
            "day $day is outside the practice range ${config.dayRange}"
        }
        repository.incrementDay(day)
    }
}
