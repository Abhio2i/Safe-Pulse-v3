// android/app/src/main/kotlin/com/example/Safe_pulse/BackgroundService.kt
package com.otoi.safe_pulse  // Match your app's package name

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor

class BackgroundService : Service() {
    private lateinit var flutterEngine: FlutterEngine
    private val channelId = "location_service"  // Match your Dart configuration

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        flutterEngine = FlutterEngine(this)
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background location tracking"
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking")
            .setContentText("Running in background")
            .setSmallIcon(R.mipmap.ic_launcher) // Ensure this resource exists
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? = null
}