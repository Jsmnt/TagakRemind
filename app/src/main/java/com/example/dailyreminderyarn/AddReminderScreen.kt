package com.example.dailyreminderyarn

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Color palette
private val PrimaryBlue = Color(0xFF2196F3)
private val LightBlue = Color(0xFFE3F2FD)
private val White = Color(0xFFFFFFFF)
private val TextGray = Color(0xFF424242)
private val ErrorRed = Color(0xFFD32F2F)

@Composable
fun AddReminderScreen(
    initialTitle: String = "",
    initialDesc: String = "",
    initialRepeatDays: String = "",
    onSaveClicked: (String, String, Long, String) -> Unit,
    onCancelClicked: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDesc) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasPickedTime by remember { mutableStateOf(initialTitle.isNotEmpty()) }

    val calendar = Calendar.getInstance()
    var selectedTimeInMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var dateText by remember { mutableStateOf("Pick Date") }
    var timeText by remember { mutableStateOf("Pick Time") }

    val selectedDays = remember {
        mutableStateListOf<String>().apply {
            if (initialRepeatDays.isNotEmpty()) {
                addAll(initialRepeatDays.split(","))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(20.dp)
    ) {
        Text(
            if (initialTitle.isEmpty()) "Create New Reminder" else "Edit Reminder",
            style = MaterialTheme.typography.headlineMedium,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage?.contains("Title") == true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedLabelColor = PrimaryBlue
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage?.contains("Description") == true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedLabelColor = PrimaryBlue
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    showDatePicker(context, calendar) {
                        selectedTimeInMillis = calendar.timeInMillis
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        dateText = dateFormat.format(calendar.time)
                        hasPickedTime = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightBlue,
                    contentColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = dateText)
            }

            Button(
                onClick = {
                    showTimePicker(context, calendar) {
                        selectedTimeInMillis = calendar.timeInMillis
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        timeText = timeFormat.format(calendar.time)
                        hasPickedTime = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightBlue,
                    contentColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = timeText)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Repeat on (Optional):",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(12.dp))
        DaySelector(selectedDays)

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage!!,
                color = ErrorRed,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Error: Title cannot be empty"
                    } else if (description.isBlank()) {
                        errorMessage = "Error: Description cannot be empty"
                    } else if (!hasPickedTime) {
                        errorMessage = "Error: Please select a Date and Time"
                    } else if (selectedTimeInMillis < System.currentTimeMillis()) {
                        errorMessage = "Error: Time cannot be in the past"
                    } else {
                        val repeatDaysString = selectedDays.joinToString(",")
                        onSaveClicked(title, description, selectedTimeInMillis, repeatDaysString)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Reminder")
            }

            OutlinedButton(
                onClick = onCancelClicked,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryBlue
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
        }
    }
}

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
                label = { Text(day.take(1)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = White,
                    containerColor = LightBlue,
                    labelColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(8.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) PrimaryBlue else Color(0xFFE0E0E0),
                    selectedBorderColor = PrimaryBlue
                )
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