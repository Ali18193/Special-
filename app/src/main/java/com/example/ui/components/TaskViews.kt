package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.PlannerViewModel
import com.example.ui.theme.GradientEnd
import com.example.ui.theme.GradientStart

@Composable
fun SmartTaskInputBar(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    var rawText by remember { mutableStateOf("") }
    val isParsing by viewModel.isAiParsing.collectAsState()
    val feedbackMessage by viewModel.aiResponseFeedback.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Intel",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
            )
            Text(
                text = "Smart Task Parser (Natural Language)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isParsing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = rawText,
            onValueChange = { rawText = it },
            placeholder = {
                Text(
                    text = "e.g., Study integrations tomorrow at 3pm, call Mom at 6pm, and work report high priority in 60m",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("natural_language_input")
                .clip(RoundedCornerShape(12.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (rawText.trim().isNotEmpty()) {
                    viewModel.parseAndAddTask(rawText)
                    rawText = ""
                    focusManager.clearFocus()
                }
            })
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Suggestion pills
            Text(
                text = "Press Enter to let AI parse + schedule instantly",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Button(
                onClick = {
                    if (rawText.trim().isNotEmpty()) {
                        viewModel.parseAndAddTask(rawText)
                        rawText = ""
                        focusManager.clearFocus()
                    }
                },
                enabled = rawText.isNotBlank() && !isParsing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("parse_send_button")
            ) {
                Text("Process AI", style = MaterialTheme.typography.labelMedium)
            }
        }

        feedbackMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRowItem(
    task: Task,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(task.category)
    val priorityIcon = getPriorityIconAndColor(task.priority)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = categoryColor.copy(alpha = 0.3f)
            )
            .testTag("task_item_${task.id}")
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.2f
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onToggleSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox completed button
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("task_complete_btn_${task.id}")
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Complete task indicator",
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.4f
                    ),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Emoji circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(categoryColor.copy(alpha = 0.15f), CircleShape)
            ) {
                Text(text = task.emoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Words column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Chip style
                    Box(
                        modifier = Modifier
                            .background(categoryColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = categoryColor
                        )
                    }

                    if (task.timeSlot != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Schedule",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = task.timeSlot,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }

                    if (task.totalPomodoros > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Pomodoros Completed count",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "x${task.totalPomodoros}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right side priority flag and minutes estimate indicator
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    imageVector = priorityIcon.first,
                    contentDescription = "Priority Flag",
                    tint = priorityIcon.second,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${task.estimatedMinutes}m",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Utility mapper helpers
@Composable
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "work" -> MaterialTheme.colorScheme.primary
        "study" -> Color(0xFF8B5CF6) // Violet
        "personal" -> Color(0xFF10B981) // Teal
        "health" -> Color(0xFFEF4444) // Coral Red
        else -> Color(0xFFF59E0B) // Amber
    }
}

fun getPriorityIconAndColor(priority: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (priority.lowercase()) {
        "urgent" -> Icons.Default.CrisisAlert to Color(0xFFEF4444) // Urgent Fire Red
        "high" -> Icons.Default.Flag to Color(0xFFF97316) // Orange High
        "medium" -> Icons.Default.Flag to Color(0xFF3B82F6) // Blue Medium
        else -> Icons.Default.Flag to Color(0xFF9CA3AF) // Slate Gray Low
    }
}
