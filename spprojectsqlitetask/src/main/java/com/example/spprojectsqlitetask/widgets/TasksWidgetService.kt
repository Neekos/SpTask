package com.example.spprojectsqlitetask.widgets

import android.content.Intent
import android.widget.RemoteViewsService

/**
 * TasksWidgetService - сервис для RemoteViews
 */
class TasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TasksRemoteViewsFactory(applicationContext)
    }
}