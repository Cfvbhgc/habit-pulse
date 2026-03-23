package com.habitpulse.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.habitpulse.app.domain.model.Habit
import com.habitpulse.app.ui.theme.GreenSuccess

/**
 * A Material3 card composable that displays a single habit with its completion status.
 *
 * The card shows the habit name and description on the left, and a circular
 * check icon on the right that can be toggled to mark the habit as complete
 * or incomplete for the current day. The check icon animates between green
 * (completed) and outline (incomplete) states.
 *
 * Tapping the card body navigates to the habit detail screen, while tapping
 * the check icon toggles the completion status.
 *
 * @param habit The [Habit] domain model to display.
 * @param onToggleCompletion Callback invoked when the user taps the completion toggle.
 *   Receives the habit ID and the new desired completion state.
 * @param onClick Callback invoked when the user taps the card body (not the toggle).
 * @param modifier Optional [Modifier] for customising the card's layout.
 */
@Composable
fun HabitCard(
    habit: Habit,
    onToggleCompletion: (Long, Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkColor by animateColorAsState(
        targetValue = if (habit.isCompletedToday) GreenSuccess else MaterialTheme.colorScheme.outline,
        animationSpec = tween(durationMillis = 300),
        label = "checkColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = habit.frequency.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            IconButton(
                onClick = { onToggleCompletion(habit.id, !habit.isCompletedToday) }
            ) {
                Icon(
                    imageVector = if (habit.isCompletedToday) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Outlined.Circle
                    },
                    contentDescription = if (habit.isCompletedToday) {
                        "Mark ${habit.name} as incomplete"
                    } else {
                        "Mark ${habit.name} as complete"
                    },
                    tint = checkColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
