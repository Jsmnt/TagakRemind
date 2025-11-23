package com.example.dailyreminderyarn

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    reminders: List<Reminder>,
    onAddClicked: () -> Unit,
    onDeleteClicked: (Reminder) -> Unit,
    onEditClicked: (Reminder) -> Unit,
    // NEW PARAMETER: This fixes the "No parameter found" error
    onToggleCompletion: (Reminder) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClicked) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(reminders) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onDelete = onDeleteClicked,
                    onClick = onEditClicked,
                    onToggleCompletion = onToggleCompletion
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItem(
    reminder: Reminder,
    onDelete: (Reminder) -> Unit,
    onClick: (Reminder) -> Unit,
    onToggleCompletion: (Reminder) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(reminder)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = MaterialTheme.colorScheme.error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = color)
            }
        }
    ) {
        Card(
            onClick = { onClick(reminder) },
            // Gray out if completed
            colors = CardDefaults.cardColors(
                containerColor = if (reminder.isCompleted) Color.LightGray else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // NEW: Checkbox
                Checkbox(
                    checked = reminder.isCompleted,
                    onCheckedChange = { onToggleCompletion(reminder) }
                )

                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleLarge,
                        // Strikethrough if completed
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                    )

                    Text(text = reminder.description)

                    val formatter = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
                    Text(text = "Time: ${formatter.format(java.util.Date(reminder.timeInMillis))}", style = MaterialTheme.typography.bodySmall)

                    if (reminder.repeatDays.isNotEmpty()) {
                        Text(text = "Repeats: ${reminder.repeatDays}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
