package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository

/**
 * Clears every count so a fresh 40-day cycle can begin.
 *
 * Destructive and irreversible, so the UI must confirm with the user before invoking this.
 */
class ResetSadhanaUseCase(
    private val repository: ChalisaCountRepository,
) {
    suspend operator fun invoke() {
        repository.resetAll()
    }
}
