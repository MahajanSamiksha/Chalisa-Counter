package com.hanumanchalisa.counter.data.importing

import android.content.Context
import androidx.core.net.toUri
import com.hanumanchalisa.counter.domain.importing.TextFileReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Reads a document the user picked through the system file picker.
 *
 * As with export, the Storage Access Framework grants access to exactly the one file chosen, so
 * import needs no storage permission and works the same for local storage, Drive or a file saved
 * from WhatsApp.
 */
class DocumentTextFileReader(
    context: Context,
) : TextFileReader {

    private val appContext: Context = context.applicationContext

    override suspend fun read(location: String, maxBytes: Int): String? = withContext(Dispatchers.IO) {
        val uri = location.toUri()
        val stream = appContext.contentResolver.openInputStream(uri)
            ?: throw IOException("Could not open $uri for reading")

        stream.use { input ->
            // Read one byte past the limit: if it arrives, the file is too large to be an export.
            val bytes = input.readNBytesCompat(maxBytes + 1)
            if (bytes.size > maxBytes) null else bytes.toString(Charsets.UTF_8)
        }
    }

    /** `InputStream.readNBytes` needs API 33; this does the same on every supported version. */
    private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
        val buffer = ByteArray(limit)
        var filled = 0
        while (filled < limit) {
            val read = read(buffer, filled, limit - filled)
            if (read < 0) break
            filled += read
        }
        return buffer.copyOf(filled)
    }
}
