package com.hanumanchalisa.counter.domain.export

/**
 * Writes text to a destination the user has chosen.
 *
 * [location] is deliberately an opaque [String] rather than an Android `Uri`: that keeps this
 * contract — and therefore the whole domain layer — free of framework types, while the data layer
 * implementation is free to interpret it as a document URI. Tests substitute an in-memory writer.
 */
interface TextFileWriter {

    /**
     * Writes [text] to [location], replacing anything already there. Throws on failure.
     *
     * @return the name the file was actually saved as, or null if that cannot be determined. The
     *   writer reports this rather than the caller predicting it, because the user may have renamed
     *   the file in the system save dialog — so only the destination knows its real name.
     */
    suspend fun write(location: String, text: String): String?
}
