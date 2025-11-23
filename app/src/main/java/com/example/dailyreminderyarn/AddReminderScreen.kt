package com.example.dailyreminderyarn

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddReminderScreen(
    initialTitle: String = "",
    initialDesc: String = "",
    initialRepeatDays: String = "", // NEW: Accept existing repeat days if editing
    onSaveClicked: (String, String, Long, String) -> Unit, // NEW: We pass back the "repeat days" string
    onCancelClicked: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDesc) }

    // Variable to track the error message
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Variable to check if user actually touched the time buttons
    var hasPickedTime by remember { mutableStateOf(initialTitle.isNotEmpty()) }

    val calendar = Calendar.getInstance()
    var selectedTimeInMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var dateText by remember { mutableStateOf("Pick Date") }
    var timeText by remember { mutableStateOf("Pick Time") }

    // NEW: Tracking selected repeat days
    // We convert the string "Mon,Wed" back into a list ["Mon", "Wed"] to show selected chips
    val selectedDays = remember {
        mutableStateListOf<String>().apply {
            if (initialRepeatDays.isNotEmpty()) {
                addAll(initialRepeatDays.split(","))
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            if (initialTitle.isEmpty()) "Create New Reminder" else "Edit Reminder",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage?.contains("Title") == true
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage?.contains("Description") == true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                showDatePicker(context, calendar) {
                    selectedTimeInMillis = calendar.timeInMillis
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dateText = dateFormat.format(calendar.time)
                    hasPickedTime = true
                }
            }) {
                Text(text = dateText)
            }

            Button(onClick = {
                showTimePicker(context, calendar) {
                    selectedTimeInMillis = calendar.timeInMillis
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    timeText = timeFormat.format(calendar.time)
                    hasPickedTime = true
                }
            }) {
                Text(text = timeText)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NEW: Day Selector UI
        Text("Repeat on (Optional):", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        DaySelector(selectedDays)

        // Show the Error Message in Red if it exists
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Button(onClick = {
                // Validation Logic
                if (title.isBlank()) {
                    errorMessage = "Error: Title cannot be empty"
                } else if (description.isBlank()) {
                    errorMessage = "Error: Description cannot be empty"
                } else if (!hasPickedTime) {
                    errorMessage = "Error: Please select a Date and Time"
                } else if (selectedTimeInMillis < System.currentTimeMillis()) {
                    errorMessage = "Error: Time cannot be in the past"
                } else {
                    // NEW: Join the selected days into a string like "Mon,Wed"
                    val repeatDaysString = selectedDays.joinToString(",")

                    onSaveClicked(title, description, selectedTimeInMillis, repeatDaysString)
                }
            }) {
                Text("Save Reminder")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = onCancelClicked) {
                Text("Cancel")
            }
        }
    }
}

// NEW: Helper Composable for Day Buttons
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySelector(selectedDays: MutableList<String>) {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            val isSelected = selectedDays.contains(day)
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) selectedDays.remove(day) else selectedDays.add(day)
                },
                label = { Text(day.take(1)) } // Just show first letter (S, M, T...)
            )
        }
    }
}

fun showDatePicker(context: Context, calendar: Calendar, onDateSelected: () -> Unit) {
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            onDateSelected()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun showTimePicker(context: Context, calendar: Calendar, onTimeSelected: () -> Unit) {
    TimePickerDialog(
        context,
        { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            onTimeSelected()
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    ).show()
}
