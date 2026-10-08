package com.hanumanchalisa.counter.presentation

import com.hanumanchalisa.counter.data.export.PlainTextSadhanaReportFormat
import com.hanumanchalisa.counter.data.importing.PlainTextSadhanaReportParser
import com.hanumanchalisa.counter.domain.FakeChalisaCountRepository
import com.hanumanchalisa.counter.domain.export.FakeTextFileWriter
import com.hanumanchalisa.counter.domain.export.FixedTimeProvider
import com.hanumanchalisa.counter.domain.importing.FakeTextFileReader
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository
import com.hanumanchalisa.counter.domain.usecase.DecrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ExportSadhanaUseCase
import com.hanumanchalisa.counter.domain.usecase.ImportSadhanaUseCase
import com.hanumanchalisa.counter.domain.usecase.IncrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ObserveSadhanaProgressUseCase
import com.hanumanchalisa.counter.domain.usecase.ResetSadhanaUseCase
import com.hanumanchalisa.counter.domain.model.SadhanaProgress
import com.hanumanchalisa.counter.presentation.state.ExportStatus
import com.hanumanchalisa.counter.presentation.state.ImportStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS

    @Before
    fun setUp() {
        // viewModelScope uses Dispatchers.Main, which has no implementation in a JVM unit test.
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val importLocation = "content://docs/counts.txt"

    /** A file exactly as the app's own export would write it, for the import tests. */
    private fun exportText(counts: Map<Int, Int>): String = PlainTextSadhanaReportFormat().format(
        SadhanaProgress(
            days = config.dayRange.map { DayProgress(day = it, count = counts[it] ?: 0) },
            config = config,
        ),
        exportedAtMillis = 1773471900000L,
    )

    private fun createViewModel(
        repository: ChalisaCountRepository,
        writer: FakeTextFileWriter = FakeTextFileWriter(),
        importFile: String? = null,
    ) = CounterViewModel(
        observeSadhanaProgress = ObserveSadhanaProgressUseCase(repository, config),
        incrementDayCount = IncrementDayCountUseCase(repository, config),
        decrementDayCount = DecrementDayCountUseCase(repository, config),
        resetSadhana = ResetSadhanaUseCase(repository),
        exportSadhana = ExportSadhanaUseCase(
            reportFormat = PlainTextSadhanaReportFormat(),
            fileWriter = writer,
            timeProvider = FixedTimeProvider(1773471900000L),
        ),
        importSadhana = ImportSadhanaUseCase(
            fileReader = FakeTextFileReader(
                importFile?.let { mapOf(importLocation to it) } ?: emptyMap(),
            ),
            reportParser = PlainTextSadhanaReportParser(),
            repository = repository,
            config = config,
        ),
    )

    @Test
    fun `state starts loading and then exposes all 40 days`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeChalisaCountRepository())

        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(40, state.progress?.days?.size)
        assertEquals(0, state.progress?.totalCount)
    }

    @Test
    fun `incrementing a day updates that day and the total`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeChalisaCountRepository())
        advanceUntilIdle()

        viewModel.onIncrementDay(1)
        viewModel.onIncrementDay(1)
        viewModel.onIncrementDay(2)
        advanceUntilIdle()

        val progress = viewModel.uiState.value.progress
        assertEquals(3, progress?.totalCount)
        assertEquals(2, progress?.days?.first { it.day == 1 }?.count)
        assertEquals(1, progress?.days?.first { it.day == 2 }?.count)
    }

    @Test
    fun `decrementing floors at zero`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeChalisaCountRepository(mapOf(6 to 1)))
        advanceUntilIdle()

        viewModel.onDecrementDay(6)
        viewModel.onDecrementDay(6)
        advanceUntilIdle()

        val progress = viewModel.uiState.value.progress
        assertEquals(0, progress?.days?.first { it.day == 6 }?.count)
        assertEquals(0, progress?.totalCount)
    }

    @Test
    fun `reset is only applied after confirmation`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository(mapOf(1 to 4))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onResetRequested()
        assertTrue(viewModel.uiState.value.isResetDialogVisible)
        // Still intact while the dialog is merely open.
        assertEquals(4, viewModel.uiState.value.progress?.totalCount)

        viewModel.onResetConfirmed()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isResetDialogVisible)
        assertEquals(0, viewModel.uiState.value.progress?.totalCount)
        assertEquals(1, repository.resetCallCount)
    }

    @Test
    fun `dismissing the dialog leaves counts untouched`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository(mapOf(1 to 4))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onResetRequested()
        viewModel.onResetDismissed()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isResetDialogVisible)
        assertEquals(4, viewModel.uiState.value.progress?.totalCount)
        assertEquals(0, repository.resetCallCount)
    }

    @Test
    fun `counts already in storage are shown on launch`() = runTest(dispatcher) {
        // Simulates reopening the app (or a phone restart): state comes from storage, not memory.
        val viewModel = createViewModel(FakeChalisaCountRepository(mapOf(1 to 3, 2 to 2, 40 to 1)))
        advanceUntilIdle()

        val progress = viewModel.uiState.value.progress
        assertEquals(6, progress?.totalCount)
        assertEquals(3, progress?.days?.first { it.day == 1 }?.count)
        assertEquals(1, progress?.days?.first { it.day == 40 }?.count)
    }

    @Test
    fun `reaching the target is reflected in state`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeChalisaCountRepository(mapOf(1 to 100)))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.progress?.isTargetReached == true)
    }

    @Test
    fun `exporting writes the current counts and reports success`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository(mapOf(1 to 4, 2 to 3))
        val writer = FakeTextFileWriter(reportedName = "counts.txt")
        val viewModel = createViewModel(repository, writer)
        advanceUntilIdle()

        viewModel.onExportLocationChosen("content://docs/counts.txt")
        advanceUntilIdle()

        assertEquals(1, writer.writeCallCount)
        assertTrue(writer.written.getValue("content://docs/counts.txt").contains("Day 1   4"))
        assertEquals(ExportStatus.Succeeded("counts.txt"), viewModel.uiState.value.exportStatus)
        assertFalse(viewModel.uiState.value.isExporting)
    }

    @Test
    fun `a failed export reports failure and keeps the counts`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository(mapOf(1 to 4))
        val writer = FakeTextFileWriter(failWith = IOException("destination gone"))
        val viewModel = createViewModel(repository, writer)
        advanceUntilIdle()

        viewModel.onExportLocationChosen("content://docs/counts.txt")
        advanceUntilIdle()

        // A failed write must never cost data or crash the app.
        assertEquals(ExportStatus.Failed, viewModel.uiState.value.exportStatus)
        assertEquals(4, viewModel.uiState.value.progress?.totalCount)
        assertEquals(4, repository.countFor(1))
        assertFalse(viewModel.uiState.value.isExporting)
    }

    @Test
    fun `export message is cleared once shown`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository(mapOf(1 to 1))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onExportLocationChosen("loc")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.exportStatus != null)

        viewModel.onExportStatusShown()

        assertEquals(null, viewModel.uiState.value.exportStatus)
    }

    @Test
    fun `export is unavailable until something has been counted`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        // Nothing recorded yet: an all-zero file would be pointless.
        assertFalse(viewModel.uiState.value.canExport)

        viewModel.onIncrementDay(1)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canExport)
    }

    @Test
    fun `choosing a file asks for confirmation before changing anything`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository(mapOf(4 to 2))
        val viewModel = createViewModel(
            repository,
            importFile = exportText(mapOf(1 to 5, 2 to 3)),
        )
        advanceUntilIdle()

        viewModel.onImportFileChosen(importLocation)
        advanceUntilIdle()

        val preview = viewModel.uiState.value.importPreview
        assertEquals(8, preview?.restoredTotal)
        assertEquals(10, preview?.totalAfterImport)
        // Counts stay untouched while the dialog is merely open.
        assertEquals(2, viewModel.uiState.value.progress?.totalCount)
        assertEquals(0, repository.restoreCallCount)
    }

    @Test
    fun `confirming the import restores the file and keeps days it does not mention`() =
        runTest(dispatcher) {
            val repository = FakeChalisaCountRepository(mapOf(4 to 2))
            val viewModel = createViewModel(
                repository,
                importFile = exportText(mapOf(1 to 5, 2 to 3)),
            )
            advanceUntilIdle()

            viewModel.onImportFileChosen(importLocation)
            advanceUntilIdle()
            viewModel.onImportConfirmed()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(10, state.progress?.totalCount)
            assertEquals(5, state.progress?.days?.first { it.day == 1 }?.count)
            // The day counted only on this phone survives the import.
            assertEquals(2, state.progress?.days?.first { it.day == 4 }?.count)
            assertEquals(ImportStatus.Succeeded(totalCount = 10, targetCount = 100), state.importStatus)
            assertEquals(null, state.importPreview)
            assertFalse(state.isImporting)
        }

    @Test
    fun `cancelling the import changes nothing`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository(mapOf(4 to 2))
        val viewModel = createViewModel(repository, importFile = exportText(mapOf(1 to 5)))
        advanceUntilIdle()

        viewModel.onImportFileChosen(importLocation)
        advanceUntilIdle()
        viewModel.onImportDismissed()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.importPreview)
        assertEquals(2, viewModel.uiState.value.progress?.totalCount)
        assertEquals(0, repository.restoreCallCount)
    }

    @Test
    fun `a day counted here and in the file is reported before being replaced`() =
        runTest(dispatcher) {
            val repository = FakeChalisaCountRepository(mapOf(12 to 1))
            val viewModel = createViewModel(repository, importFile = exportText(mapOf(12 to 2)))
            advanceUntilIdle()

            viewModel.onImportFileChosen(importLocation)
            advanceUntilIdle()

            val replacement = viewModel.uiState.value.importPreview?.replacedDays?.single()
            assertEquals(12, replacement?.day)
            assertEquals(1, replacement?.currentCount)
            assertEquals(2, replacement?.importedCount)

            viewModel.onImportConfirmed()
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.progress?.totalCount)
        }

    @Test
    fun `importing a file whose counts already match says so without a dialog`() =
        runTest(dispatcher) {
            val repository = FakeChalisaCountRepository(mapOf(1 to 5))
            val viewModel = createViewModel(repository, importFile = exportText(mapOf(1 to 5)))
            advanceUntilIdle()

            viewModel.onImportFileChosen(importLocation)
            advanceUntilIdle()

            assertEquals(null, viewModel.uiState.value.importPreview)
            assertEquals(ImportStatus.AlreadyUpToDate, viewModel.uiState.value.importStatus)
            assertEquals(0, repository.restoreCallCount)
        }

    @Test
    fun `importing the same file twice does not count anything twice`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository()
        val viewModel = createViewModel(repository, importFile = exportText(mapOf(1 to 5, 2 to 3)))
        advanceUntilIdle()

        viewModel.onImportFileChosen(importLocation)
        advanceUntilIdle()
        viewModel.onImportConfirmed()
        advanceUntilIdle()
        assertEquals(8, viewModel.uiState.value.progress?.totalCount)

        viewModel.onImportFileChosen(importLocation)
        advanceUntilIdle()

        // Second time round there is nothing left to change, so no dialog and no double count.
        assertEquals(ImportStatus.AlreadyUpToDate, viewModel.uiState.value.importStatus)
        assertEquals(8, viewModel.uiState.value.progress?.totalCount)
    }

    @Test
    fun `the wrong kind of file is rejected and the counts are kept`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository(mapOf(1 to 3))
        val viewModel = createViewModel(repository, importFile = "Shopping list\nMilk\n")
        advanceUntilIdle()

        viewModel.onImportFileChosen(importLocation)
        advanceUntilIdle()

        assertEquals(ImportStatus.NotAnExport, viewModel.uiState.value.importStatus)
        assertEquals(null, viewModel.uiState.value.importPreview)
        assertEquals(3, viewModel.uiState.value.progress?.totalCount)
    }

    @Test
    fun `an edited file is rejected and the counts are kept`() = runTest(dispatcher) {
        val edited = exportText(mapOf(1 to 5)).replace("Day 1   5", "Day 1   50")
        val repository = FakeChalisaCountRepository(mapOf(1 to 3))
        val viewModel = createViewModel(repository, importFile = edited)
        advanceUntilIdle()

        viewModel.onImportFileChosen(importLocation)
        advanceUntilIdle()

        assertEquals(ImportStatus.Damaged, viewModel.uiState.value.importStatus)
        assertEquals(3, viewModel.uiState.value.progress?.totalCount)
    }

    @Test
    fun `an unreadable file is reported and the counts are kept`() = runTest(dispatcher) {
        val repository = FakeChalisaCountRepository(mapOf(1 to 3))
        // No file registered at the location, so the reader throws.
        val viewModel = createViewModel(repository, importFile = null)
        advanceUntilIdle()

        viewModel.onImportFileChosen(importLocation)
        advanceUntilIdle()

        assertEquals(ImportStatus.Unreadable, viewModel.uiState.value.importStatus)
        assertEquals(3, viewModel.uiState.value.progress?.totalCount)
        assertFalse(viewModel.uiState.value.isImporting)
    }

    @Test
    fun `an all-zero export has nothing to import`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            FakeChalisaCountRepository(),
            importFile = exportText(emptyMap()),
        )
        advanceUntilIdle()

        viewModel.onImportFileChosen(importLocation)
        advanceUntilIdle()

        assertEquals(ImportStatus.NothingToImport, viewModel.uiState.value.importStatus)
    }

    @Test
    fun `import message is cleared once shown`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            FakeChalisaCountRepository(),
            importFile = exportText(emptyMap()),
        )
        advanceUntilIdle()

        viewModel.onImportFileChosen(importLocation)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.importStatus != null)

        viewModel.onImportStatusShown()

        assertEquals(null, viewModel.uiState.value.importStatus)
    }

    @Test
    fun `import is available on a fresh install, unlike export`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeChalisaCountRepository())
        advanceUntilIdle()

        // Nothing to export yet, but importing is exactly what a new phone needs.
        assertFalse(viewModel.uiState.value.canExport)
        assertTrue(viewModel.uiState.value.canImport)
    }
}
