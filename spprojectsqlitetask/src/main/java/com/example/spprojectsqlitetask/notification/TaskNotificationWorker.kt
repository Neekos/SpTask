package com.example.spprojectsqlitetask.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.spprojectsqlitetask.data.AppDatabase
import com.example.spprojectsqlitetask.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TaskNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            // Используем runBlocking для работы с suspend функциями в Worker
            val result = runBlocking {
                // Получаем базу данных и репозиторий
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = TaskRepository(database.taskDao())

                // Получаем все задачи через Flow (получаем текущее значение)
                val allTasks = repository.allTasks.first()
                val incompleteCount = allTasks.count { !it.isComplete }

                // Показываем уведомление если есть невыполненные задачи
                if (incompleteCount > 0) {
                    val notificationHelper = NotificationHelper(applicationContext)
                    notificationHelper.showDailyTaskReminder(incompleteCount)
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry() // Пробуем снова при ошибке
        }
    }
}