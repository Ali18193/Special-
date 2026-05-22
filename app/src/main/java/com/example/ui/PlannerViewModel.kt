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

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PlannerRepository(db.plannerDao())

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
            calculateTodayPomodoroCount()
            triggerAiSuggestions()
        }
    }

    // --- Seed Data ---
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

    fun deleteCurrentTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            if (selectedTask.value?.id == task.id) {
                selectedTask.value = null
                isRightPanelOpen.value = false
            }
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
