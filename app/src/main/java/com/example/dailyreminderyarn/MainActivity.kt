package com.example.dailyreminderyarn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.room.Room
import com.example.dailyreminderyarn.ui.theme.DailyReminderYarnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1. ASK FOR PERMISSION (Android 13+) ---
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            }
        }

        // --- 2. START THE UI ---
        setContent {
            DailyReminderYarnTheme {
                // Setup Database
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "reminder-database"
                ).allowMainThreadQueries().build()

                // Setup Scheduler
                val scheduler = NotificationScheduler(applicationContext)

                // Show App
                ReminderApp(db, scheduler)
            }
        }
    }
}

@Composable
fun ReminderApp(db: AppDatabase, scheduler: NotificationScheduler) {
    var activeReminder by remember { mutableStateOf<Reminder?>(null) }
    var showAddScreen by remember { mutableStateOf(false) }
    var reminderList by remember { mutableStateOf(db.reminderDao().getAll()) }

    if (showAddScreen) {
        AddReminderScreen(
            initialTitle = activeReminder?.title ?: "",
            initialDesc = activeReminder?.description ?: "",
            initialRepeatDays = activeReminder?.repeatDays ?: "",
            onSaveClicked = { title, desc, time, repeatDays ->
                val id = activeReminder?.id ?: 0

                val reminderToSave = Reminder(
                    id = id,
                    title = title,
                    description = desc,
                    timeInMillis = time,
                    repeatDays = repeatDays,
                    isCompleted = activeReminder?.isCompleted ?: false
                )

                if (id == 0) {
                    db.reminderDao().insert(reminderToSave)
                } else {
                    db.reminderDao().delete(activeReminder!!)
                    db.reminderDao().insert(reminderToSave)
                }

                scheduler.scheduleNotification(reminderToSave)
                reminderList = db.reminderDao().getAll()
                showAddScreen = false
                activeReminder = null
            },
            onCancelClicked = {
                showAddScreen = false
                activeReminder = null
            }
        )
    } else {
        HomeScreen(
            reminders = reminderList,
            onAddClicked = {
                activeReminder = null
                showAddScreen = true
            },
            onDeleteClicked = { reminder ->
                db.reminderDao().delete(reminder)
                reminderList = db.reminderDao().getAll()
            },
            onEditClicked = { reminder ->
                activeReminder = reminder
                showAddScreen = true
            },
            onToggleCompletion = { reminder ->
                val updatedReminder = reminder.copy(isCompleted = !reminder.isCompleted)
                db.reminderDao().delete(reminder)
                db.reminderDao().insert(updatedReminder)
                reminderList = db.reminderDao().getAll()
            }
        )
    }
}
