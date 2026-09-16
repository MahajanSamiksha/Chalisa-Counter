package com.hanumanchalisa.counter.data.export

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.hanumanchalisa.counter.domain.export.TextFileWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Writes text to a document the user picked through the system file picker.
 *
 * Using the Storage Access Framework means the app gets permission to exactly the one file you chose
 * and nothing else — so the export needs **no storage permission at all**, and works to internal
 * storage, an SD card, or a cloud provider like Drive without knowing the difference.
 *
 * The `"wt"` mode matters: plain `"w"` leaves any trailing bytes of an existing longer file in place,
 * which would corrupt the report when overwriting a previous export. `"wt"` truncates first.
 */
class DocumentTextFileWriter(
    context: Context,
) : TextFileWriter {

    private val appContext: Context = context.applicationContext

    override suspend fun write(location: String, text: String): String? = withContext(Dispatchers.IO) {
        val uri = location.toUri()
        val stream = appContext.contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("Could not open $uri for writing")

        stream.use { output ->
            output.write(text.toByteArray(Charsets.UTF_8))
            output.flush()
        }

        displayNameOf(uri)
    }

    /**
     * Looks up the document's own name so the confirmation message can quote it.
     *
     * Best-effort: a provider is not obliged to supply a display name, and failing to read one must
     * not turn a successful write into a reported failure — hence the null return rather than a throw.
     */
    private fun displayNameOf(uri: Uri): String? = try {
        appContext.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameColumn >= 0 && cursor.moveToFirst()) cursor.getString(nameColumn) else null
            }
    } catch (e: Exception) {
        null
    }
}
