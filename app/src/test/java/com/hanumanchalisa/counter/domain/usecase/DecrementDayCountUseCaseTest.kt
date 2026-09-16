package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.domain.FakeChalisaCountRepository
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DecrementDayCountUseCaseTest {

    private val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS

    @Test
    fun `decrement removes one recitation`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(2 to 3))
        val decrement = DecrementDayCountUseCase(repository, config)

        decrement(2)

        assertEquals(2, repository.countFor(2))
    }

    @Test
    fun `decrement stops at zero and never goes negative`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(5 to 1))
        val decrement = DecrementDayCountUseCase(repository, config)

        repeat(4) { decrement(5) }

        assertEquals(0, repository.countFor(5))
    }

    @Test
    fun `decrementing an untouched day is a no-op`() = runTest {
        val repository = FakeChalisaCountRepository()
        val decrement = DecrementDayCountUseCase(repository, config)

        decrement(9)

        assertEquals(0, repository.countFor(9))
    }

    @Test
    fun `decrement affects only the targeted day`() = runTest {
        val repository = FakeChalisaCountRepository(mapOf(1 to 2, 2 to 2))
        val decrement = DecrementDayCountUseCase(repository, config)

        decrement(1)

        assertEquals(1, repository.countFor(1))
        assertEquals(2, repository.countFor(2))
    }
}
