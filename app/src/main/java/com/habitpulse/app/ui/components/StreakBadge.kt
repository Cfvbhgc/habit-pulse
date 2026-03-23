package com.habitpulse.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.habitpulse.app.ui.theme.OrangeStreak
import com.habitpulse.app.ui.theme.OrangeStreakLight

/**
 * A compact badge composable that displays a streak count with a fire icon.
 *
 * The badge is designed to be placed alongside habit information to provide
 * a quick visual indicator of consecutive completion days. When the streak
 * is zero, the badge uses muted colors; active streaks use the orange accent.
 *
 * @param streakCount The number of consecutive days in the streak.
 * @param label A short text label displayed below the count (e.g., "Current" or "Best").
 * @param modifier Optional [Modifier] for layout customisation.
 */
@Composable
fun StreakBadge(
    streakCount: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (streakCount > 0) {
        OrangeStreakLight
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val iconTint = if (streakCount > 0) {
        OrangeStreak
    } else {
        MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = "$streakCount day streak",
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = streakCount.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = if (streakCount > 0) OrangeStreak else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
