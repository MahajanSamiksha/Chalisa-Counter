package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.domain.FakeChalisaCountRepository
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveSadhanaProgressUseCaseTest {

    private val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS

    @Test
    fun `an empty store still yields all 40 days at zero`() = runTest {
        val observe = ObserveSadhanaProgressUseCase(FakeChalisaCountRepository(), config)

        val progress = observe().first()

        assertEquals(40, progress.days.size)
        assertEquals(0, progress.totalCount)
        assertTrue(progress.days.all { it.count == 0 })
    }

    @Test
    fun `sparse records expand into the full day list in order`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(1 to 2, 40 to 5))
        val observe = ObserveSadhanaProgressUseCase(repository, config)

        val progress = observe().first()

        assertEquals(40, progress.days.size)
        assertEquals(config.dayRange.toList(), progress.days.map { it.day })
        assertEquals(2, progress.days.first { it.day == 1 }.count)
        assertEquals(0, progress.days.first { it.day == 2 }.count)
        assertEquals(5, progress.days.first { it.day == 40 }.count)
    }

    @Test
    fun `total is the sum across every day`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(1 to 3, 2 to 4, 15 to 10))
        val observe = ObserveSadhanaProgressUseCase(repository, config)

        assertEquals(17, observe().first().totalCount)
    }

    @Test
    fun `progress reflects writes made after collection starts`() = runTest {
        val repository = FakeChalisaCountRepository()
        val observe = ObserveSadhanaProgressUseCase(repository, config)
        val increment = IncrementDayCountUseCase(repository, config)

        assertEquals(0, observe().first().totalCount)
        increment(4)
        increment(4)

        val updated = observe().first()
        assertEquals(2, updated.totalCount)
        assertEquals(2, updated.days.first { it.day == 4 }.count)
    }

    @Test
    fun `target is not reached below the goal`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(1 to 99))
        val observe = ObserveSadhanaProgressUseCase(repository, config)

        val progress = observe().first()

        assertFalse(progress.isTargetReached)
        assertEquals(1, progress.remainingCount)
        assertEquals(0.99f, progress.completionFraction, 0.001f)
    }

    @Test
    fun `target reached at exactly the goal`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(1 to 100))
        val observe = ObserveSadhanaProgressUseCase(repository, config)

        val progress = observe().first()

        assertTrue(progress.isTargetReached)
        assertEquals(0, progress.remainingCount)
        assertEquals(1f, progress.completionFraction, 0.001f)
    }

    @Test
    fun `exceeding the target clamps the progress fraction`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(1 to 150))
        val observe = ObserveSadhanaProgressUseCase(repository, config)

        val progress = observe().first()

        assertEquals(150, progress.totalCount)
        assertEquals(1f, progress.completionFraction, 0.001f)
        assertEquals(0, progress.remainingCount)
    }

    @Test
    fun `reset returns every day to zero`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(1 to 5, 2 to 6))
        val observe = ObserveSadhanaProgressUseCase(repository, config)
        val reset = ResetSadhanaUseCase(repository)

        assertEquals(11, observe().first().totalCount)
        reset()

        val afterReset = observe().first()
        assertEquals(0, afterReset.totalCount)
        assertEquals(40, afterReset.days.size)
        assertTrue(afterReset.days.all { it.count == 0 })
    }

    @Test
    fun `a shorter cycle config yields that many days`() = runTest {
        val shortConfig = SadhanaConfig(totalDays = 21, targetCount = 50)
        val observe = ObserveSadhanaProgressUseCase(FakeChalisaCountRepository(), shortConfig)

        val progress = observe().first()

        assertEquals(21, progress.days.size)
        assertEquals(50, progress.targetCount)
    }
}
