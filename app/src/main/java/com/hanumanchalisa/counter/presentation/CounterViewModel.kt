package com.hanumanchalisa.counter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanumanchalisa.counter.domain.usecase.DecrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ExportSadhanaUseCase
import com.hanumanchalisa.counter.domain.usecase.IncrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ObserveSadhanaProgressUseCase
import com.hanumanchalisa.counter.domain.usecase.ResetSadhanaUseCase
import com.hanumanchalisa.counter.presentation.state.CounterUiState
import com.hanumanchalisa.counter.presentation.state.ExportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the counter screen's state and turns user intents into use case calls.
 *
 * It depends only on the four use cases it actually needs — not on the repository or the database —
 * so it can be unit tested on the JVM with a fake repository and no Android device.
 *
 * The mutable state is private and exposed as a read-only [StateFlow]; the UI can observe it but only
 * the functions below can change it. Surviving configuration changes (rotation) comes free from
 * [ViewModel].
 */
class CounterViewModel(
    observeSadhanaProgress: ObserveSadhanaProgressUseCase,
    private val incrementDayCount: IncrementDayCountUseCase,
    private val decrementDayCount: DecrementDayCountUseCase,
    private val resetSadhana: ResetSadhanaUseCase,
    private val exportSadhana: ExportSadhanaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    init {
        // Collect for the ViewModel's lifetime: any change written to the database — including a
        // reset — flows straight back into the UI, so state and storage cannot diverge.
        viewModelScope.launch {
            observeSadhanaProgress().collect { progress ->
                _uiState.update { it.copy(progress = progress) }
            }
        }
    }

    fun onIncrementDay(day: Int) {
        viewModelScope.launch { incrementDayCount(day) }
    }

    fun onDecrementDay(day: Int) {
        viewModelScope.launch { decrementDayCount(day) }
    }

    fun onResetRequested() {
        _uiState.update { it.copy(isResetDialogVisible = true) }
    }

    fun onResetDismissed() {
        _uiState.update { it.copy(isResetDialogVisible = false) }
    }

    fun onResetConfirmed() {
        viewModelScope.launch {
            resetSadhana()
            _uiState.update { it.copy(isResetDialogVisible = false) }
        }
    }

    /** The file name to pre-fill in the system save dialog. */
    fun suggestedExportFileName(): String = exportSadhana.suggestedFileName()

    /** The MIME type the save dialog should create the document with. */
    fun exportMimeType(): String = exportSadhana.mimeType

    /**
     * Writes the current counts to the document the user chose.
     *
     * A failed write must not crash the app or lose the counts — the database is untouched either
     * way — so any error becomes a message on screen instead of an exception.
     */
    fun onExportLocationChosen(location: String) {
        val progress = _uiState.value.progress ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportStatus = null) }
            val status = try {
                val savedAs = exportSadhana(progress = progress, location = location)
                ExportStatus.Succeeded(fileName = savedAs)
            } catch (e: Exception) {
                ExportStatus.Failed
            }
            _uiState.update { it.copy(isExporting = false, exportStatus = status) }
        }
    }

    /** Clears the export message once it has been shown, so it is not shown twice. */
    fun onExportStatusShown() {
        _uiState.update { it.copy(exportStatus = null) }
    }
}
