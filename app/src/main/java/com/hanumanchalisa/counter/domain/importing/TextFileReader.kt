package com.hanumanchalisa.counter.domain.importing

/**
 * Reads text from a source the user has chosen — the counterpart of
 * [com.hanumanchalisa.counter.domain.export.TextFileWriter].
 *
 * [location] is an opaque [String] for the same reason as on the writer: the domain layer stays free
 * of Android types, and tests substitute an in-memory reader.
 */
interface TextFileReader {

    /**
     * Reads the whole of [location] as UTF-8 text. Throws if it cannot be opened or read.
     *
     * @return the text, or null when the file is larger than [maxBytes]. The limit means picking a
     *   video or a large document by mistake is rejected without loading it into memory.
     */
    suspend fun read(location: String, maxBytes: Int): String?
}
