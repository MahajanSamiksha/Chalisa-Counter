package com.hanumanchalisa.counter.data.importing

import com.hanumanchalisa.counter.domain.importing.ReportParseResult
import com.hanumanchalisa.counter.domain.importing.SadhanaReportParser
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.model.SadhanaConfig

/**
 * Reads files written by [com.hanumanchalisa.counter.data.export.PlainTextSadhanaReportFormat].
 *
 * Only the day table matters: lines such as `Day 7   3` carry the counts and the closing
 * `Total  57` line is a checksum. The heading lines are for people and are ignored, which keeps
 * import working for every file already exported. Matching is tolerant of extra spaces and of the
 * Windows line endings a file picks up after a trip through email or a PC.
 *
 * Validation is all-or-nothing: if any check fails, no counts are returned.
 */
class PlainTextSadhanaReportParser : SadhanaReportParser {

    // Drive and messaging apps often label a .txt file with a generic type, so those are offered too.
    override val acceptedMimeTypes: List<String> =
        listOf("text/plain", "text/*", "application/octet-stream")

    override fun parse(text: String, config: SadhanaConfig): ReportParseResult {
        val lines = text.removePrefix(BYTE_ORDER_MARK)
            .replace(NON_BREAKING_SPACE, ' ')
            .replace(ZERO_WIDTH_SPACE, "")
            .lines()
            .map { it.trim() }

        val dayRows = lines.mapNotNull { DAY_ROW.matchEntire(it) }
        if (dayRows.isEmpty()) return ReportParseResult.NotAReport

        val days = dayRows.map { row ->
            val (day, count) = row.destructured
            val dayNumber = day.toIntOrNull()
            val countValue = count.toIntOrNull()
            // A number too long to be real is treated as damage rather than skipped, so the user is
            // told the file is corrupt instead of being told it is not an export at all.
            if (dayNumber == null || countValue == null ||
                !config.isValidDay(dayNumber) || countValue > MAX_COUNT_PER_DAY
            ) {
                return ReportParseResult.Damaged
            }
            DayProgress(day = dayNumber, count = countValue)
        }
        if (days.map { it.day }.toSet().size != days.size) return ReportParseResult.Damaged

        // The Total row guards against a file that was cut short or edited by hand.
        val total = lines.firstNotNullOfOrNull { TOTAL_ROW.matchEntire(it) }
            ?.groupValues?.get(1)?.toIntOrNull()
            ?: return ReportParseResult.Damaged
        if (total.toLong() != days.sumOf { it.count.toLong() }) return ReportParseResult.Damaged

        return ReportParseResult.Parsed(days.sortedBy { it.day })
    }

    private companion object {
        /** Written as code points, not the characters themselves, so this file stays plain ASCII. */
        const val BYTE_ORDER_MARK = "\uFEFF"
        const val NON_BREAKING_SPACE = '\u00A0'
        const val ZERO_WIDTH_SPACE = "\u200B"

        /** Beyond a day of solid reciting, so a longer number means the file is damaged. */
        const val MAX_COUNT_PER_DAY = 100_000

        /**
         * `Day 7   3` — digits only, so the `Day    Count` heading never matches. The widths are
         * loose on purpose: an impossible number is caught above and reported as damage, rather
         * than slipping past the match and leaving the row silently ignored.
         */
        val DAY_ROW = Regex("""Day\s+(\d{1,9})\s+(\d{1,9})""")

        /** `Total  57` — distinct from the `Total        : 57 of 100` summary line, which has a colon. */
        val TOTAL_ROW = Regex("""Total\s+(\d{1,9})""")
    }
}
