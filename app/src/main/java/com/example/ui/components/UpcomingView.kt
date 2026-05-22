package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UpcomingView(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()

    // We will generate the next 7 days starting from 2026-05-22 (Friday)
    val daysList = remember {
        val list = mutableListOf<Pair<String, String>>() // Pair(DayLabel, YYYY-MM-DD DateString)
        val sdfLabel = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val sdfValue = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        // Pin calendar start date to 2026-05-22
        calendar.set(2026, Calendar.MAY, 22)

        for (i in 0 until 7) {
            val label = if (i == 0) "Today" else if (i == 1) "Tomorrow" else sdfLabel.format(calendar.time)
            val dateStr = sdfValue.format(calendar.time)
            list.add(label to dateStr)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Weekly Sprint Planning",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "An overview of your next 7 days. Tap any task to check subtasks or details.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal scrolling columns representing the days!
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(daysList) { (label, dateVal) ->
                val dayTasks = allTasks.filter { it.date == dateVal }
                val totalMinutes = dayTasks.sumOf { it.estimatedMinutes }

                UpcomingDayColumn(
                    label = label,
                    dateVal = dateVal,
                    tasks = dayTasks,
                    totalMinutes = totalMinutes,
                    onSelectDate = { viewModel.selectDate(dateVal) ; viewModel.currentTab.value = "Today" },
                    onSelectTask = { viewModel.selectTask(it) },
                    onReschedule = { task, targetDate -> viewModel.rescheduleTask(task, targetDate) },
                    daysList = daysList
                )
            }
        }
    }
}

@Composable
fun UpcomingDayColumn(
    label: String,
    dateVal: String,
    tasks: List<Task>,
    totalMinutes: Int,
    onSelectDate: () -> Unit,
    onSelectTask: (Task) -> Unit,
    onReschedule: (Task, String) -> Unit,
    daysList: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Day Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = dateVal,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                TextButton(
                    onClick = onSelectDate,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Focus", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${tasks.size} Tasks",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${totalMinutes}m est",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks planned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks) { task ->
                        UpcomingTaskItemCard(
                            task = task,
                            onClick = { onSelectTask(task) },
                            onShift = {
                                // Shift to the next day! Find current index and move +1
                                val currentIndex = daysList.indexOfFirst { it.second == task.date }
                                if (currentIndex != -1 && currentIndex < daysList.size - 1) {
                                    onReschedule(task, daysList[currentIndex + 1].second)
                                } else {
                                    // Wrap back to today
                                    onReschedule(task, daysList[0].second)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingTaskItemCard(
    task: Task,
    onClick: () -> Unit,
    onShift: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(task.category)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = task.emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground,
                    style = if (task.isCompleted) MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${task.category} • ${task.estimatedMinutes}m",
                    fontSize = 9.sp,
                    color = categoryColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            // Quick shift day button
            IconButton(
                onClick = onShift,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Shift task day",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
