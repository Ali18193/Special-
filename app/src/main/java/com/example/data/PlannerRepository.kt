package com.example.data

import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val plannerDao: PlannerDao) {

    fun getAllTasks(): Flow<List<Task>> = plannerDao.getAllTasks()

    fun getTasksForDate(date: String): Flow<List<Task>> = plannerDao.getTasksForDate(date)

    suspend fun getTaskById(id: Long): Task? = plannerDao.getTaskById(id)

    suspend fun insertTask(task: Task): Long = plannerDao.insertTask(task)

    suspend fun updateTask(task: Task) = plannerDao.updateTask(task)

    suspend fun deleteTask(task: Task) = plannerDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = plannerDao.deleteTaskById(id)

    fun getSubtasksForTask(taskId: Long): Flow<List<Subtask>> = plannerDao.getSubtasksForTask(taskId)

    suspend fun getSubtasksForTaskSync(taskId: Long): List<Subtask> = plannerDao.getSubtasksForTaskSync(taskId)

    suspend fun insertSubtask(subtask: Subtask): Long = plannerDao.insertSubtask(subtask)

    suspend fun updateSubtask(subtask: Subtask) = plannerDao.updateSubtask(subtask)

    suspend fun deleteSubtask(subtask: Subtask) = plannerDao.deleteSubtask(subtask)

    suspend fun deleteSubtaskById(id: Long) = plannerDao.deleteSubtaskById(id)

    fun getAllPomodoroSessions(): Flow<List<PomodoroSession>> = plannerDao.getAllPomodoroSessions()

    suspend fun insertPomodoroSession(session: PomodoroSession): Long = plannerDao.insertPomodoroSession(session)
}
