package com.example.spprojectsqlitetask.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.spprojectsqlitetask.MainActivity
import com.example.spprojectsqlitetask.R  // Используй свой R, а не android.R

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "daily_tasks_channel"
        private const val CHANNEL_NAME = "Ежедневные напоминания"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ежедневные напоминания о невыполненных задачах"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDailyTaskReminder(incompleteCount: Int) {
        if (incompleteCount == 0) return

        // Создаем Intent для открытия приложения при нажатии на уведомление
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // Исправлено здесь
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Создаем уведомление
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // Оставляем android.R для системных иконок
            .setContentTitle("📋 Напоминание о задачах")
            .setContentText("У вас $incompleteCount невыполненных задач на сегодня")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("У вас $incompleteCount невыполненных задач. Не забудьте их завершить сегодня!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,  // Оставляем android.R для системных иконок
                "Открыть задачи",
                pendingIntent
            )
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}