package com.hanumanchalisa.counter.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanumanchalisa.counter.R
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.model.SadhanaProgress
import com.hanumanchalisa.counter.presentation.ui.theme.HanumanChalisaTheme

/**
 * Fixed header showing the app name, the running total against the target, and a progress bar.
 *
 * Its only job is displaying [progress] — it holds no state and performs no logic, so it can be
 * previewed and reasoned about in isolation.
 */
@Composable
fun ProgressHeader(
    progress: SadhanaProgress,
    modifier: Modifier = Modifier,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = progress.completionFraction,
        label = "progressFraction",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.app_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                // Read out as one phrase ("Total 47 of 100") instead of two loose numbers.
                Text(
                    text = stringResource(
                        R.string.total_of_target,
                        progress.totalCount,
                        progress.targetCount,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription =
                            "Total ${progress.totalCount} of ${progress.targetCount}"
                    },
                )
            }

            LinearProgressIndicator(
                progress = { animatedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )

            Text(
                text = if (progress.isTargetReached) {
                    stringResource(R.string.target_reached)
                } else {
                    pluralStringResource(
                        R.plurals.remaining_count,
                        progress.remainingCount,
                        progress.remainingCount,
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressHeaderPreview() {
    val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS
    HanumanChalisaTheme {
        ProgressHeader(
            progress = SadhanaProgress(
                days = config.dayRange.map { DayProgress(it, if (it <= 12) 4 else 0) },
                config = config,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressHeaderCompletePreview() {
    val config = SadhanaConfig.HANUMAN_CHALISA_40_DAYS
    HanumanChalisaTheme {
        ProgressHeader(
            progress = SadhanaProgress(
                days = config.dayRange.map { DayProgress(it, 3) },
                config = config,
            ),
        )
    }
}
