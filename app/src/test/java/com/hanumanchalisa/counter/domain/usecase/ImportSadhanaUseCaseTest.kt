package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.data.export.PlainTextSadhanaReportFormat
import com.hanumanchalisa.counter.data.importing.PlainTextSadhanaReportParser
import com.hanumanchalisa.counter.domain.FakeChalisaCountRepository
import com.hanumanchalisa.counter.domain.importing.FakeTextFileReader
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.model.SadhanaProgress
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ImportSadhanaUseCaseTest {

    private val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS
    private val location = "content://docs/counts.txt"

    private fun progressWith(counts: Map<Int, Int>) = SadhanaProgress(
        days = config.dayRange.map { DayProgress(day = it, count = counts[it] ?: 0) },
        config = config,
    )

    private fun exportOf(counts: Map<Int, Int>): String = PlainTextSadhanaReportFormat()
        .format(progressWith(counts), exportedAtMillis = 1773471900000L)

    private fun useCase(
        fileText: String?,
        repository: FakeChalisaCountRepository = FakeChalisaCountRepository(),
    ) = ImportSadhanaUseCase(
        fileReader = FakeTextFileReader(fileText?.let { mapOf(location to it) } ?: emptyMap()),
        reportParser = PlainTextSadhanaReportParser(),
        repository = repository,
        config = config,
    )

    private suspend fun previewOf(
        fileCounts: Map<Int, Int>,
        current: Map<Int, Int> = emptyMap(),
        repository: FakeChalisaCountRepository = FakeChalisaCountRepository(current),
    ) = useCase(exportOf(fileCounts), repository).preview(location, progressWith(current))

    @Test
    fun `restores every day from the file onto an empty phone`() = runTest {
        val repository = FakeChalisaCountRepository()
        val useCase = useCase(exportOf(mapOf(1 to 5, 2 to 3, 3 to 4)), repository)

        val check = useCase.preview(location, progressWith(emptyMap())) as ImportCheck.Ready
        assertEquals(12, check.preview.restoredTotal)
        assertEquals(12, check.preview.totalAfterImport)
        // Nothing is written until the user confirms.
        assertEquals(0, repository.restoreCallCount)

        useCase.apply(check.preview)

        assertEquals(5, repository.countFor(1))
        assertEquals(3, repository.countFor(2))
        assertEquals(4, repository.countFor(3))
    }

    @Test
    fun `days absent from the file keep their count on this phone`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(4 to 2))
        val useCase = useCase(exportOf(mapOf(1 to 5, 2 to 3, 3 to 4)), repository)

        val check = useCase.preview(location, progressWith(mapOf(4 to 2))) as ImportCheck.Ready
        assertEquals(listOf(DayProgress(day = 4, count = 2)), check.preview.keptDays)
        assertEquals(14, check.preview.totalAfterImport)
        assertTrue(check.preview.replacedDays.isEmpty())

        useCase.apply(check.preview)

        assertEquals(2, repository.countFor(4))
    }

    @Test
    fun `a zero in the file never clears a count on this phone`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(4 to 2))
        // Day 4 is present in the file as an explicit zero.
        val useCase = useCase(exportOf(mapOf(1 to 5)), repository)

        val check = useCase.preview(location, progressWith(mapOf(4 to 2))) as ImportCheck.Ready
        useCase.apply(check.preview)

        assertEquals(2, repository.countFor(4))
        assertEquals(7, check.preview.totalAfterImport)
    }

    @Test
    fun `a day counted in both places takes the count from the file and is reported`() = runTest {
        val check = previewOf(fileCounts = mapOf(12 to 2), current = mapOf(12 to 1))
            as ImportCheck.Ready

        val replacement = check.preview.replacedDays.single()
        assertEquals(12, replacement.day)
        assertEquals(1, replacement.currentCount)
        assertEquals(2, replacement.importedCount)
        assertEquals(2, check.preview.totalAfterImport)
    }

    @Test
    fun `importing the same file twice gives the same counts`() = runTest {
        val repository = FakeChalisaCountRepository()
        val useCase = useCase(exportOf(mapOf(1 to 5, 2 to 3)), repository)

        val first = useCase.preview(location, progressWith(emptyMap())) as ImportCheck.Ready
        useCase.apply(first.preview)
        val afterFirst = progressWith(mapOf(1 to repository.countFor(1), 2 to repository.countFor(2)))

        val second = useCase.preview(location, afterFirst) as ImportCheck.Ready
        useCase.apply(second.preview)

        // Set, not added: the totals are identical after the second import.
        assertEquals(5, repository.countFor(1))
        assertEquals(3, repository.countFor(2))
        assertTrue(second.preview.changesNothing)
    }

    @Test
    fun `a file matching this phone is flagged as changing nothing`() = runTest {
        val check = previewOf(fileCounts = mapOf(1 to 5), current = mapOf(1 to 5))
            as ImportCheck.Ready

        assertTrue(check.preview.changesNothing)
    }

    @Test
    fun `an unrelated file is rejected without writing`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(1 to 3))

        val check = useCase("Shopping list\nMilk\n", repository)
            .preview(location, progressWith(mapOf(1 to 3)))

        assertEquals(ImportCheck.NotAnExport, check)
        assertEquals(0, repository.restoreCallCount)
        assertEquals(3, repository.countFor(1))
    }

    @Test
    fun `an edited file is rejected without writing`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(1 to 3))
        val edited = exportOf(mapOf(1 to 5)).replace("Day 1   5", "Day 1   50")

        val check = useCase(edited, repository).preview(location, progressWith(mapOf(1 to 3)))

        assertEquals(ImportCheck.Damaged, check)
        assertEquals(0, repository.restoreCallCount)
        assertEquals(3, repository.countFor(1))
    }

    @Test
    fun `an all-zero export has nothing to import`() = runTest {
        val check = previewOf(fileCounts = emptyMap())

        assertEquals(ImportCheck.NothingToImport, check)
    }

    @Test
    fun `an oversized file is not treated as an export`() = runTest {
        val huge = "Day 1  1\n".repeat(20_000)

        val check = useCase(huge).preview(location, progressWith(emptyMap()))

        assertEquals(ImportCheck.NotAnExport, check)
    }

    @Test
    fun `a read failure propagates so the caller can report it`() {
        assertThrows(IOException::class.java) {
            runTest { useCase(fileText = null).preview(location, progressWith(emptyMap())) }
        }
    }

    @Test
    fun `offers plain text to the file picker`() {
        assertTrue(useCase("").acceptedMimeTypes.contains("text/plain"))
    }
}
