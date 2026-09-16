package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.data.export.PlainTextSadhanaReportFormat
import com.hanumanchalisa.counter.domain.export.FakeTextFileWriter
import com.hanumanchalisa.counter.domain.export.FixedTimeProvider
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.model.SadhanaProgress
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ExportSadhanaUseCaseTest {

    private val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS

    // 14 March 2026, 09:05 local time — fixed so the stamped output never varies by run.
    private val fixedTime = 1773471900000L

    private fun progressWith(counts: Map<Int, Int>) = SadhanaProgress(
        days = config.dayRange.map { DayProgress(day = it, count = counts[it] ?: 0) },
        config = config,
    )

    private fun useCase(writer: FakeTextFileWriter) = ExportSadhanaUseCase(
        reportFormat = PlainTextSadhanaReportFormat(),
        fileWriter = writer,
        timeProvider = FixedTimeProvider(fixedTime),
    )

    @Test
    fun `writes the report to the chosen location`() = runTest {
        val writer = FakeTextFileWriter()

        useCase(writer)(progressWith(mapOf(1 to 3, 2 to 2)), "content://docs/export.txt")

        assertEquals(1, writer.writeCallCount)
        assertTrue(writer.written.containsKey("content://docs/export.txt"))
    }

    @Test
    fun `report contains every day and the correct total`() = runTest {
        val writer = FakeTextFileWriter()

        useCase(writer)(progressWith(mapOf(1 to 5, 2 to 3, 40 to 2)), "loc")
        val text = writer.written.getValue("loc")

        assertTrue("Day 1 count missing", text.contains("Day 1   5"))
        assertTrue("Day 2 count missing", text.contains("Day 2   3"))
        assertTrue("Day 40 count missing", text.contains("Day 40  2"))
        // Untapped days must still appear, so the file is a complete record.
        assertTrue("Day 20 zero row missing", text.contains("Day 20  0"))
        assertTrue("total missing", text.contains("Total        : 10 of 100"))
        assertTrue("remaining missing", text.contains("90 more to reach the target"))
        assertEquals(40, Regex("""^Day \d+""", RegexOption.MULTILINE).findAll(text).count())
    }

    @Test
    fun `report notes when the target has been reached`() = runTest {
        val writer = FakeTextFileWriter()

        useCase(writer)(progressWith(mapOf(1 to 100)), "loc")

        assertTrue(writer.written.getValue("loc").contains("Target reached"))
    }

    @Test
    fun `returns the name the destination reports`() = runTest {
        val writer = FakeTextFileWriter(reportedName = "my-renamed-file.txt")

        val savedAs = useCase(writer)(progressWith(mapOf(1 to 1)), "loc")

        // The user may rename the file in the save dialog, so the destination's name wins.
        assertEquals("my-renamed-file.txt", savedAs)
    }

    @Test
    fun `falls back to the suggested name when none is reported`() = runTest {
        val writer = FakeTextFileWriter(reportedName = null)

        val savedAs = useCase(writer)(progressWith(mapOf(1 to 1)), "loc")

        assertTrue("unexpected name: $savedAs", savedAs.startsWith("hanuman-chalisa-count-"))
        assertTrue(savedAs.endsWith(".txt"))
    }

    @Test
    fun `suggested file name is stamped and safe for a file system`() {
        val name = useCase(FakeTextFileWriter()).suggestedFileName()

        assertTrue(name.endsWith(".txt"))
        // No characters that Android or Windows would reject in a file name.
        assertTrue("unsafe name: $name", name.none { it in """\/:*?"<>|""" })
    }

    @Test
    fun `a write failure propagates so the caller can report it`() {
        val writer = FakeTextFileWriter(failWith = IOException("no space"))

        assertThrows(IOException::class.java) {
            runTest { useCase(writer)(progressWith(mapOf(1 to 1)), "loc") }
        }
    }

    @Test
    fun `mime type is plain text`() {
        assertEquals("text/plain", useCase(FakeTextFileWriter()).mimeType)
    }
}
