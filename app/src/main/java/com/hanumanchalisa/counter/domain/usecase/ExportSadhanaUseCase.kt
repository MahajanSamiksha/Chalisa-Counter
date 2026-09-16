package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.domain.export.SadhanaReportFormat
import com.hanumanchalisa.counter.domain.export.TextFileWriter
import com.hanumanchalisa.counter.domain.export.TimeProvider
import com.hanumanchalisa.counter.domain.model.SadhanaProgress

/**
 * Writes the current progress to a text file at a location the user picked.
 *
 * One job: take a snapshot, render it, hand it to the writer. It neither reads the database (the
 * caller already holds the snapshot on screen) nor knows what a document URI is, so the same use
 * case would serve a CSV export or a share sheet unchanged.
 */
class ExportSadhanaUseCase(
    private val reportFormat: SadhanaReportFormat,
    private val fileWriter: TextFileWriter,
    private val timeProvider: TimeProvider,
) {
    /**
     * Renders [progress] and writes it to [location]. Throws if the write fails.
     *
     * @return the name the file was saved as, falling back to the suggested name when the
     *   destination does not report one.
     */
    suspend operator fun invoke(progress: SadhanaProgress, location: String): String {
        val exportedAt = timeProvider.currentTimeMillis()
        val text = reportFormat.format(progress, exportedAt)
        return fileWriter.write(location, text) ?: reportFormat.fileName(exportedAt)
    }

    /** The file name to suggest in the system's save dialog. */
    fun suggestedFileName(): String = reportFormat.fileName(timeProvider.currentTimeMillis())

    /** The MIME type to create the document with. */
    val mimeType: String get() = reportFormat.mimeType
}
