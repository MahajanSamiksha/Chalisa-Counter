package com.hanumanchalisa.counter.domain.export

import com.hanumanchalisa.counter.domain.model.SadhanaProgress

/**
 * Turns a [SadhanaProgress] snapshot into an exportable file: its name and its contents.
 *
 * Naming and rendering are the two halves of one responsibility — "what an export looks like" — so
 * they live together here. Keeping this an interface means a different shape of export (CSV, JSON)
 * can be added as a new implementation without touching the export use case or the UI (Open/Closed).
 */
interface SadhanaReportFormat {

    /** Suggested file name for an export taken at [exportedAtMillis], including its extension. */
    fun fileName(exportedAtMillis: Long): String

    /** The MIME type the file should be created with, e.g. `text/plain`. */
    val mimeType: String

    /** The full text of the export. */
    fun format(progress: SadhanaProgress, exportedAtMillis: Long): String
}
