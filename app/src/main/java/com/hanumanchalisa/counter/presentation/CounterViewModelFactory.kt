package com.hanumanchalisa.counter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hanumanchalisa.counter.di.AppContainer

/**
 * Supplies [CounterViewModel] with its use cases.
 *
 * A factory is needed because [CounterViewModel] takes constructor parameters, and the framework can
 * only instantiate no-argument ViewModels on its own. Keeping the construction here preserves the
 * ViewModel's testability: it never reaches out for its own dependencies.
 */
class CounterViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CounterViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return CounterViewModel(
            observeSadhanaProgress = container.observeSadhanaProgress,
            incrementDayCount = container.incrementDayCount,
            decrementDayCount = container.decrementDayCount,
            resetSadhana = container.resetSadhana,
            exportSadhana = container.exportSadhana,
            importSadhana = container.importSadhana,
        ) as T
    }
}
