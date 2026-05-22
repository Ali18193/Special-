package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarView(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val activeSelectedDate by viewModel.selectedDate.collectAsState()

    // Keep track of our virtual calendar month (initially May 2026 based on our pin date)
    var calendarYear by remember { mutableStateOf(2026) }
    var calendarMonth by remember { mutableStateOf(Calendar.MAY) }

    val monthLabels = listOf(
        "January", "February", "March", "April", "May", "June", 
        "July", "August", "September", "October", "November", "December"
    )

    // Compute calendar grid slots (Days of the week grid)
    val gridDays = remember(calendarYear, calendarMonth) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, calendarYear)
        calendar.set(Calendar.MONTH, calendarMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday...
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val days = mutableListOf<String?>()
        // Prefill empty blocks prior to start of month (assuming Monday is start of week)
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        for (o in 0 until offset) {
            days.add(null)
        }
        for (d in 1..maxDays) {
            days.add(String.format(Locale.getDefault(), "%02d", d))
        }
        days
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Interactive Calendar",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Visual dashboard density mapping",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            // Month Swapper
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (calendarMonth == Calendar.JANUARY) {
                            calendarMonth = Calendar.DECEMBER
                            calendarYear -= 1
                        } else {
                            calendarMonth -= 1
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev month")
                }

                Text(
                    text = "${monthLabels[calendarMonth]} $calendarYear",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(130.dp)
                )

                IconButton(
                    onClick = {
                        if (calendarMonth == Calendar.DECEMBER) {
                            calendarMonth = Calendar.JANUARY
                            calendarYear += 1
                        } else {
                            calendarMonth += 1
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next month")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row of header days
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dayName ->
                Text(
                    text = dayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Calendar Box
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gridDays) { dayNumber ->
                if (dayNumber == null) {
                    // Empty cell spacer
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%s", calendarYear, calendarMonth + 1, dayNumber)
                    val isSelected = activeSelectedDate == formattedDate
                    val dayTasks = allTasks.filter { it.date == formattedDate }

                    CalendarDayBlock(
                        dayNumber = dayNumber,
                        dateValue = formattedDate,
                        selected = isSelected,
                        tasks = dayTasks,
                        onClick = {
                            viewModel.selectDate(formattedDate)
                            viewModel.currentTab.value = "Today"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDayBlock(
    dayNumber: String,
    dateValue: String,
    selected: Boolean,
    tasks: List<Task>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTasks = tasks.filter { !it.isCompleted }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .testTag("calendar_day_block_$dateValue")
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayNumber.toInt().toString(), // Clean double digits to single display
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )

            // Indicators
            if (tasks.isNotEmpty()) {
                val uniqueCategories = tasks.map { it.category }.distinct().take(4)
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    uniqueCategories.forEach { cat ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(getCategoryColor(cat), CircleShape)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}
