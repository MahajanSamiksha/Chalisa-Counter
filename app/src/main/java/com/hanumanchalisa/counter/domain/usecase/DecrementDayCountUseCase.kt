package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository

/**
 * Removes one recitation from a given day, to correct an accidental tap.
 *
 * A count can never go below zero — that floor is enforced in the repository's SQL so it holds even
 * if two decrements race each other.
 */
class DecrementDayCountUseCase(
    private val repository: ChalisaCountRepository,
    private val config: SadhanaConfig,
) {
    suspend operator fun invoke(day: Int) {
        require(config.isValidDay(day)) {
            "day $day is outside the practice range ${config.dayRange}"
        }
        repository.decrementDay(day)
    }
}
