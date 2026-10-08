package com.hanumanchalisa.counter.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanumanchalisa.counter.R
import com.hanumanchalisa.counter.domain.importing.DayReplacement
import com.hanumanchalisa.counter.domain.importing.ImportPreview
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.presentation.ui.theme.HanumanChalisaTheme

/**
 * Confirmation step shown before an import, spelling out what the file will and will not change.
 *
 * Import can overwrite a day that was already counted on this phone, so the dialog names every such
 * day with both numbers rather than reporting a single total the user would have to reconcile
 * afterwards. Dismiss is the safe default: tapping outside or pressing back imports nothing.
 */
@Composable
fun ImportConfirmDialog(
    preview: ImportPreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.import_dialog_title),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Section(title = stringResource(R.string.import_dialog_restored_heading)) {
                    Text(
                        stringResource(
                            R.string.import_dialog_restored,
                            pluralStringResource(
                                R.plurals.recitation_count,
                                preview.restoredTotal,
                                preview.restoredTotal,
                            ),
                            pluralStringResource(
                                R.plurals.day_count,
                                preview.restoredDays.size,
                                preview.restoredDays.size,
                            ),
                        ),
                    )
                }

                if (preview.keptDays.isNotEmpty()) {
                    // Each day is formatted by stringResource rather than String.format so the
                    // numbers use the same locale as the rest of the dialog. map is inline, so a
                    // composable call inside it is allowed; joinToString's lambda is not.
                    val keptDays = preview.keptDays.map { day ->
                        stringResource(R.string.import_dialog_kept_day, day.day, day.count)
                    }.joinToString()
                    Section(title = stringResource(R.string.import_dialog_kept_heading)) {
                        Text(stringResource(R.string.import_dialog_kept, keptDays))
                    }
                }

                if (preview.replacedDays.isNotEmpty()) {
                    Section(title = stringResource(R.string.import_dialog_replaced_heading)) {
                        Text(stringResource(R.string.import_dialog_replaced_intro))
                        preview.replacedDays.forEach { replacement ->
                            Text(
                                stringResource(
                                    R.string.import_dialog_replaced_row,
                                    replacement.day,
                                    replacement.currentCount,
                                    replacement.importedCount,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(
                        R.string.import_dialog_total,
                        preview.totalAfterImport,
                        preview.targetCount,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.import_dialog_safety),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.import_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.import_dialog_cancel))
            }
        },
    )
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportConfirmDialogPreview() {
    HanumanChalisaTheme {
        ImportConfirmDialog(
            preview = ImportPreview(
                restoredDays = listOf(
                    DayProgress(day = 1, count = 5),
                    DayProgress(day = 2, count = 3),
                    DayProgress(day = 12, count = 2),
                ),
                keptDays = listOf(DayProgress(day = 4, count = 2)),
                replacedDays = listOf(
                    DayReplacement(day = 12, currentCount = 1, importedCount = 2),
                ),
                totalAfterImport = 12,
                targetCount = 100,
                changesNothing = false,
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
