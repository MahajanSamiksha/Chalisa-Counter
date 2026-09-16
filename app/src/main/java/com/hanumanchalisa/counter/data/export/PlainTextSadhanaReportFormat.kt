package com.hanumanchalisa.counter.data.export

import com.hanumanchalisa.counter.domain.export.SadhanaReportFormat
import com.hanumanchalisa.counter.domain.model.SadhanaProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders progress as a human-readable plain-text report.
 *
 * Plain text is chosen over CSV or JSON because the point of the export is that *you* can open it
 * anywhere — a phone notes app, WhatsApp, email — and still read it years later, with no tool
 * required. Every day of the cycle is listed, including zeros, so the file is a complete record
 * rather than something that has to be cross-referenced with the app.
 *
 * Dates use a fixed [Locale.US] pattern so the file name sorts chronologically and never contains
 * characters a file system would reject.
 */
class PlainTextSadhanaReportFormat : SadhanaReportFormat {

    override val mimeType: String = MIME_TYPE

    override fun fileName(exportedAtMillis: Long): String {
        val stamp = SimpleDateFormat(FILE_NAME_DATE_PATTERN, Locale.US).format(Date(exportedAtMillis))
        return "hanuman-chalisa-count-$stamp.txt"
    }

    override fun format(progress: SadhanaProgress, exportedAtMillis: Long): String {
        val exportedOn = SimpleDateFormat(READABLE_DATE_PATTERN, Locale.US).format(Date(exportedAtMillis))

        return buildString {
            appendLine("Hanuman Chalisa Counter")
            appendLine("=======================")
            appendLine()
            appendLine("Exported on : $exportedOn")
            appendLine("Total        : ${progress.totalCount} of ${progress.targetCount}")
            if (progress.isTargetReached) {
                appendLine("Status       : Target reached. Jai Hanuman!")
            } else {
                appendLine("Status       : ${progress.remainingCount} more to reach the target")
            }
            appendLine("Days recited : ${progress.days.count { it.hasRecitations }} of ${progress.days.size}")
            appendLine()
            appendLine("Day    Count")
            appendLine("------------")
            progress.days.forEach { day ->
                appendLine("Day ${day.day.toString().padEnd(2)}  ${day.count}")
            }
            appendLine("------------")
            appendLine("Total  ${progress.totalCount}")
        }
    }

    private companion object {
        const val MIME_TYPE = "text/plain"
        const val FILE_NAME_DATE_PATTERN = "yyyy-MM-dd-HHmm"
        const val READABLE_DATE_PATTERN = "d MMMM yyyy, h:mm a"
    }
}
