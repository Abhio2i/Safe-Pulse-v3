package com.otoi.safe_pulse  // Must match your package name

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.otoi.safe_pulse.BackgroundService
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, BackgroundService::class.java)
            context.startService(serviceIntent)
        }
    }
}