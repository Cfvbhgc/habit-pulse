package com.habitpulse.app.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.ui.components.StreakBadge
import com.habitpulse.app.ui.components.WeeklyChart
import com.habitpulse.app.ui.theme.GreenSuccess
import com.habitpulse.app.ui.theme.RedMissed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detail screen composable showing a habit's complete information.
 *
 * Displays the habit name, description, frequency, streak badges, a weekly
 * completion chart, and a completion toggle button. Also provides navigation
 * to the edit screen and stats screen, as well as a delete action with
 * confirmation dialog.
 *
 * @param repository The [HabitRepository] used to construct the [HabitDetailViewModel].
 * @param habitId The ID of the habit to display.
 * @param onNavigateBack Callback to navigate back to the previous screen.
 * @param onEditHabit Callback to navigate to the edit screen for this habit.
 * @param onViewStats Callback to navigate to the statistics screen for this habit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    repository: HabitRepository,
    habitId: Long,
    onNavigateBack: () -> Unit,
    onEditHabit: () -> Unit,
    onViewStats: () -> Unit
) {
    val viewModel: HabitDetailViewModel = viewModel {
        HabitDetailViewModel(repository, habitId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is HabitDetailUiState.Deleted) {
            onNavigateBack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Habit?") },
            text = { Text("This will permanently delete this habit and all its history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteHabit()
                    }
                ) {
                    Text("Delete", color = RedMissed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditHabit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit habit"
                        )
                    }
                    IconButton(onClick = onViewStats) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = "View statistics"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete habit",
                            tint = RedMissed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HabitDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is HabitDetailUiState.Success -> {
                val habit = state.habit
                val stats = state.stats

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Habit name and description
                    if (habit != null) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (habit.description.isNotBlank()) {
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${habit.frequency.replaceFirstChar { it.uppercase() }} habit",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )

                        if (habit.reminderTime != null) {
                            Text(
                                text = "Reminder at ${habit.reminderTime}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Completion toggle button
                        Button(
                            onClick = { viewModel.toggleCompletion(!habit.isCompletedToday) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (habit.isCompletedToday) {
                                    GreenSuccess
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        ) {
                            Icon(
                                imageVector = if (habit.isCompletedToday) {
                                    Icons.Filled.CheckCircle
                                } else {
                                    Icons.Outlined.Circle
                                },
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = if (habit.isCompletedToday) {
                                    "Completed Today"
                                } else {
                                    "Mark as Complete"
                                }
                            )
                        }
                    }

                    // Streak badges
                    if (stats != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StreakBadge(
                                streakCount = stats.currentStreak,
                                label = "Current",
                                modifier = Modifier.weight(1f)
                            )
                            StreakBadge(
                                streakCount = stats.bestStreak,
                                label = "Best",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Completion rate card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${(stats.completionRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Completion Rate",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${stats.totalCompletions} total completions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Weekly chart
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "This Week",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                WeeklyChart(weeklyData = stats.weeklyData)
                            }
                        }

                        // View full stats button
                        OutlinedButton(
                            onClick = onViewStats,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("View Full Statistics")
                        }
                    }

                    // Recent completions
                    if (state.recentCompletions.isNotEmpty()) {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                        state.recentCompletions.take(10).forEach { completion ->
                            Text(
                                text = dateFormat.format(Date(completion.dateMillis)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            is HabitDetailUiState.Deleted -> {
                // Navigation handled by LaunchedEffect
            }
        }
    }
}
