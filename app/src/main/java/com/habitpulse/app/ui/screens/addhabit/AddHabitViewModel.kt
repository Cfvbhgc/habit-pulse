package com.habitpulse.app.ui.screens.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.domain.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Add/Edit Habit screen.
 *
 * Manages form state for creating a new habit or editing an existing one.
 * When [habitId] is provided, the ViewModel loads the existing habit data
 * to pre-populate the form fields.
 *
 * @property repository The [HabitRepository] used to persist habit data.
 * @property habitId The ID of the habit to edit, or null when creating a new habit.
 */
class AddHabitViewModel(
    private val repository: HabitRepository,
    private val habitId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddHabitUiState())

    /**
     * The current form state including all editable fields and validation status.
     */
    val uiState: StateFlow<AddHabitUiState> = _uiState.asStateFlow()

    init {
        if (habitId != null) {
            loadHabit(habitId)
        }
    }

    /**
     * Loads an existing habit's data into the form fields.
     *
     * @param id The ID of the habit to load.
     */
    private fun loadHabit(id: Long) {
        viewModelScope.launch {
            repository.observeHabitWithStatus(id).collect { habit ->
                if (habit != null) {
                    _uiState.update {
                        it.copy(
                            name = habit.name,
                            description = habit.description,
                            frequency = habit.frequency,
                            reminderTime = habit.reminderTime ?: "",
                            isEditing = true,
                            isLoaded = true
                        )
                    }
                }
            }
        }
    }

    /**
     * Updates the habit name field.
     *
     * @param name The new name value.
     */
    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    /**
     * Updates the habit description field.
     *
     * @param description The new description value.
     */
    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /**
     * Updates the habit frequency selection.
     *
     * @param frequency The new frequency value ("daily", "weekly", or "monthly").
     */
    fun onFrequencyChanged(frequency: String) {
        _uiState.update { it.copy(frequency = frequency) }
    }

    /**
     * Updates the reminder time field.
     *
     * @param time The new reminder time in "HH:mm" format, or empty to disable reminders.
     */
    fun onReminderTimeChanged(time: String) {
        _uiState.update { it.copy(reminderTime = time) }
    }

    /**
     * Validates the form and saves the habit if validation passes.
     *
     * Performs the following validation:
     * - Name must not be blank.
     *
     * On success, either creates a new habit or updates the existing one,
     * then sets [AddHabitUiState.isSaved] to true to signal the UI to navigate back.
     */
    fun saveHabit() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Habit name is required") }
            return
        }

        viewModelScope.launch {
            val habit = Habit(
                id = habitId ?: 0L,
                name = currentState.name.trim(),
                description = currentState.description.trim(),
                frequency = currentState.frequency,
                reminderTime = currentState.reminderTime.ifBlank { null }
            )

            if (habitId != null) {
                repository.updateHabit(habit)
            } else {
                repository.createHabit(habit)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }
}

/**
 * UI state for the Add/Edit Habit form.
 *
 * @property name The current value of the habit name text field.
 * @property description The current value of the description text field.
 * @property frequency The selected frequency: "daily", "weekly", or "monthly".
 * @property reminderTime The reminder time string in "HH:mm" format, or empty if not set.
 * @property nameError Validation error message for the name field, or null if valid.
 * @property isEditing Whether the form is in edit mode (true) or create mode (false).
 * @property isLoaded Whether existing habit data has been loaded (relevant only when editing).
 * @property isSaved Whether the habit was successfully saved, signalling navigation should occur.
 */
data class AddHabitUiState(
    val name: String = "",
    val description: String = "",
    val frequency: String = "daily",
    val reminderTime: String = "",
    val nameError: String? = null,
    val isEditing: Boolean = false,
    val isLoaded: Boolean = false,
    val isSaved: Boolean = false
)
