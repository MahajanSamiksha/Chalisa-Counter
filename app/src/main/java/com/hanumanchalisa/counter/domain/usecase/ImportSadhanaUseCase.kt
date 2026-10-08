package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.domain.importing.DayReplacement
import com.hanumanchalisa.counter.domain.importing.ImportPreview
import com.hanumanchalisa.counter.domain.importing.ReportParseResult
import com.hanumanchalisa.counter.domain.importing.SadhanaReportParser
import com.hanumanchalisa.counter.domain.importing.TextFileReader
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.model.SadhanaProgress
import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository

/**
 * Brings counts from an exported file into the app — typically when moving to a new phone.
 *
 * Split into two steps so nothing is written until the user has seen what will change:
 * [preview] reads and checks the file and describes the outcome; [apply] writes it.
 * See [ImportPreview] for the rule that decides each day's count.
 */
class ImportSadhanaUseCase(
    private val fileReader: TextFileReader,
    private val reportParser: SadhanaReportParser,
    private val repository: ChalisaCountRepository,
    private val config: SadhanaConfig,
) {
    /** MIME types to offer in the system file picker. */
    val acceptedMimeTypes: List<String> get() = reportParser.acceptedMimeTypes

    /**
     * Reads the file at [location] and works out what importing it into [current] would do.
     * Writes nothing. Throws if the file cannot be read.
     */
    suspend fun preview(location: String, current: SadhanaProgress): ImportCheck {
        val text = fileReader.read(location, MAX_FILE_BYTES) ?: return ImportCheck.NotAnExport

        val fileDays = when (val result = reportParser.parse(text, config)) {
            is ReportParseResult.Parsed -> result.days.filter { it.hasRecitations }
            ReportParseResult.NotAReport -> return ImportCheck.NotAnExport
            ReportParseResult.Damaged -> return ImportCheck.Damaged
        }
        if (fileDays.isEmpty()) return ImportCheck.NothingToImport

        val currentByDay = current.days.associate { it.day to it.count }
        val fileByDay = fileDays.associate { it.day to it.count }

        return ImportCheck.Ready(
            ImportPreview(
                restoredDays = fileDays,
                keptDays = current.days.filter { it.hasRecitations && it.day !in fileByDay },
                replacedDays = fileDays.mapNotNull { fileDay ->
                    val currentCount = currentByDay[fileDay.day] ?: 0
                    if (currentCount > 0 && currentCount != fileDay.count) {
                        DayReplacement(fileDay.day, currentCount, fileDay.count)
                    } else {
                        null
                    }
                },
                totalAfterImport = current.days.sumOf { fileByDay[it.day] ?: it.count },
                targetCount = config.targetCount,
                changesNothing = fileDays.all { currentByDay[it.day] == it.count },
            ),
        )
    }

    /** Writes the file's counts for the days in [preview]; every other day is left untouched. */
    suspend fun apply(preview: ImportPreview) {
        repository.restoreDays(preview.restoredDays)
    }

    private companion object {
        /** A full 40-day export is about 1 KB; anything this large is certainly not one. */
        const val MAX_FILE_BYTES = 64 * 1024
    }
}

/** The outcome of checking a file before import. */
sealed interface ImportCheck {

    /** The file is valid; [preview] describes what importing it will change. */
    data class Ready(val preview: ImportPreview) : ImportCheck

    /** The file is not a Chalisa Counter export. */
    data object NotAnExport : ImportCheck

    /** The file looks like an export but is edited or incomplete. */
    data object Damaged : ImportCheck

    /** A valid export, but every day in it is zero. */
    data object NothingToImport : ImportCheck
}
