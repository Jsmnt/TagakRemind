package com.example.dailyreminderyarn

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase

// 1. The Data Access Object (DAO)
@Dao
interface ReminderDao {
    // make sure the table name 'Reminder' here matches the @Entity defined in Reminder.kt
    @Query("SELECT * FROM Reminder")
    fun getAll(): List<Reminder>

    @Insert
    fun insert(reminder: Reminder)

    @Delete
    fun delete(reminder: Reminder)
}

// 2. The Database Class
@Database(entities = [Reminder::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}
