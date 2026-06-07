package com.edumap.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    const val CHANNEL_ID_UPDATES = "edumap_updates"
    const val CHANNEL_ID_FAVORITES = "edumap_favorites"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_UPDATES,
                    "Обновления",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Новые дисциплины и материалы"
                }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_FAVORITES,
                    "Избранное",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Напоминания об избранных дисциплинах"
                }
            )
        }
    }

    fun showUpdateNotification(context: Context, title: String, message: String) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
