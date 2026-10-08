package com.hanumanchalisa.counter

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanumanchalisa.counter.presentation.CounterViewModel
import com.hanumanchalisa.counter.presentation.CounterViewModelFactory
import com.hanumanchalisa.counter.presentation.ui.CounterScreen
import com.hanumanchalisa.counter.presentation.ui.theme.HanumanChalisaTheme

/**
 * The app's only activity: builds the ViewModel from the application's container and hands its state
 * to [CounterScreen].
 *
 * Deliberately thin — it wires things together and nothing more, so no business logic is tied to the
 * Android framework.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as ChalisaApplication).container

        setContent {
            HanumanChalisaTheme {
                val viewModel: CounterViewModel = viewModel(
                    factory = CounterViewModelFactory(container),
                )
                val uiState by viewModel.uiState.collectAsState()

                // The system's own "save file" dialog. Using it means the app never needs a storage
                // permission and you choose exactly where the export goes — Downloads, Drive, or
                // anywhere else. A null result means you backed out, so nothing happens.
                val createDocument = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument(viewModel.exportMimeType()),
                ) { uri ->
                    if (uri != null) {
                        viewModel.onExportLocationChosen(uri.toString())
                    }
                }

                // The system's "open file" dialog, again needing no permission: the app can read only
                // the one file you pick.
                val openDocument = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument(),
                ) { uri ->
                    if (uri != null) {
                        viewModel.onImportFileChosen(uri.toString())
                    }
                }

                CounterScreen(
                    uiState = uiState,
                    onIncrementDay = viewModel::onIncrementDay,
                    onDecrementDay = viewModel::onDecrementDay,
                    onResetRequested = viewModel::onResetRequested,
                    onResetConfirmed = viewModel::onResetConfirmed,
                    onResetDismissed = viewModel::onResetDismissed,
                    onExportRequested = {
                        createDocument.launch(viewModel.suggestedExportFileName())
                    },
                    onExportStatusShown = viewModel::onExportStatusShown,
                    onImportRequested = { openDocument.launch(viewModel.importMimeTypes()) },
                    onImportConfirmed = viewModel::onImportConfirmed,
                    onImportDismissed = viewModel::onImportDismissed,
                    onImportStatusShown = viewModel::onImportStatusShown,
                    onPrivacyPolicyRequested = ::openPrivacyPolicy,
                )
            }
        }
    }

    private fun openPrivacyPolicy() {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, getString(R.string.privacy_policy_url).toUri())
                    .addCategory(Intent.CATEGORY_BROWSABLE),
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.privacy_policy_open_failed, Toast.LENGTH_LONG).show()
        }
    }
}
