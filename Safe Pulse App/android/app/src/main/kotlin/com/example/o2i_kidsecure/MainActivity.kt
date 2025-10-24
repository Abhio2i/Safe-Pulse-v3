package com.otoi.safe_pulse

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.content.Context
import android.content.Intent
import io.flutter.embedding.android.FlutterActivity
import com.otoi.safe_pulse.BackgroundService

class MainActivity: FlutterActivity() {

    override fun onResume() {
        super.onResume()
        createNotificationChannel(this) // Create the notification channel before starting service
        val serviceIntent = Intent(this, BackgroundService::class.java)
        startService(serviceIntent)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_service", // Must match ID in Dart
                "Location Service", // Human-readable name
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Used for background location tracking"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
