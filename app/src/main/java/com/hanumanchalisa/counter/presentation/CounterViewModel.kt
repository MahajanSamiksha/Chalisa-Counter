package com.hanumanchalisa.counter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanumanchalisa.counter.domain.usecase.DecrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ExportSadhanaUseCase
import com.hanumanchalisa.counter.domain.usecase.ImportCheck
import com.hanumanchalisa.counter.domain.usecase.ImportSadhanaUseCase
import com.hanumanchalisa.counter.domain.usecase.IncrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ObserveSadhanaProgressUseCase
import com.hanumanchalisa.counter.domain.usecase.ResetSadhanaUseCase
import com.hanumanchalisa.counter.presentation.state.CounterUiState
import com.hanumanchalisa.counter.presentation.state.ExportStatus
import com.hanumanchalisa.counter.presentation.state.ImportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the counter screen's state and turns user intents into use case calls.
 *
 * It depends only on the use cases it actually needs — not on the repository or the database —
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
    private val importSadhana: ImportSadhanaUseCase,
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

    /** MIME types the system file picker should offer for import. */
    fun importMimeTypes(): Array<String> = importSadhana.acceptedMimeTypes.toTypedArray()

    /**
     * Reads and checks the file the user picked, then either asks for confirmation or explains why
     * it cannot be imported. Nothing is written at this stage.
     */
    fun onImportFileChosen(location: String) {
        val progress = _uiState.value.progress ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importStatus = null) }
            val check = try {
                importSadhana.preview(location = location, current = progress)
            } catch (e: Exception) {
                null
            }
            _uiState.update { state ->
                // Matching on every ImportCheck rather than falling through to an else: a case
                // added later then has to be given a message here instead of being mistaken for one.
                val checked = when (check) {
                    null -> state.copy(importStatus = ImportStatus.Unreadable)
                    is ImportCheck.Ready ->
                        if (check.preview.changesNothing) {
                            state.copy(importStatus = ImportStatus.AlreadyUpToDate)
                        } else {
                            state.copy(importPreview = check.preview)
                        }
                    ImportCheck.NotAnExport -> state.copy(importStatus = ImportStatus.NotAnExport)
                    ImportCheck.Damaged -> state.copy(importStatus = ImportStatus.Damaged)
                    ImportCheck.NothingToImport ->
                        state.copy(importStatus = ImportStatus.NothingToImport)
                }
                checked.copy(isImporting = false)
            }
        }
    }

    /** Applies the import the user has just reviewed in the confirmation dialog. */
    fun onImportConfirmed() {
        val preview = _uiState.value.importPreview ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(importPreview = null, isImporting = true) }
            val status = try {
                importSadhana.apply(preview)
                ImportStatus.Succeeded(
                    totalCount = preview.totalAfterImport,
                    targetCount = preview.targetCount,
                )
            } catch (e: Exception) {
                ImportStatus.Failed
            }
            _uiState.update { it.copy(isImporting = false, importStatus = status) }
        }
    }

    /** The user backed out of the confirmation dialog; nothing is written. */
    fun onImportDismissed() {
        _uiState.update { it.copy(importPreview = null) }
    }

    /** Clears the import message once it has been shown, so it is not shown twice. */
    fun onImportStatusShown() {
        _uiState.update { it.copy(importStatus = null) }
    }
}
