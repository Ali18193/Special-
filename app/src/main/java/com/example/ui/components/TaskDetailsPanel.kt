package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subtask
import com.example.data.Task
import java.util.Locale
import com.example.ui.PlannerViewModel
import com.example.ui.theme.GradientEnd
import com.example.ui.theme.GradientStart

@Composable
fun TaskDetailsPanel(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val task by viewModel.selectedTask.collectAsState()
    val subtasks by viewModel.currentSubtasks.collectAsState()
    var newSubtaskText by remember { mutableStateOf("") }
    var isOriginalTextCollapsed by remember { mutableStateOf(true) }

    if (task == null) {
        // Fallback or Empty state: Show general Floating Pomodoro Timer
        Box(
            modifier = modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Flow Timer",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select any task to configure subtasks or log direct focus minutes against it.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Render general Timer anyway so user has a floating clock!
                PomodoroCircularClock(viewModel = viewModel)
            }
        }
        return
    }

    val activeTask = task!!

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
            )
            .padding(18.dp)
    ) {
        // Top row: Back button and Reschedule controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { viewModel.closeRightPanel() },
                modifier = Modifier.testTag("close_details_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Close details panel",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Task Workspace",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
            // Delete task button
            IconButton(
                onClick = { viewModel.deleteCurrentTask(activeTask) },
                modifier = Modifier.testTag("delete_task_details_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Meta Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                getCategoryColor(activeTask.category).copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Text(text = activeTask.emoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = activeTask.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Planned: ${activeTask.date}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Collapsible Original raw text details
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isOriginalTextCollapsed = !isOriginalTextCollapsed }
                        ) {
                            Icon(
                                imageVector = if (isOriginalTextCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Note Feed Extract",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!isOriginalTextCollapsed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = activeTask.originalText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // RESCHEDULE QUICK BUTTON ROW
            item {
                Column {
                    Text(
                        text = "Quick Reschedule",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "Today" to "2026-05-22",
                            "Tomorrow" to "2026-05-23",
                            "Next Monday" to "2026-05-25"
                        ).forEach { (label, dateVal) ->
                            val isCurrentDate = activeTask.date == dateVal
                            OutlinedButton(
                                onClick = { viewModel.rescheduleTask(activeTask, dateVal) },
                                modifier = Modifier.weight(1f),
                                border = if (isCurrentDate) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonBorder,
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrentDate) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrentDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            // SUBTASK CHECKLIST
            item {
                Column {
                    Text(
                        text = "Subtasks / Checklists",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newSubtaskText,
                        onValueChange = { newSubtaskText = it },
                        placeholder = { Text("Add critical subtask...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_subtask_input"),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newSubtaskText.isNotBlank()) {
                                        viewModel.addSubtask(newSubtaskText)
                                        newSubtaskText = ""
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add subtask")
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newSubtaskText.isNotBlank()) {
                                viewModel.addSubtask(newSubtaskText)
                                newSubtaskText = ""
                            }
                        })
                    )
                }
            }

            items(subtasks) { sub ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleSubtaskCompletion(sub) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (sub.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = "Toggle subtask complete",
                            tint = if (sub.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.4f
                            ),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sub.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (sub.isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.deleteSubtask(sub) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete subtask",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // INTEGRATED POMODORO WORKSPACE
            item {
                Column {
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Linked Pomodoro Workspace",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    PomodoroCircularClock(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PomodoroCircularClock(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val durationMinutes by viewModel.pomodoroMinutes.collectAsState()
    val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsState()
    val isRunning by viewModel.isPomodoroRunning.collectAsState()
    val isFocusMode by viewModel.isPomodoroFocusMode.collectAsState()
    val timerMode by viewModel.pomodoroTimerMode.collectAsState()
    val totalGoal by viewModel.pomodoroGoal.collectAsState()
    val completedCount by viewModel.pomodoroLoggedCompleted.collectAsState()
    val autoStartNext by viewModel.autoStartNext.collectAsState()

    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", secondsLeft / 60, secondsLeft % 60)

    val totalSeconds = durationMinutes * 60
    val progressRatio = if (totalSeconds > 0) secondsLeft.toFloat() / totalSeconds else 0f

    // Animated colors for circular progress background based on mode
    val progressColor = when (timerMode) {
        "Work" -> Color(0xFFEF4444) // Bright red
        "Short Break" -> Color(0xFF10B981) // Green
        else -> Color(0xFF3B82F6) // Blue
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                Triple("Work", 25, "Work"),
                Triple("Short Break", 5, "Short Break"),
                Triple("Long Break", 15, "Long Break")
            ).forEach { (label, min, mode) ->
                val active = timerMode == mode
                OutlinedButton(
                    onClick = { viewModel.configurePomodoro(min, mode) },
                    border = BorderStroke(
                        width = if (active) 1.5.dp else 1.dp,
                        color = if (active) progressColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (active) progressColor.copy(alpha = 0.08f) else Color.Transparent
                    )
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) progressColor else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large animated circular timer custom Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2, size.height / 2)
                val radius = (size.width - 20) / 2
                
                // Track arc
                drawCircle(
                    color = progressColor.copy(alpha = 0.15f),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active countdown arc
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = -360f * progressRatio,
                    useCenter = false,
                    topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                    size = Size(size.width - 20.dp.toPx(), size.height - 20.dp.toPx()),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = timerMode,
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controlling buttons (Play, Pause, Focus, Reset)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Immersive Dim Focus Toggler
            IconButton(
                onClick = { viewModel.togglePomodoroFocusMode() },
                modifier = Modifier
                    .background(if (isFocusMode) Color(0xFFFBBF24).copy(alpha = 0.2f) else Color.Transparent, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFocusMode) Icons.Default.Brightness2 else Icons.Default.Brightness5,
                    contentDescription = "Focus mode toggle",
                    tint = if (isFocusMode) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onBackground
                )
            }

            // Central Play/Pause button
            Button(
                onClick = {
                    if (isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = progressColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp)
                    .testTag("pomodoro_play_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isRunning) "Pause" else "Start", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Reset button
            IconButton(
                onClick = { viewModel.configurePomodoro(durationMinutes, timerMode) },
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Daily limits and Auto-start
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Auto-start Next Session",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = autoStartNext,
                onCheckedChange = { viewModel.autoStartNext.value = it },
                modifier = Modifier.scale(0.8f)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = null,
                tint = progressColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Sessions Logged: $completedCount Completed / Target $totalGoal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
