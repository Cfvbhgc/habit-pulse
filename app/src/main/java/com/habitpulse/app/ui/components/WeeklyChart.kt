package com.habitpulse.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habitpulse.app.ui.theme.ChartCompleted
import com.habitpulse.app.ui.theme.ChartIncomplete
import com.habitpulse.app.ui.theme.ChartToday
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * A Canvas-based bar chart composable that displays weekly habit completion data.
 *
 * Renders 7 vertical bars representing the last 7 days. Each bar is coloured
 * based on whether the habit was completed that day:
 * - **Completed**: Uses the primary chart colour.
 * - **Today (incomplete)**: Uses an accent colour to draw attention.
 * - **Incomplete**: Uses a muted grey.
 *
 * Day-of-week labels are displayed below each bar using the device locale.
 *
 * @param weeklyData A list of exactly 7 booleans where index 0 is 6 days ago
 *   and index 6 is today. `true` indicates the habit was completed.
 * @param modifier Optional [Modifier] for layout customisation.
 */
@Composable
fun WeeklyChart(
    weeklyData: List<Boolean>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val dayLabels = (6 downTo 0).map { daysAgo ->
        today.minusDays(daysAgo.toLong())
            .dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val barCount = weeklyData.size
            val spacing = 12.dp.toPx()
            val totalSpacing = spacing * (barCount + 1)
            val barWidth = (size.width - totalSpacing) / barCount
            val maxBarHeight = size.height - 8.dp.toPx()

            weeklyData.forEachIndexed { index, isCompleted ->
                val x = spacing + index * (barWidth + spacing)
                val barHeight = if (isCompleted) maxBarHeight else maxBarHeight * 0.3f
                val y = size.height - barHeight

                val color = when {
                    isCompleted -> ChartCompleted
                    index == weeklyData.lastIndex -> ChartToday
                    else -> ChartIncomplete
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
