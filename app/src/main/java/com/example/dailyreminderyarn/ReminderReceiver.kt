package com.example.dailyreminderyarn

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        // --- HANDLE SNOOZE ---
        if (action == "SNOOZE_ACTION") {
            val title = intent.getStringExtra("REMINDER_TITLE") ?: ""
            val id = intent.getIntExtra("REMINDER_ID", 0)
            // Get the duration from the button, default to 10 mins (600,000ms) if missing
            val snoozeDurationMs = intent.getLongExtra("SNOOZE_DURATION", 600_000)

            // Calculate new time
            val snoozeTime = System.currentTimeMillis() + snoozeDurationMs

            // PRO TEST TOAST
            val minutes = snoozeDurationMs / 60000
            Toast.makeText(context, "Snoozing for $minutes minutes...", Toast.LENGTH_SHORT).show()

            val newIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("REMINDER_TITLE", title)
                putExtra("REMINDER_ID", id)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id, // Use same ID
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
            }

            // Cancel the notification so it stops ringing/showing
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(id)

            return
        }

        // --- NORMAL ALARM ---
        val title = intent.getStringExtra("REMINDER_TITLE") ?: "Reminder"
        val id = intent.getIntExtra("REMINDER_ID", 0)

        // PRO TEST: VISUAL DEBUGGING
        Toast.makeText(context, "ALARM TRIGGERED: $title", Toast.LENGTH_LONG).show()

        showNotification(context, title, "Time for your task!", id)
    }

    private fun showNotification(context: Context, title: String, desc: String, id: Int) {
        // NEW CHANNEL ID: Forces the phone to apply new sound settings
        val channelId = "daily_reminder_alarm_sound"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. DEFINE THE SOUND (System Alarm Sound)
        val soundUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if channel exists first to avoid resetting it constantly
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Daily Reminders (Loud)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    // 2. SET THE SOUND ON THE CHANNEL
                    setSound(soundUri, android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    )
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        // --- HELPER TO CREATE SNOOZE BUTTONS ---
        fun createSnoozePendingIntent(durationMinutes: Int): PendingIntent {
            val durationMs = durationMinutes * 60 * 1000L // Convert mins to milliseconds
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "SNOOZE_ACTION"
                putExtra("REMINDER_TITLE", title)
                putExtra("REMINDER_ID", id)
                putExtra("SNOOZE_DURATION", durationMs) // Pass the custom time
            }
            // IMPORTANT: RequestCode must be unique per button (id + duration) so they don't overwrite each other
            return PendingIntent.getBroadcast(
                context,
                id + durationMinutes,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Build Notification with 3 options
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri) // Set sound here for older Android versions
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 5m", createSnoozePendingIntent(5))
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 10m", createSnoozePendingIntent(10))
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 15m", createSnoozePendingIntent(15))
            .build()

        notificationManager.notify(id, notification)
    }
}
