package com.elifnuroksuz.alarmclock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm received")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1
        val channelId = "androidknowledge"

        // Android 8.0 (API 26) ve üstü için bildirim kanalı oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Alarm Channel"
            val channelDescription = "Channel for Alarm Manager"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
            Log.d("AlarmReceiver", "Notification channel created")
        }

        // Bildirim oluştur
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Küçük simgeyi uygun bir ikonla değiştirin
            .setContentTitle("Alarm")
            .setContentText("It's time!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Bildirime tıklanınca otomatik olarak kapatılacak

        // Bildirimi göster
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d("AlarmReceiver", "Notification sent")
    }
}
