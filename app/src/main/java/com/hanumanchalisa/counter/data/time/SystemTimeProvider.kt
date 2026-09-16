package com.hanumanchalisa.counter.data.time

import com.hanumanchalisa.counter.domain.export.TimeProvider

/** The real clock, used by the running app. */
class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
