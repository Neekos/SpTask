package com.example.spprojectsqlitetask.widgets

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.example.spprojectsqlitetask.R
import com.example.spprojectsqlitetask.data.AppDatabase
import com.example.spprojectsqlitetask.data.Task
import com.example.spprojectsqlitetask.repository.TaskRepository
import kotlinx.coroutines.runBlocking

/**
 * TasksRemoteViewsFactory - фабрика для создания элементов списка в виджете
 */
class TasksRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var tasks = listOf<Task>()

    override fun onCreate() {
        loadTasks()
    }

    override fun onDataSetChanged() {
        loadTasks()
    }

    override fun onDestroy() {}

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)
        val task = tasks[position]

        // Устанавливаем иконку статуса
        val statusIcon = if (task.isComplete) R.drawable.ic_checked else R.drawable.ic_unchecked
        views.setImageViewResource(R.id.widget_task_status, statusIcon)

        // Устанавливаем заголовок
        views.setTextViewText(R.id.widget_task_title, task.title)

        // Устанавливаем уровень сложности
        views.setTextViewText(R.id.widget_task_level, task.level)

        // Устанавливаем цвет бейджа
        val badgeColor = when (task.level) {
            "EASY" -> ContextCompat.getColor(context, R.color.green)
            "MEDIUM" -> ContextCompat.getColor(context, R.color.orange)
            "HARD" -> ContextCompat.getColor(context, R.color.red)
            else -> ContextCompat.getColor(context, R.color.gray)
        }
        views.setInt(R.id.widget_task_level, "setBackgroundColor", badgeColor)

        // Intent для клика
        val fillInIntent = Intent().apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_description", task.description)
            putExtra("task_level", task.level)
            putExtra("task_is_complete", task.isComplete)
        }
        views.setOnClickFillInIntent(R.id.widget_task_item, fillInIntent)

        return views
    }

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = tasks[position].id.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getLoadingView(): RemoteViews? = null

    private fun loadTasks() {
        tasks = runBlocking {
            val database = AppDatabase.getDatabase(context)
            val repository = TaskRepository(database.taskDao())
            repository.getAllTasksOnce()
        }
    }
}