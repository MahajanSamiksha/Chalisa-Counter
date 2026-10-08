package com.hanumanchalisa.counter.domain.importing

import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.model.SadhanaConfig

/**
 * Reads the day counts back out of an exported file — the inverse of
 * [com.hanumanchalisa.counter.domain.export.SadhanaReportFormat].
 */
interface SadhanaReportParser {

    /** MIME types to offer in the system file picker. */
    val acceptedMimeTypes: List<String>

    /** Extracts every day row from [text], checked against [config]. Never throws. */
    fun parse(text: String, config: SadhanaConfig): ReportParseResult
}

/** What a parser made of a file. */
sealed interface ReportParseResult {

    /** A well-formed export; [days] holds each day row in the file, zeros included. */
    data class Parsed(val days: List<DayProgress>) : ReportParseResult

    /** Nothing in the file looks like an export — most likely the wrong file was picked. */
    data object NotAReport : ReportParseResult

    /**
     * It looks like an export but does not add up — a day outside the cycle, a day listed twice, or
     * a total that does not match the rows. Typically a hand-edited or truncated file. Importing part
     * of it could silently restore wrong counts, so it is rejected as a whole.
     */
    data object Damaged : ReportParseResult
}
