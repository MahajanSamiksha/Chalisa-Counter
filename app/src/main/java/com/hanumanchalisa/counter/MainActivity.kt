package com.hanumanchalisa.counter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                )
            }
        }
    }
}
