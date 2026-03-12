package com.example.spprojectsqlitetask.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.spprojectsqlitetask.MainActivity
import com.example.spprojectsqlitetask.R
import com.example.spprojectsqlitetask.data.AppDatabase
import com.example.spprojectsqlitetask.repository.TaskRepository
import kotlinx.coroutines.runBlocking

/**
 * TasksWidgetProvider - провайдер для виджета задач на главном экране
 */
class TasksWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "ACTION_TASK_CLICK") {
            val taskId = intent.getIntExtra("task_id", -1)
            val taskTitle = intent.getStringExtra("task_title")
            val taskDescription = intent.getStringExtra("task_description")
            val taskLevel = intent.getStringExtra("task_level")
            val taskIsComplete = intent.getBooleanExtra("task_is_complete", false)

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_task_id", taskId)
                putExtra("open_task_title", taskTitle)
                putExtra("open_task_description", taskDescription)
                putExtra("open_task_level", taskLevel)
                putExtra("open_task_is_complete", taskIsComplete)
            }
            context.startActivity(openIntent)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // ИСПРАВЛЕНО: widget_task -> widget_tasks
            val views = RemoteViews(context.packageName, R.layout.widget_task)

            // Настраиваем RemoteViews для списка
            val intent = Intent(context, TasksWidgetService::class.java)
            views.setRemoteAdapter(R.id.widget_list, intent)

            // Устанавливаем пустое view для списка
            views.setEmptyView(R.id.widget_list, android.R.id.empty)

            // Устанавливаем Intent для клика по элементу списка
            val clickIntent = Intent(context, TasksWidgetProvider::class.java).apply {
                action = "ACTION_TASK_CLICK"
            }
            val clickPendingIntent = PendingIntent.getBroadcast(
                context, 0, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntent)

            // Устанавливаем клик для открытия приложения
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_open_app, openAppPendingIntent)

            // Обновляем счетчик задач
            updateTaskCount(context, views)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
        }

        private fun updateTaskCount(context: Context, views: RemoteViews) {
            val count = runBlocking {
                val database = AppDatabase.getDatabase(context)
                val repository = TaskRepository(database.taskDao())
                repository.getAllTasksOnce().size
            }
            views.setTextViewText(R.id.widget_task_count, "$count задач")
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TasksWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}