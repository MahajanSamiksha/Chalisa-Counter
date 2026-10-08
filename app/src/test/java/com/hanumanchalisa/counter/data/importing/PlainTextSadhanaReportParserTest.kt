package com.hanumanchalisa.counter.data.importing

import com.hanumanchalisa.counter.data.export.PlainTextSadhanaReportFormat
import com.hanumanchalisa.counter.domain.importing.ReportParseResult
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.model.SadhanaProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class PlainTextSadhanaReportParserTest {

    private val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS
    private val parser = PlainTextSadhanaReportParser()

    private fun exportOf(counts: Map<Int, Int>): String = PlainTextSadhanaReportFormat().format(
        SadhanaProgress(
            days = config.dayRange.map { DayProgress(day = it, count = counts[it] ?: 0) },
            config = config,
        ),
        exportedAtMillis = 1773471900000L,
    )

    private fun countsOf(result: ReportParseResult): Map<Int, Int> =
        (result as ReportParseResult.Parsed).days.associate { it.day to it.count }

    @Test
    fun `reads back exactly what export wrote`() {
        val counts = mapOf(1 to 5, 2 to 3, 12 to 11, 40 to 2)

        val parsed = countsOf(parser.parse(exportOf(counts), config))

        assertEquals(40, parsed.size)
        assertEquals(counts, parsed.filterValues { it > 0 })
    }

    /** A file as written by the version already on the Play Store, kept verbatim. */
    @Test
    fun `reads a file from the released version`() {
        val text = """
            Hanuman Chalisa Counter
            =======================

            Exported on : 14 March 2026, 9:05 AM
            Total        : 12 of 100
            Status       : 88 more to reach the target
            Days recited : 3 of 40

            Day    Count
            ------------
            Day 1   5
            Day 2   3
            Day 3   4
            Day 4   0
            ------------
            Total  12
        """.trimIndent()

        assertEquals(mapOf(1 to 5, 2 to 3, 3 to 4, 4 to 0), countsOf(parser.parse(text, config)))
    }

    @Test
    fun `tolerates Windows line endings, a byte order mark and extra spaces`() {
        val text = "\uFEFFDay 1   5\r\n  Day 2     3  \r\nTotal  8\r\n"

        assertEquals(mapOf(1 to 5, 2 to 3), countsOf(parser.parse(text, config)))
    }

    @Test
    fun `an unrelated text file is not an export`() {
        val result = parser.parse("Shopping list\nMilk\nBread\n", config)

        assertEquals(ReportParseResult.NotAReport, result)
    }

    @Test
    fun `an empty file is not an export`() {
        assertEquals(ReportParseResult.NotAReport, parser.parse("", config))
    }

    @Test
    fun `a total that does not match the days is rejected`() {
        val edited = exportOf(mapOf(1 to 5)).replace("Day 1   5", "Day 1   50")

        assertEquals(ReportParseResult.Damaged, parser.parse(edited, config))
    }

    @Test
    fun `a file cut off before the total is rejected`() {
        val truncated = exportOf(mapOf(1 to 5)).substringBefore("Day 30")

        assertEquals(ReportParseResult.Damaged, parser.parse(truncated, config))
    }

    @Test
    fun `a day outside the cycle is rejected`() {
        assertEquals(ReportParseResult.Damaged, parser.parse("Day 41  2\nTotal  2", config))
        assertEquals(ReportParseResult.Damaged, parser.parse("Day 0   2\nTotal  2", config))
    }

    /** Webmail and some editors turn runs of spaces into non-breaking ones. */
    @Test
    fun `tolerates non-breaking and zero width spaces`() {
        val text = "Day\u00A01\u00A0\u00A05\nDay 2\u200B   3\nTotal  8"

        assertEquals(mapOf(1 to 5, 2 to 3), countsOf(parser.parse(text, config)))
    }

    @Test
    fun `an impossibly long day number is reported as damage`() {
        assertEquals(ReportParseResult.Damaged, parser.parse("Day 99999  1\nTotal  1", config))
    }

    @Test
    fun `an impossibly large count is reported as damage`() {
        assertEquals(
            ReportParseResult.Damaged,
            parser.parse("Day 1  12345678\nTotal  12345678", config),
        )
    }

    @Test
    fun `a day listed twice is rejected`() {
        assertEquals(ReportParseResult.Damaged, parser.parse("Day 1  2\nDay 1  3\nTotal  5", config))
    }
}
