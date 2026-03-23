package com.habitpulse.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.ui.components.HabitCard

/**
 * Main home screen composable displaying today's habits with completion toggles.
 *
 * Shows a list of all habits with their current-day completion status. Each habit
 * is rendered as a [HabitCard] that can be tapped to view details or toggled
 * to mark as complete. A floating action button allows creating new habits.
 *
 * When no habits exist, an empty state message guides the user to create their first habit.
 * A progress indicator at the top shows overall completion progress for the day.
 *
 * @param repository The [HabitRepository] used to construct the [HomeViewModel].
 * @param onAddHabit Callback invoked when the user taps the floating action button.
 * @param onHabitClick Callback invoked when the user taps a habit card, receiving the habit ID.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: HabitRepository,
    onAddHabit: () -> Unit,
    onHabitClick: (Long) -> Unit
) {
    val viewModel: HomeViewModel = viewModel {
        HomeViewModel(repository)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HabitPulse",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add new habit",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeUiState.Success -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (state.habits.isEmpty()) {
                        EmptyState(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        )
                    } else {
                        HabitList(
                            habits = state.habits,
                            onToggleCompletion = viewModel::toggleCompletion,
                            onHabitClick = onHabitClick,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays the list of habits with an overall progress indicator.
 *
 * @param habits The list of habits to display.
 * @param onToggleCompletion Callback for toggling a habit's completion status.
 * @param onHabitClick Callback for navigating to habit details.
 * @param modifier Optional layout modifier.
 */
@Composable
private fun HabitList(
    habits: List<com.habitpulse.app.domain.model.Habit>,
    onToggleCompletion: (Long, Boolean) -> Unit,
    onHabitClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = habits.count { it.isCompletedToday }
    val totalCount = habits.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            DailyProgressHeader(
                completedCount = completedCount,
                totalCount = totalCount,
                progress = progress
            )
        }

        items(
            items = habits,
            key = { it.id }
        ) { habit ->
            HabitCard(
                habit = habit,
                onToggleCompletion = onToggleCompletion,
                onClick = { onHabitClick(habit.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Header showing daily completion progress as a fraction and linear progress bar.
 *
 * @param completedCount Number of habits completed today.
 * @param totalCount Total number of habits.
 * @param progress Completion fraction between 0 and 1.
 */
@Composable
private fun DailyProgressHeader(
    completedCount: Int,
    totalCount: Int,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Today's Progress",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$completedCount of $totalCount completed",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Empty state displayed when no habits have been created yet.
 *
 * @param modifier Layout modifier, typically filling the screen.
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No habits yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap + to add your first habit!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
