package com.hanumanchalisa.counter.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanumanchalisa.counter.R
import com.hanumanchalisa.counter.domain.model.DayProgress
import com.hanumanchalisa.counter.presentation.ui.theme.HanumanChalisaTheme

/** Touch target size for the +/- buttons: comfortably above the 48dp accessibility minimum. */
private val ButtonSize = 56.dp

/**
 * One row of the list: `Day N   ( − )  count  ( + )`.
 *
 * A pure display component — it reports taps upward through [onIncrement] / [onDecrement] and never
 * changes the count itself. That keeps the single source of truth in the ViewModel, and lets this
 * row be previewed with any count.
 *
 * The count is display-only text, not a text field: it cannot be edited or cleared by accident, and
 * no keyboard ever appears.
 */
@Composable
fun DayCountRow(
    dayProgress: DayProgress,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Days with recitations are tinted so progress is visible while scrolling.
    val containerColor = if (dayProgress.hasRecitations) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.day_label, dayProgress.day),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            val decrementLabel = stringResource(R.string.decrement_day, dayProgress.day)
            OutlinedIconButton(
                onClick = { onDecrement(dayProgress.day) },
                // Nothing to remove at zero, so the control is disabled rather than silently inert.
                enabled = dayProgress.hasRecitations,
                modifier = Modifier
                    .size(ButtonSize)
                    .semantics { contentDescription = decrementLabel },
            ) {
                Text(
                    text = "−", // MINUS SIGN — visually matches "+" better than a hyphen.
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Box(
                modifier = Modifier.widthIn(min = 52.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = dayProgress.count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            val incrementLabel = stringResource(R.string.increment_day, dayProgress.day)
            FilledIconButton(
                onClick = { onIncrement(dayProgress.day) },
                modifier = Modifier
                    .size(ButtonSize)
                    .semantics { contentDescription = incrementLabel },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DayCountRowPreview() {
    HanumanChalisaTheme {
        DayCountRow(
            dayProgress = DayProgress(day = 3, count = 4),
            onIncrement = {},
            onDecrement = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DayCountRowZeroPreview() {
    HanumanChalisaTheme {
        DayCountRow(
            dayProgress = DayProgress(day = 7, count = 0),
            onIncrement = {},
            onDecrement = {},
        )
    }
}
