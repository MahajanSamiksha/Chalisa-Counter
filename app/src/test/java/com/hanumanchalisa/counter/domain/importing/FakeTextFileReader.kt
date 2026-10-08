package com.hanumanchalisa.counter.domain.importing

import java.io.IOException

/**
 * In-memory stand-in for a picked document, so import can be tested on the JVM.
 *
 * @param files contents keyed by location; a missing location behaves like an unreadable file.
 */
class FakeTextFileReader(
    private val files: Map<String, String> = emptyMap(),
) : TextFileReader {

    override suspend fun read(location: String, maxBytes: Int): String? {
        val text = files[location] ?: throw IOException("No file at $location")
        return if (text.toByteArray(Charsets.UTF_8).size > maxBytes) null else text
    }
}
