package com.example.dailyreminderyarn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNotification(reminder: Reminder) {
        // Check for exact alarm permission on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // If we don't have permission, just ask the user (or show a toast)
                // For this simple app, we'll just toast and return
                Toast.makeText(context, "Please allow Exact Alarms in settings", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("REMINDER_DESC", reminder.description)
        }

        // Unique ID (using reminder.id) so we don't overwrite other alarms
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm
        // setExactAndAllowWhileIdle ensures it rings even in Doze mode
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.timeInMillis,
            pendingIntent
        )

        Toast.makeText(context, "Reminder Scheduled!", Toast.LENGTH_SHORT).show()
    }
}
