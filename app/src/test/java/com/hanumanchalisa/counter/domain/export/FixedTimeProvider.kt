package com.hanumanchalisa.counter.domain.export

/** A clock pinned to one instant, so date-stamped output is deterministic across runs. */
class FixedTimeProvider(private val fixedMillis: Long) : TimeProvider {
    override fun currentTimeMillis(): Long = fixedMillis
}
