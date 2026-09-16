package com.hanumanchalisa.counter.presentation.state

import com.hanumanchalisa.counter.domain.model.SadhanaProgress

/**
 * Everything the counter screen needs to render, in one immutable value.
 *
 * Holding the whole screen state in a single object means the UI can never show a half-updated
 * combination — for example a new total next to stale day counts.
 *
 * @property progress the current practice snapshot, or null until the first database read arrives.
 * @property isResetDialogVisible whether the reset confirmation dialog is showing.
 * @property isExporting whether a file write is in flight, used to disable the export button.
 * @property exportStatus the result of the last export awaiting display, or null when there is none.
 */
data class CounterUiState(
    val progress: SadhanaProgress? = null,
    val isResetDialogVisible: Boolean = false,
    val isExporting: Boolean = false,
    val exportStatus: ExportStatus? = null,
) {
    /** True until the first value has been loaded from storage. */
    val isLoading: Boolean get() = progress == null

    /**
     * True when there is something worth exporting.
     *
     * Exporting an all-zero file is not useful, so the button stays disabled until at least one
     * recitation is recorded — and while a write is already running.
     */
    val canExport: Boolean get() = !isExporting && (progress?.totalCount ?: 0) > 0
}
