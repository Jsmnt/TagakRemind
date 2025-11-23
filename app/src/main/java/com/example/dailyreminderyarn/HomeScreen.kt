package com.example.dailyreminderyarn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color palette
private val PrimaryBlue = Color(0xFF2196F3)
private val White = Color(0xFFFFFFFF)
private val TextGray = Color(0xFF424242)
private val LightGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    reminders: List<Reminder>,
    onAddClicked: () -> Unit,
    onDeleteClicked: (Reminder) -> Unit,
    onEditClicked: (Reminder) -> Unit,
    onToggleCompletion: (Reminder) -> Unit
) {
    Scaffold(
        containerColor = White,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TagakRemind",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onAddClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Reminder",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No reminders as of the moment!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGray.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = reminders,
                    key = { reminder -> reminder.id }
                ) { reminder ->
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
        },
        positionalThreshold = { it * 0.5f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    ) {
        Card(
            onClick = { onClick(reminder) },
            colors = CardDefaults.cardColors(
                containerColor = if (reminder.isCompleted) LightGray else White
            ),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (reminder.isCompleted) Color(0xFFE0E0E0) else Color(0xFFEEEEEE)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Checkbox(
                    checked = reminder.isCompleted,
                    onCheckedChange = { onToggleCompletion(reminder) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryBlue,
                        uncheckedColor = Color(0xFFBDBDBD)
                    )
                )

                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (reminder.isCompleted) TextGray.copy(alpha = 0.5f) else TextGray,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (reminder.isCompleted) TextGray.copy(alpha = 0.4f) else TextGray.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val formatter = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
                        Text(
                            text = formatter.format(java.util.Date(reminder.timeInMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = PrimaryBlue.copy(alpha = 0.8f)
                        )

                        if (reminder.repeatDays.isNotEmpty()) {
                            Text(
                                text = "• ${reminder.repeatDays}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}