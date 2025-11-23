package com.example.dailyreminderyarn

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Reminder")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timeInMillis: Long,
    // NEW FIELDS:
    val isCompleted: Boolean = false,
    val repeatDays: String = ""
)
