package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsDashboard(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val allPomodoros by viewModel.allPomodoros.collectAsState()
    val aiSuggestionsText by viewModel.aiSmartSuggestionsText.collectAsState()

    // Calculations
    val completedTasks = allTasks.filter { it.isCompleted }
    val totalCount = allTasks.size
    val completionRatio = if (totalCount > 0) completedTasks.size.toFloat() / totalCount else 0f

    val categories = listOf("Work", "Study", "Personal", "Health", "Other")
    val categoryStats = categories.map { cat ->
        val catTasks = allTasks.filter { it.category == cat }
        val catCompleted = catTasks.filter { it.isCompleted }
        val pct = if (catTasks.isNotEmpty()) catCompleted.size.toFloat() / catTasks.size else 0f
        Triple(cat, catTasks.size, pct)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Productivity Insights",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Understand your execution speed, streaks, and focus metrics.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        // LARGE CIRCULAR PROGRESS RING & SUGGESTIONS ACCENT CARD
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Ring Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = completionRatio,
                        animationSpec = tween(durationMillis = 1000)
                    )

                    val strokePrimary = MaterialTheme.colorScheme.primary
                    val strokeSecondary = MaterialTheme.colorScheme.secondary

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = (size.width - 16.dp.toPx()) / 2

                        // Gray Background Circle
                        drawCircle(
                            color = strokePrimary.copy(alpha = 0.1f),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 8.dp.toPx())
                        )

                        // Progress Arc
                        drawArc(
                            brush = Brush.linearGradient(listOf(strokePrimary, strokeSecondary)),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                size.width - 16.dp.toPx(),
                                size.height - 16.dp.toPx()
                            ),
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.getDefault(), "%d%%", (completionRatio * 100).toInt()),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Done Today",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Global Completion Rating",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your completion target sits at ${completedTasks.size} out of $totalCount planned tasks. Great velocity, maintain consistency!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // SUMMARY STAT INDICES CARDS
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Streak Counter
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Current Streak", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("5 Days", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("Target >80% completions", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    }
                }

                // Focus Minutes
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Focus Logged", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val workedMins = allPomodoros.sumOf { it.durationMinutes }
                        Text("${workedMins}m Focus", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("${allPomodoros.size} total sessions", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    }
                }
            }
        }

        // GITHUB-STYLE HEATMAP
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Weekly Activity Map",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "GitHub-style visualization representing daily executed metrics",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Heatmap Grid: 7 rows (Mon-Sun), 14 columns
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (row in 0 until 7) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Day name spacer
                                val dayName = when(row) {
                                    1 -> "Mon"
                                    3 -> "Wed"
                                    5 -> "Fri"
                                    else -> ""
                                }
                                Text(
                                    text = dayName,
                                    fontSize = 8.sp,
                                    modifier = Modifier.width(20.dp),
                                    textAlign = TextAlign.Start,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )

                                for (col in 0 until 14) {
                                    // Simulated density coloring: some random shades represent completion load
                                    val activityDensity = (row + col) % 5
                                    val cellColor = when (activityDensity) {
                                        0 -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) // Empty
                                        1 -> Color(0xFFC7D2FE) // Indigo Light
                                        2 -> Color(0xFF818CF8) // Indigo Mid
                                        3 -> Color(0xFF4F46E5) // Indigo Dark
                                        else -> Color(0xFF312E81) // Deep Royal Indigo
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(cellColor)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Less", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf(0, 1, 2, 3, 4).forEach { density ->
                            val cellColor = when (density) {
                                0 -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                1 -> Color(0xFFC7D2FE)
                                2 -> Color(0xFF818CF8)
                                3 -> Color(0xFF4F46E5)
                                else -> Color(0xFF312E81)
                            }
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(1.dp)).background(cellColor))
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("More", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // AI POWERED SUGGESTIONS CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Tip",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Smart Suggestion Engine",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = aiSuggestionsText ?: "Evaluating completed logs details...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // CATEGORY FOCUS LOADS INDEX LIST
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Category Execution Densities",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    categoryStats.forEach { (cat, count, ratio) ->
                        val catColor = getCategoryColor(cat)
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(catColor, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(cat, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("$count planned (${(ratio * 100).toInt()}% Done)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Simple Custom category progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (count > 0) ratio else 0f)
                                        .fillMaxHeight()
                                        .background(catColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
