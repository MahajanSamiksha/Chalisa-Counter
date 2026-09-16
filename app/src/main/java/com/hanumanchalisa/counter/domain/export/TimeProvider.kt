package com.hanumanchalisa.counter.domain.export

/**
 * Supplies the current time.
 *
 * Reading the clock directly would make anything that stamps a date untestable, since the expected
 * value would change every run. Depending on this abstraction instead lets tests pin the clock to a
 * fixed instant (Dependency Inversion), while the app supplies the real one.
 */
fun interface TimeProvider {
    fun currentTimeMillis(): Long
}
