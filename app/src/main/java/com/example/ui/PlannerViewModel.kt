package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.ExtractedTask
import com.example.api.GeminiService
import com.example.data.*
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

sealed class SnackbarEvent {
    data class Show(
        val message: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    ) : SnackbarEvent()
}

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PlannerRepository(db.plannerDao())

    // --- Language Translation state ---
    val selectedLanguage = MutableStateFlow("EN") // EN or AZ

    // --- Snackbar flow ---
    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    fun showSnackbar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        viewModelScope.launch {
            _snackbarEvent.emit(SnackbarEvent.Show(message, actionLabel, onAction))
        }
    }

    // --- Preferences Keys / Mock Preferences for Applet simplicity ---
    private var customApiKey: String? = null
    var appTheme = MutableStateFlow(AppTheme.LIGHT)
        private set

    // --- Gemini Service ---
    private val geminiService = GeminiService { customApiKey }

    // --- UI Navigation & Focus ---
    val currentTab = MutableStateFlow("Today") // Today, Upcoming, Calendar, Stats, Settings
    val selectedDate = MutableStateFlow("2026-05-22") // YYYY-MM-DD format (Starts with meta time: 2026-05-22)
    val selectedTask = MutableStateFlow<Task?>(null)
    val isRightPanelOpen = MutableStateFlow(false)

    // --- Reactive Data Flows ---
    val allTasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTasks: StateFlow<List<Task>> = selectedDate
        .flatMapLatest { date -> repository.getTasksForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPomodoros: StateFlow<List<PomodoroSession>> = repository.getAllPomodoroSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subtasks for the currently selected task
    val currentSubtasks = selectedTask
        .flatMapLatest { task ->
            if (task != null) {
                repository.getSubtasksForTask(task.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Pomodoro State ---
    val pomodoroMinutes = MutableStateFlow(25) // presets: 25, 5, 15
    val pomodoroSecondsLeft = MutableStateFlow(25 * 60)
    val isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroFocusMode = MutableStateFlow(false)
    val pomodoroGoal = MutableStateFlow(4) // default daily goal: 4 sessions
    val pomodoroLoggedCompleted = MutableStateFlow(0)
    val autoStartNext = MutableStateFlow(false)
    val pomodoroTimerMode = MutableStateFlow("Work") // Work, Short Break, Long Break

    private var timerJob: Job? = null

    // --- AI Assist State ---
    val isAiParsing = MutableStateFlow(false)
    val aiResponseFeedback = MutableStateFlow<String?>(null)
    val aiDailyReviewText = MutableStateFlow<String?>(null)
    val aiSmartSuggestionsText = MutableStateFlow<String?>(null)

    init {
        // Hydrate some premium mock/seed data on empty DB to show impressive screens instantly
        viewModelScope.launch {
            allTasks.first { true } // await load
            if (allTasks.value.isEmpty()) {
                seedInitialData()
            }
            allHabits.first { true } // await habits load
            if (allHabits.value.isEmpty()) {
                seedInitialHabits()
            }
            calculateTodayPomodoroCount()
            triggerAiSuggestions()
        }
    }

    // --- Seed Data ---
    private suspend fun seedInitialHabits() {
        val seedHabits = listOf(
            Habit(title = "Drink 8 glasses of water", category = "Health", streak = 5, isCompletedToday = false, emoji = "💧"),
            Habit(title = "Read 15 pages", category = "Study", streak = 12, isCompletedToday = true, emoji = "📖"),
            Habit(title = "Morning Meditation", category = "Personal", streak = 3, isCompletedToday = false, emoji = "🧘‍♂️"),
            Habit(title = "Cardio Exercise", category = "Health", streak = 0, isCompletedToday = false, emoji = "🏃‍♂️")
        )
        for (h in seedHabits) {
            repository.insertHabit(h)
        }
    }

    private suspend fun seedInitialData() {
        val today = "2026-05-22"
        val tomorrow = "2026-05-23"
        val dayAfter = "2026-05-24"

        val seedTasks = listOf(
            Task(title = "FlowPlan Design System", originalText = "Design the premium Notion-like dark layout components", category = "Work", priority = "High", estimatedMinutes = 45, date = today, emoji = "🎨"),
            Task(title = "Calculus II Integrals", originalText = "Review standard integration techniques and do homework exercises", category = "Study", priority = "Urgent", estimatedMinutes = 60, date = today, emoji = "📚"),
            Task(title = "Call Mom", originalText = "Give Mom a quick ring in the evening to catch up", category = "Personal", priority = "Medium", estimatedMinutes = 15, date = today, emoji = "📞"),
            Task(title = "Cardio Workout", originalText = "Go for a light 5K run in the park", category = "Health", priority = "Low", estimatedMinutes = 30, date = today, emoji = "🏃‍♂️"),
            Task(title = "Database Schema Spec", originalText = "Finalize Postgres SQL migrations for production team", category = "Work", priority = "High", estimatedMinutes = 45, date = tomorrow, emoji = "🗄️"),
            Task(title = "Groceries shopping list", originalText = "Pick up organic milk, avocados, whole wheat bread", category = "Personal", priority = "Low", estimatedMinutes = 20, date = tomorrow, emoji = "🛒"),
            Task(title = "Read 3 Chapters", originalText = "Read atomic habits study and review key productivity tricks", category = "Study", priority = "Medium", estimatedMinutes = 40, date = dayAfter, emoji = "📖")
        )

        for (t in seedTasks) {
            val taskId = repository.insertTask(t)
            if (t.title.contains("Design System")) {
                repository.insertSubtask(Subtask(taskId = taskId, title = "Define Color Scheme tokens"))
                repository.insertSubtask(Subtask(taskId = taskId, title = "Mock glassmorphic surfaces"))
                repository.insertSubtask(Subtask(taskId = taskId, title = "Double-check 48dp touch sizes"))
            } else if (t.title.contains("Integrals")) {
                repository.insertSubtask(Subtask(taskId = taskId, title = "Complete chapter 4 review"))
                repository.insertSubtask(Subtask(taskId = taskId, title = "Verify math examples"))
            }
        }
    }

    // --- Task Actions ---
    fun selectDate(date: String) {
        selectedDate.value = date
    }

    fun selectTask(task: Task?) {
        selectedTask.value = task
        if (task != null) {
            isRightPanelOpen.value = true
        }
    }

    fun toggleRightPanel() {
        isRightPanelOpen.value = !isRightPanelOpen.value
    }

    fun closeRightPanel() {
        isRightPanelOpen.value = false
    }

    fun changeTheme(theme: AppTheme) {
        appTheme.value = theme
    }

    fun setCustomApiKey(key: String) {
        customApiKey = if (key.isBlank()) null else key
        triggerAiSuggestions()
    }

    fun getCustomApiKey(): String {
        return customApiKey ?: ""
    }

    fun addTaskFast(title: String, category: String, priority: String, estMinutes: Int, timeSlot: String?, emoji: String) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                originalText = "Fast addition: $title",
                category = category,
                priority = priority,
                estimatedMinutes = estMinutes,
                timeSlot = timeSlot,
                date = selectedDate.value,
                emoji = emoji
            )
            repository.insertTask(task)
        }
    }

    /**
     * Parse raw unstructured natural language and insert tasks dynamically.
     */
    fun parseAndAddTask(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            isAiParsing.value = true
            aiResponseFeedback.value = "Analyzing note text..."
            try {
                val extractedList = geminiService.parseTask(input, selectedDate.value)
                for (extracted in extractedList) {
                    val task = Task(
                        title = extracted.title,
                        originalText = input,
                        category = extracted.category,
                        priority = extracted.priority,
                        estimatedMinutes = extracted.estimatedMinutes,
                        timeSlot = extracted.timeSlot,
                        date = extracted.date,
                        emoji = extracted.emoji
                    )
                    repository.insertTask(task)
                }
                aiResponseFeedback.value = "Extracted ${extractedList.size} task(s) successfully!"
            } catch (e: Exception) {
                aiResponseFeedback.value = "Parsing error: ${e.message}. Quick added."
                // Fallback quick insert on catastrophical crash
                addTaskFast(
                    title = input.take(30),
                    category = "Personal",
                    priority = "Medium",
                    estMinutes = 25,
                    timeSlot = null,
                    emoji = "📝"
                )
            } finally {
                isAiParsing.value = false
                delay(3000)
                aiResponseFeedback.value = null
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
            // If selected, update details container
            if (selectedTask.value?.id == task.id) {
                selectedTask.value = updated
            }
        }
    }

    fun updateTaskDetails(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            if (selectedTask.value?.id == task.id) {
                selectedTask.value = task
            }
        }
    }

    // --- Task Deletion states for Undo ---
    private var recentlyDeletedTask: Task? = null
    private var recentlyDeletedSubtasks: List<Subtask> = emptyList()
    private var recentlyClearedTasks: List<Task> = emptyList()

    fun deleteCurrentTask(task: Task) {
        viewModelScope.launch {
            // Save state for undo
            recentlyDeletedTask = task
            recentlyDeletedSubtasks = repository.getSubtasksForTaskSync(task.id)
            
            repository.deleteTask(task)
            if (selectedTask.value?.id == task.id) {
                selectedTask.value = null
                isRightPanelOpen.value = false
            }

            // Fire undo snackbar
            val msg = Translations.get("task_deleted_msg", selectedLanguage.value).format(task.title)
            val undoLbl = Translations.get("undo", selectedLanguage.value)
            showSnackbar(
                message = msg,
                actionLabel = undoLbl,
                onAction = {
                    undoTaskDelete()
                }
            )
        }
    }

    fun undoTaskDelete() {
        val task = recentlyDeletedTask ?: return
        viewModelScope.launch {
            val newId = repository.insertTask(task.copy(id = 0))
            recentlyDeletedSubtasks.forEach { sub ->
                repository.insertSubtask(sub.copy(id = 0, taskId = newId))
            }
            recentlyDeletedTask = null
            recentlyDeletedSubtasks = emptyList()
        }
    }

    fun clearCompletedTodayTasks() {
        viewModelScope.launch {
            val completed = todayTasks.value.filter { it.isCompleted }
            if (completed.isEmpty()) return@launch
            
            recentlyClearedTasks = completed
            completed.forEach {
                repository.deleteTask(it)
            }
            
            val msg = Translations.get("completed_cleared_msg", selectedLanguage.value)
            val undoLbl = Translations.get("undo", selectedLanguage.value)
            showSnackbar(
                message = msg,
                actionLabel = undoLbl,
                onAction = {
                    undoClearCompletedTasks()
                }
            )
        }
    }

    fun undoClearCompletedTasks() {
        val cleared = recentlyClearedTasks
        if (cleared.isEmpty()) return
        viewModelScope.launch {
            cleared.forEach { task ->
                repository.insertTask(task.copy(id = 0))
            }
            recentlyClearedTasks = emptyList()
        }
    }

    // --- Habit Actions & State for Undo ---
    private var recentlyDeletedHabit: Habit? = null

    fun addHabit(title: String, category: String, emoji: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val h = Habit(title = title, category = category, emoji = emoji, streak = 0, isCompletedToday = false)
            repository.insertHabit(h)
        }
    }

    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            val nextCompleted = !habit.isCompletedToday
            val nextStreak = if (nextCompleted) habit.streak + 1 else (habit.streak - 1).coerceAtLeast(0)
            repository.updateHabit(habit.copy(isCompletedToday = nextCompleted, streak = nextStreak))
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            recentlyDeletedHabit = habit
            repository.deleteHabit(habit)
            
            val msg = Translations.get("habit_deleted_msg", selectedLanguage.value).format(habit.title)
            val undoLbl = Translations.get("undo", selectedLanguage.value)
            showSnackbar(
                message = msg,
                actionLabel = undoLbl,
                onAction = {
                    undoHabitDelete()
                }
            )
        }
    }

    fun undoHabitDelete() {
        val habit = recentlyDeletedHabit ?: return
        viewModelScope.launch {
            repository.insertHabit(habit.copy(id = 0))
            recentlyDeletedHabit = null
        }
    }

    fun rescheduleTask(task: Task, newDate: String) {
        viewModelScope.launch {
            val updated = task.copy(date = newDate)
            repository.updateTask(updated)
            if (selectedTask.value?.id == task.id) {
                selectedTask.value = updated
            }
        }
    }

    // --- Subtask Actions ---
    fun addSubtask(title: String) {
        val task = selectedTask.value ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            val sub = Subtask(taskId = task.id, title = title)
            repository.insertSubtask(sub)
        }
    }

    fun toggleSubtaskCompletion(subtask: Subtask) {
        viewModelScope.launch {
            repository.updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted))
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.deleteSubtask(subtask)
        }
    }

    // --- Pomodoro Ticker Engine ---

    fun configurePomodoro(minutes: Int, mode: String) {
        stopPomodoro()
        pomodoroMinutes.value = minutes
        pomodoroSecondsLeft.value = minutes * 60
        pomodoroTimerMode.value = mode
    }

    fun startPomodoro() {
        if (isPomodoroRunning.value) return
        isPomodoroRunning.value = true
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive && pomodoroSecondsLeft.value > 0) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    pomodoroSecondsLeft.value -= 1
                }
            }
            if (pomodoroSecondsLeft.value == 0) {
                withContext(Dispatchers.Main) {
                    completePomodoroSession()
                }
            }
        }
    }

    fun pausePomodoro() {
        stopPomodoro()
    }

    fun togglePomodoroFocusMode() {
        isPomodoroFocusMode.value = !isPomodoroFocusMode.value
    }

    private fun stopPomodoro() {
        isPomodoroRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    private suspend fun completePomodoroSession() {
        stopPomodoro()
        val currentTaskSelected = selectedTask.value
        val minutesWorked = pomodoroMinutes.value

        // Log to database
        val session = PomodoroSession(
            taskId = currentTaskSelected?.id,
            durationMinutes = minutesWorked
        )
        repository.insertPomodoroSession(session)

        // Increment current selected task totals
        if (currentTaskSelected != null) {
            val updated = currentTaskSelected.copy(
                totalPomodoros = currentTaskSelected.totalPomodoros + 1
            )
            repository.updateTask(updated)
            selectedTask.value = updated
        }

        calculateTodayPomodoroCount()

        // Handle auto-start transition or breaks
        if (pomodoroTimerMode.value == "Work") {
            // Worked completed. Recommend a break!
            configurePomodoro(5, "Short Break")
            if (autoStartNext.value) {
                startPomodoro()
            }
        } else {
            // Break completed. Go back to work!
            configurePomodoro(25, "Work")
            if (autoStartNext.value) {
                startPomodoro()
            }
        }
    }

    private fun calculateTodayPomodoroCount() {
        viewModelScope.launch {
            allPomodoros.collectLatest { list ->
                // Basic check for today meta timestamp range
                val cal = Calendar.getInstance()
                // Simple counts for demonstration (simplistic filtering on latest logs)
                pomodoroLoggedCompleted.value = list.size
            }
        }
    }

    // --- AI Suggestions & Summary Triggers ---

    fun triggerAiSuggestions() {
        viewModelScope.launch {
            val historySummaryInput = "User completed ${allPomodoros.value.size} Pomodoros across multiple categories."
            aiSmartSuggestionsText.value = "Analyzing performance..."
            val recommendation = geminiService.generateSuggestions(historySummaryInput)
            aiSmartSuggestionsText.value = recommendation
        }
    }

    fun generateDailyReview() {
        viewModelScope.launch {
            val currentTasks = todayTasks.value
            val completed = currentTasks.filter { it.isCompleted }
            val completedCount = completed.size
            val missed = currentTasks.filter { !it.isCompleted }
            val missedCount = missed.size

            val completedStr = completed.joinToString { it.title }
            val missedStr = missed.joinToString { it.title }

            aiDailyReviewText.value = "AI summarizing your day..."
            val review = geminiService.generateDailySummary(
                completedCount = completedCount,
                missedCount = missedCount,
                completedTasksStr = completedStr,
                missedTasksStr = missedStr
            )
            aiDailyReviewText.value = review
        }
    }

    // Bulk action helpers
    fun bulkDelete(items: List<Task>) {
        viewModelScope.launch {
            for (t in items) {
                repository.deleteTask(t)
            }
        }
    }

    fun bulkComplete(items: List<Task>) {
        viewModelScope.launch {
            for (t in items) {
                repository.updateTask(t.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
            }
        }
    }

    fun archiveCompletedTasks(daysAgo: Int) {
        viewModelScope.launch {
            // Simple helper to remove older completed tasks
            val cutoff = System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000L)
            val outdated = allTasks.value.filter { it.isCompleted && (it.completedAt ?: 0L) < cutoff }
            for (t in outdated) {
                repository.deleteTask(t)
            }
        }
    }
}
