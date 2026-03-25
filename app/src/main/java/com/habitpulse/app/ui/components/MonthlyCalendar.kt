package com.habitpulse.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habitpulse.app.ui.theme.ChartCompleted
import com.habitpulse.app.ui.theme.ChartIncomplete
import com.habitpulse.app.ui.theme.ChartToday
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * A Canvas-based calendar heatmap composable displaying the last 30 days of completion data.
 *
 * Renders a grid of rounded squares arranged in rows of 7 (matching a week layout).
 * Each cell represents one day and is coloured to indicate whether the habit was
 * completed (primary colour), missed (grey), or is today (accent).
 *
 * The grid starts from the oldest day (30 days ago) and fills left-to-right,
 * top-to-bottom, with day-of-week headers displayed above the grid.
 *
 * @param monthlyData A list of up to 30 booleans where index 0 is 29 days ago
 *   and the last index is today. `true` indicates the habit was completed.
 * @param modifier Optional [Modifier] for layout customisation.
 */
@Composable
fun MonthlyCalendar(
    monthlyData: List<Boolean>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val startDate = today.minusDays((monthlyData.size - 1).toLong())
    val startDayOfWeek = startDate.dayOfWeek.value % 7 // 0 = Monday

    val dayOfWeekLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = modifier.fillMaxWidth()) {
        // Day-of-week header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            dayOfWeekLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Calendar grid
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 4.dp)
        ) {
            val columns = 7
            val cellSpacing = 4.dp.toPx()
            val cellSize = (size.width - cellSpacing * (columns + 1)) / columns
            val rows = ((monthlyData.size + startDayOfWeek + columns - 1) / columns)

            monthlyData.forEachIndexed { index, isCompleted ->
                val gridIndex = index + startDayOfWeek
                val col = gridIndex % columns
                val row = gridIndex / columns

                val x = cellSpacing + col * (cellSize + cellSpacing)
                val y = row * (cellSize + cellSpacing)

                val isToday = index == monthlyData.lastIndex
                val color = when {
                    isCompleted -> ChartCompleted
                    isToday -> ChartToday.copy(alpha = 0.5f)
                    else -> ChartIncomplete
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}
