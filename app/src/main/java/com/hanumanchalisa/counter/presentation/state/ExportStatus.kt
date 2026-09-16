package com.hanumanchalisa.counter.presentation.state

/**
 * The outcome of the most recent export, waiting to be shown to the user.
 *
 * A sealed hierarchy rather than a nullable string pair: the compiler then guarantees the UI handles
 * every case, and adding a future outcome cannot silently fall through. The ViewModel deals in these
 * framework-free values and the UI maps them to strings, so no `Context` is needed to build a message.
 */
sealed interface ExportStatus {

    /** The file was written; [fileName] is what it was saved as, for the confirmation message. */
    data class Succeeded(val fileName: String) : ExportStatus

    /** The write failed — typically the chosen location became unavailable. */
    data object Failed : ExportStatus
}
