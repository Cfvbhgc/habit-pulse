package com.habitpulse.app.ui.screens.addhabit

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitpulse.app.data.repository.HabitRepository

/**
 * Screen composable for creating a new habit or editing an existing one.
 *
 * Provides a form with fields for habit name, description, frequency selection
 * (via radio buttons), and an optional reminder time (via a time picker dialog).
 * The screen title dynamically changes between "New Habit" and "Edit Habit"
 * based on whether a [habitId] is provided.
 *
 * When the user saves successfully, the screen automatically navigates back
 * via the [onNavigateBack] callback.
 *
 * @param repository The [HabitRepository] used to construct the [AddHabitViewModel].
 * @param habitId The ID of the habit to edit, or null to create a new habit.
 * @param onNavigateBack Callback invoked when the user presses back or successfully saves.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    repository: HabitRepository,
    habitId: Long?,
    onNavigateBack: () -> Unit
) {
    val viewModel: AddHabitViewModel = viewModel {
        AddHabitViewModel(repository, habitId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Edit Habit" else "New Habit"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Name field
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Habit Name") },
                placeholder = { Text("e.g., Exercise, Read, Meditate") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { error ->
                    { Text(error) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Description field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description (optional)") },
                placeholder = { Text("Add details about your habit") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Frequency selection
            Text(
                text = "Frequency",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(modifier = Modifier.selectableGroup()) {
                listOf("daily", "weekly", "monthly").forEach { frequency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = uiState.frequency == frequency,
                                onClick = { viewModel.onFrequencyChanged(frequency) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.frequency == frequency,
                            onClick = null
                        )
                        Text(
                            text = frequency.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Reminder time
            Text(
                text = "Reminder Time",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.reminderTime,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Tap to set time") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "Select reminder time"
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                TextButton(
                    onClick = {
                        val hour = if (uiState.reminderTime.isNotBlank()) {
                            uiState.reminderTime.split(":").firstOrNull()?.toIntOrNull() ?: 9
                        } else 9
                        val minute = if (uiState.reminderTime.isNotBlank()) {
                            uiState.reminderTime.split(":").lastOrNull()?.toIntOrNull() ?: 0
                        } else 0

                        TimePickerDialog(
                            context,
                            { _, selectedHour, selectedMinute ->
                                val formattedTime = String.format(
                                    "%02d:%02d",
                                    selectedHour,
                                    selectedMinute
                                )
                                viewModel.onReminderTimeChanged(formattedTime)
                            },
                            hour,
                            minute,
                            true
                        ).show()
                    }
                ) {
                    Text("Set")
                }

                if (uiState.reminderTime.isNotBlank()) {
                    TextButton(
                        onClick = { viewModel.onReminderTimeChanged("") }
                    ) {
                        Text("Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = viewModel::saveHabit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.isEditing) "Update Habit" else "Create Habit",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
