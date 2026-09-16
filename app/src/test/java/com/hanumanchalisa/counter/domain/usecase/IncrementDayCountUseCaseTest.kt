package com.hanumanchalisa.counter.domain.usecase

import com.hanumanchalisa.counter.domain.FakeChalisaCountRepository
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class IncrementDayCountUseCaseTest {

    private val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS

    @Test
    fun `increment adds one to an untouched day`() = runTest {
        val repository = FakeChalisaCountRepository()
        val increment = IncrementDayCountUseCase(repository, config)

        increment(1)

        assertEquals(1, repository.countFor(1))
    }

    @Test
    fun `repeated increments accumulate`() = runTest {
        val repository = FakeChalisaCountRepository()
        val increment = IncrementDayCountUseCase(repository, config)

        repeat(5) { increment(7) }

        assertEquals(5, repository.countFor(7))
    }

    @Test
    fun `increment affects only the targeted day`() = runTest {
        val repository = FakeChalisaCountRepository()
        val increment = IncrementDayCountUseCase(repository, config)

        increment(3)
        increment(3)
        increment(10)

        assertEquals(2, repository.countFor(3))
        assertEquals(1, repository.countFor(10))
        assertEquals(0, repository.countFor(4))
    }

    @Test
    fun `the last day of the cycle is incrementable`() = runTest {
        val repository = FakeChalisaCountRepository()
        val increment = IncrementDayCountUseCase(repository, config)

        increment(40)

        assertEquals(1, repository.countFor(40))
    }

    @Test
    fun `a day beyond the cycle is rejected`() = runTest {
        val repository = FakeChalisaCountRepository()
        val increment = IncrementDayCountUseCase(repository, config)

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { increment(41) }
        }
        assertEquals(0, repository.countFor(41))
    }

    @Test
    fun `a day below the cycle is rejected`() = runTest {
        val repository = FakeChalisaCountRepository()
        val increment = IncrementDayCountUseCase(repository, config)

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { increment(0) }
        }
    }
}
