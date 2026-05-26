package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val originalText: String,
    val category: String = "Personal", // Work, Study, Personal, Health, Other
    val priority: String = "Medium",  // Low, Medium, High, Urgent
    val estimatedMinutes: Int = 25,
    val timeSlot: String? = null,      // e.g. "14:00"
    val date: String,                  // "YYYY-MM-DD" e.g. "2026-05-22"
    val emoji: String = "📝",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val recurrenceType: String = "None", // None, Daily, Weekly, Monthly
    val totalPomodoros: Int = 0
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,          // Can link to a specific task
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 25
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Personal",
    val streak: Int = 0,
    val isCompletedToday: Boolean = false,
    val emoji: String = "✨"
)
