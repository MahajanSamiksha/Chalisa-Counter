package com.hanumanchalisa.counter.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanumanchalisa.counter.R
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.model.SadhanaProgress
import com.hanumanchalisa.counter.presentation.state.CounterUiState
import com.hanumanchalisa.counter.presentation.state.ExportStatus
import com.hanumanchalisa.counter.presentation.state.ImportStatus
import com.hanumanchalisa.counter.presentation.ui.theme.HanumanChalisaTheme

/**
 * The single screen of the app: fixed progress header, scrollable Day 1 … Day 40 list, and a Reset
 * button pinned to the bottom.
 *
 * Renders [uiState] and forwards counter events. Counter state lives in the ViewModel; the
 * informational dialog keeps its own visibility state.
 */
@Composable
fun CounterScreen(
    uiState: CounterUiState,
    onIncrementDay: (Int) -> Unit,
    onDecrementDay: (Int) -> Unit,
    onResetRequested: () -> Unit,
    onResetConfirmed: () -> Unit,
    onResetDismissed: () -> Unit,
    onExportRequested: () -> Unit,
    onExportStatusShown: () -> Unit,
    onImportRequested: () -> Unit,
    onImportConfirmed: () -> Unit,
    onImportDismissed: () -> Unit,
    onImportStatusShown: () -> Unit,
    onPrivacyPolicyRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = uiState.progress
    val snackbarHostState = remember { SnackbarHostState() }
    var isDataInfoVisible by rememberSaveable { mutableStateOf(false) }

    // Show the export outcome once, then tell the ViewModel it has been seen so a rotation does not
    // replay it. Keyed on the status object so each new outcome triggers a fresh message.
    val exportStatus = uiState.exportStatus
    val successMessage = stringResource(
        R.string.export_succeeded,
        (exportStatus as? ExportStatus.Succeeded)?.fileName ?: "",
    )
    val failureMessage = stringResource(R.string.export_failed)
    val exportButtonDescription = stringResource(R.string.export_button_description)
    LaunchedEffect(exportStatus) {
        if (exportStatus == null) return@LaunchedEffect
        val message = when (exportStatus) {
            is ExportStatus.Succeeded -> successMessage
            ExportStatus.Failed -> failureMessage
        }
        snackbarHostState.showSnackbar(message)
        onExportStatusShown()
    }

    // Import messages work the same way, and each one states whether the counts changed.
    val importStatus = uiState.importStatus
    val importButtonDescription = stringResource(R.string.import_button_description)
    val importSucceeded = stringResource(
        R.string.import_succeeded,
        (importStatus as? ImportStatus.Succeeded)?.totalCount ?: 0,
        (importStatus as? ImportStatus.Succeeded)?.targetCount ?: 0,
    )
    val importUpToDate = stringResource(R.string.import_already_up_to_date)
    val importNotAnExport = stringResource(R.string.import_not_an_export)
    val importDamaged = stringResource(R.string.import_damaged)
    val importNothing = stringResource(R.string.import_nothing_to_import)
    val importUnreadable = stringResource(R.string.import_unreadable)
    val importFailed = stringResource(R.string.import_failed)
    LaunchedEffect(importStatus) {
        if (importStatus == null) return@LaunchedEffect
        val message = when (importStatus) {
            is ImportStatus.Succeeded -> importSucceeded
            ImportStatus.AlreadyUpToDate -> importUpToDate
            ImportStatus.NotAnExport -> importNotAnExport
            ImportStatus.Damaged -> importDamaged
            ImportStatus.NothingToImport -> importNothing
            ImportStatus.Unreadable -> importUnreadable
            ImportStatus.Failed -> importFailed
        }
        snackbarHostState.showSnackbar(message)
        onImportStatusShown()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            // Header sits outside the list so the total stays visible while scrolling.
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onPrivacyPolicyRequested,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.privacy_policy_button))
                    }
                    TextButton(
                        onClick = { isDataInfoVisible = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.data_info_button))
                    }
                }
                if (progress != null) {
                    ProgressHeader(progress = progress)
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Export and Import are the pair that moves a practice between phones, so they
                    // share a row above the destructive action.
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onExportRequested,
                            enabled = uiState.canExport,
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = exportButtonDescription
                                },
                        ) {
                            Text(
                                text = stringResource(R.string.export_button),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )
                        }

                        OutlinedButton(
                            onClick = onImportRequested,
                            enabled = uiState.canImport,
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = importButtonDescription
                                },
                        ) {
                            Text(
                                text = stringResource(R.string.import_button),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )
                        }
                    }

                    // Reset is filled, so the destructive action stands out in colour while sitting
                    // on its own row, apart from the everyday buttons.
                    Button(
                        onClick = onResetRequested,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.reset_button),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        if (progress == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Keyed by day number so rows keep their identity and recycle correctly.
                items(items = progress.days, key = { it.day }) { dayProgress ->
                    DayCountRow(
                        dayProgress = dayProgress,
                        onIncrement = onIncrementDay,
                        onDecrement = onDecrementDay,
                    )
                }
            }
        }
    }

    if (isDataInfoVisible) {
        DataInfoDialog(onDismiss = { isDataInfoVisible = false })
    }

    uiState.importPreview?.let { preview ->
        ImportConfirmDialog(
            preview = preview,
            onConfirm = onImportConfirmed,
            onDismiss = onImportDismissed,
        )
    }

    if (uiState.isResetDialogVisible) {
        ResetConfirmDialog(
            onConfirm = onResetConfirmed,
            onDismiss = onResetDismissed,
        )
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun CounterScreenPreview() {
    val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS
    HanumanChalisaTheme {
        CounterScreen(
            uiState = CounterUiState(
                progress = SadhanaProgress(
                    days = config.dayRange.map { day ->
                        DayProgress(day = day, count = if (day <= 5) day else 0)
                    },
                    config = config,
                ),
            ),
            onIncrementDay = {},
            onDecrementDay = {},
            onResetRequested = {},
            onResetConfirmed = {},
            onResetDismissed = {},
            onExportRequested = {},
            onExportStatusShown = {},
            onImportRequested = {},
            onImportConfirmed = {},
            onImportDismissed = {},
            onImportStatusShown = {},
            onPrivacyPolicyRequested = {},
        )
    }
}
