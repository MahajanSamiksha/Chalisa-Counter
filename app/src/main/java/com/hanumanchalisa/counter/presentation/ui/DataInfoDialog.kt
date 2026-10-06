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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hanumanchalisa.counter.R

@Composable
fun DataInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.data_info_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.data_info_storage_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(stringResource(R.string.data_info_storage))
                Text(
                    stringResource(R.string.data_info_backup_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(stringResource(R.string.data_info_backup))
                Text(
                    stringResource(R.string.data_info_export_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(stringResource(R.string.data_info_export))
                Text(
                    stringResource(R.string.data_info_deletion_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(stringResource(R.string.data_info_deletion))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.data_info_close))
            }
        },
    )
}
