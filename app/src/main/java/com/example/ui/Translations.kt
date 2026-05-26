package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

object Translations {
    private val en = mapOf(
        // Navigation & General
        "today" to "Today",
        "upcoming" to "Upcoming",
        "calendar" to "Calendar",
        "exam" to "Exam",
        "stats" to "Stats",
        "settings" to "Settings",
        "habits" to "Habits",
        "app_name" to "FlowPlan AI",
        "save" to "Save",
        "cancel" to "Cancel",
        "delete" to "Delete",
        "edit" to "Edit",
        "add" to "Add",
        "back" to "Back",
        "undo" to "Undo",
        "completed" to "Completed",
        "active" to "Active",

        // Today view
        "focus_target" to "Focus Target: %s",
        "completed_ratio" to "Completed %d / %d tasks today",
        "end_of_day_review" to "End of Day Review",
        "review_progress" to "Review Progress",
        "inbox_clear" to "Inbox clear! Try writing or parsing tasks.",
        "clear_completed_tasks" to "Clear Completed",
        "completed_cleared_msg" to "All completed tasks cleared!",
        "task_deleted_msg" to "Task \"%s\" deleted!",
        "habit_deleted_msg" to "Habit \"%s\" deleted!",

        // Add task / Parser
        "parse_placeholder" to "Type a task (e.g., Study Math at 4pm tomorrow for 60m)...",
        "quick_add" to "Quick Add Task",
        "task_title" to "Task Title",
        "category" to "Category",
        "priority" to "Priority",
        "duration" to "Duration (minutes)",
        "time_slot" to "Time Slot (HH:MM)",
        "save_task" to "Save Task",

        // Task details
        "task_details" to "Task Details",
        "subtasks" to "Subtasks",
        "add_subtask_placeholder" to "Add subtask...",
        "timer" to "Timer",
        "start_work" to "Start Work",
        "pause" to "Pause",
        "resume" to "Resume",
        "reset" to "Reset",
        "pomodoros" to "Pomodoros",
        "recurrence" to "Recurrence",
        "schedule" to "Schedule",

        // Settings view
        "configuration" to "Workspace Configuration",
        "fine_tune" to "Fine-tune AI behavior and Pomodoro target rates.",
        "api_connection" to "Gemini API Connection",
        "api_desc" to "Enter your custom Gemini API Key to enable raw natural language parsing. If left empty, the system automatically uses the preconfigured key or fallback local regex parser.",
        "connect_api" to "Connect API",
        "system_prefs" to "System Preferences",
        "sessions_target" to "Daily Pomodoro sessions target",
        "task_archive" to "Automatic Task Archive",
        "archive_desc" to "Delete completed tasks older than 7 days",
        "archive_now" to "Archive Now",
        "save_prefs" to "Save System Preferences",
        "save_success" to "Configuration successfully saved!",
        "archive_success" to "Older completed tasks archived!",
        "prefs_success" to "Preferences successfully updated!",
        "verified_account" to "FlowPlan Premium Account",
        "language" to "System Language",
        "lang_desc" to "Select between English and Azerbaijani Latin layout.",

        // Habits View
        "habit_tracker" to "Habit Tracker",
        "habit_desc" to "Build small daily actions into permanent routines.",
        "add_habit" to "Create Habit",
        "habit_title_placeholder" to "Habit title (e.g. Morning Meditation)",
        "empty_habits" to "No habits configured yet. Create one above!",
        "streak_days" to "%d Days Streak",
        "habit_done" to "Done",

        // Realtime Countdown (Exam)
        "nearest_exam" to "Countdown for nearest exam",
        "exam_date" to "Date: %s",
        "days_left_singular" to "1 day left",
        "days_left_plural" to "%d days left"
    )

    private val az = mapOf(
        // Navigation & General
        "today" to "Bugün",
        "upcoming" to "Gələcək",
        "calendar" to "Təqvim",
        "exam" to "İmtahanlar",
        "stats" to "Statistika",
        "settings" to "Ayarlar",
        "habits" to "Vərdişlər",
        "app_name" to "FlowPlan AI",
        "save" to "Saxla",
        "cancel" to "Ləğv et",
        "delete" to "Sil",
        "edit" to "Düzəliş et",
        "add" to "Əlavə et",
        "back" to "Geri",
        "undo" to "Geri al",
        "completed" to "Tamamlandı",
        "active" to "Aktiv",

        // Today view
        "focus_target" to "Hədəf Günü: %s",
        "completed_ratio" to "Bugün %d / %d tapşırıq tamamlandı",
        "end_of_day_review" to "Günün Sonunda İcmal",
        "review_progress" to "Tərəqqini İcmal Et",
        "inbox_clear" to "Gələnlər qutusu təmizdir! Yeni tapşırıqlar əlavə edin.",
        "clear_completed_tasks" to "Tamamlananları Sil",
        "completed_cleared_msg" to "Bütün tamamlanmış tapşırıqlar təmizləndi!",
        "task_deleted_msg" to "\"%s\" tapşırığı silindi!",
        "habit_deleted_msg" to "\"%s\" vərdişi silindi!",

        // Add task / Parser
        "parse_placeholder" to "Tapşırıq yazın (məs., Sabah saat 4-də Riyaziyyat dərsi 60 dəq)...",
        "quick_add" to "Sürətli Tapşırıq Əlavəsi",
        "task_title" to "Tapşırıq Başlığı",
        "category" to "Kateqoriya",
        "priority" to "Prioritet",
        "duration" to "Müddət (dəqiqə)",
        "time_slot" to "Vaxt Aralığı (SS:DD)",
        "save_task" to "Tapşırığı Saxla",

        // Task details
        "task_details" to "Tapşırıq Detalları",
        "subtasks" to "Yarım-tapşırıqlar",
        "add_subtask_placeholder" to "Yarım-tapşırıq əlavə et...",
        "timer" to "Taymer",
        "start_work" to "İşi Başlat",
        "pause" to "Duraklat",
        "resume" to "Davam et",
        "reset" to "Sıfırla",
        "pomodoros" to "Pomodorolar",
        "recurrence" to "Təkrarlanma",
        "schedule" to "Cədvəl",

        // Settings view
        "configuration" to "İş Sahəsi Ayarları",
        "fine_tune" to "AI davranışını və Pomodoro hədəflərini tənzimləyin.",
        "api_connection" to "Gemini API Bağlantısı",
        "api_desc" to "Təbii dildə analiz üçün fərdi Gemini API Key daxil edin. Boş qoyulduqda, sistem daxili açardan və ya lokal analizdən istifadə edəcək.",
        "connect_api" to "API-ya Bağlan",
        "system_prefs" to "Sistem Üstünlükləri",
        "sessions_target" to "Günlük Pomodoro sessiyası hədəfi",
        "task_archive" to "Avtomatik Tapşırıq Arxivi",
        "archive_desc" to "7 gündən köhnə tamamlanmış tapşırıqları sil",
        "archive_now" to "İndi Arxivlə",
        "save_prefs" to "Sistem Üstünlüklərini Saxla",
        "save_success" to "Ayarlar uğurla saxlanıldı!",
        "archive_success" to "Köhnə tamamlanmış tapşırıqlar arxivləndi!",
        "prefs_success" to "Sistem üstünlükləri uğurla yeniləndi!",
        "verified_account" to "FlowPlan Premium Hesabı",
        "language" to "Sistem Dili",
        "lang_desc" to "İngilis və ya Azərbaycan dilləri arasında seçim edin.",

        // Habits View
        "habit_tracker" to "Vərdiş İzləyicisi",
        "habit_desc" to "Kiçik günlük addımları daimi vərdişlərə çevirin.",
        "add_habit" to "Vərdiş Yarat",
        "habit_title_placeholder" to "Vərdiş başlığı (məs. Səhər Meditasiyası)",
        "empty_habits" to "Hələ vərdiş yaradılmayıb. Yuxarıdan yenisini əlavə edin!",
        "streak_days" to "%d Gün Seriya",
        "habit_done" to "Hazır",

        // Realtime Countdown (Exam)
        "nearest_exam" to "Ən yaxın imtahana qalan vaxt",
        "exam_date" to "Tarix: %s",
        "days_left_singular" to "1 gün qaldı",
        "days_left_plural" to "%d gün qaldı"
    )

    fun get(key: String, lang: String): String {
        val dict = if (lang.equals("AZ", ignoreCase = true)) az else en
        return dict[key] ?: en[key] ?: key
    }
}

@Composable
fun stringResource(key: String, viewModel: PlannerViewModel): String {
    val lang by viewModel.selectedLanguage.collectAsState()
    return Translations.get(key, lang)
}
