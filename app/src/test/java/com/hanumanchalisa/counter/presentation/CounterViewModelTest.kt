package com.hanumanchalisa.counter.presentation

import com.hanumanchalisa.counter.data.export.PlainTextSadhanaReportFormat
import com.hanumanchalisa.counter.domain.FakeChalisaCountRepository
import com.hanumanchalisa.counter.domain.export.FakeTextFileWriter
import com.hanumanchalisa.counter.domain.export.FixedTimeProvider
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository
import com.hanumanchalisa.counter.domain.usecase.DecrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ExportSadhanaUseCase
import com.hanumanchalisa.counter.domain.usecase.IncrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ObserveSadhanaProgressUseCase
import com.hanumanchalisa.counter.domain.usecase.ResetSadhanaUseCase
import com.hanumanchalisa.counter.presentation.state.ExportStatus
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

    private fun createViewModel(
        repository: ChalisaCountRepository,
        writer: FakeTextFileWriter = FakeTextFileWriter(),
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
}
