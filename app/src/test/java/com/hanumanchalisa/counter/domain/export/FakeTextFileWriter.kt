package com.hanumanchalisa.counter.domain.export

import java.io.IOException

/**
 * In-memory stand-in for a real file destination.
 *
 * Substitutable for [TextFileWriter] anywhere the production writer is used (Liskov), which is what
 * lets the export logic be tested on the JVM with no device, no file system and no permissions.
 *
 * @param failWith set to throw instead of writing, to exercise the failure path.
 * @param reportedName the display name to report back; null mimics a provider that supplies none.
 */
class FakeTextFileWriter(
    private val failWith: IOException? = null,
    private val reportedName: String? = null,
) : TextFileWriter {

    /** Everything written so far, keyed by location, so tests can assert on the exact contents. */
    val written: MutableMap<String, String> = mutableMapOf()

    var writeCallCount: Int = 0
        private set

    override suspend fun write(location: String, text: String): String? {
        writeCallCount++
        failWith?.let { throw it }
        written[location] = text
        return reportedName
    }
}
