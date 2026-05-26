package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale

fun formatEpochMs(epochMs: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(epochMs))
}

// ─── Data Models ───────────────────────────────────────────────────────────────

data class Exam(
    val id: Int,
    val subject: String,
    val emoji: String,
    val dateMs: Long,
    val topics: List<ExamTopic>,
    val color: Color
)

data class ExamTopic(
    val name: String,
    var isDone: Boolean = false
)

// ─── Color Palette ─────────────────────────────────────────────────────────────

private val Coral     = Color(0xFFFF6B6B)
private val Mint      = Color(0xFF4ECDC4)
private val Sunshine  = Color(0xFFFFE66D)
private val Lavender  = Color(0xFFA8DADC)
private val Peach     = Color(0xFFFFB347)
private val Sky       = Color(0xFF74B9FF)
private val Rose      = Color(0xFFFF7675)
private val Lime      = Color(0xFF55EFC4)

private val examColors = listOf(Coral, Mint, Peach, Sky, Rose, Lime, Lavender, Sunshine)

// ─── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun ExamScreen(
    modifier: Modifier = Modifier
) {
    var exams by remember {
        mutableStateOf(
            listOf(
                Exam(
                    id = 1,
                    subject = "Riyaziyyat",
                    emoji = "📐",
                    dateMs = System.currentTimeMillis() + 5L * 24 * 3600 * 1000,
                    topics = listOf(
                        ExamTopic("İnteqral"),
                        ExamTopic("Diferensial tənliklər"),
                        ExamTopic("Ardıcıllıqlar"),
                    ),
                    color = Coral
                ),
                Exam(
                    id = 2,
                    subject = "Fizika",
                    emoji = "⚛️",
                    dateMs = System.currentTimeMillis() + 12L * 24 * 3600 * 1000,
                    topics = listOf(
                        ExamTopic("Mexanika"),
                        ExamTopic("Elektrik"),
                        ExamTopic("Optika"),
                        ExamTopic("Termodinamika"),
                    ),
                    color = Mint
                ),
                Exam(
                    id = 3,
                    subject = "İngilis dili",
                    emoji = "🌍",
                    dateMs = System.currentTimeMillis() + 3L * 24 * 3600 * 1000,
                    topics = listOf(
                        ExamTopic("Grammar"),
                        ExamTopic("Reading"),
                        ExamTopic("Writing"),
                    ),
                    color = Sunshine
                )
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedExamIdForDetail by remember { mutableStateOf<Int?>(null) }
    var promptPomodoroTopicName by remember { mutableStateOf<String?>(null) }
    var promptPomodoroExamId by remember { mutableStateOf<Int?>(null) }
    var activePomodoroTopicName by remember { mutableStateOf<String?>(null) }
    var activePomodoroExamId by remember { mutableStateOf<Int?>(null) }
    var pomodoroIsTestMode by remember { mutableStateOf(false) }
    var activePomodoroDurationMinutes by remember { mutableStateOf(25) }

    val liveDetailExam = exams.find { it.id == selectedExamIdForDetail }

    Box(modifier = modifier.fillMaxSize()) {
        BackgroundBlobs()

        Column(modifier = Modifier.fillMaxSize()) {
            ExamHeader()

            val nearestExam = exams.filter { !it.topics.all { t -> t.isDone } || it.topics.isEmpty() }
                .minByOrNull { it.dateMs } ?: exams.minByOrNull { it.dateMs }

            nearestExam?.let {
                CountdownBanner(exam = it, onClick = { selectedExamIdForDetail = it.id })
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(exams.sortedBy { it.dateMs }) { exam ->
                    ExamCard(
                        exam = exam,
                        onCardClick = { selectedExamIdForDetail = exam.id }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(60.dp)
                .testTag("add_exam_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "İmtahan əlavə et", tint = Color.White, modifier = Modifier.size(28.dp))
        }

        // Add Exam Dialog
        if (showAddDialog) {
            AddExamDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { subject, emoji, daysUntil, topics ->
                    val newExam = Exam(
                        id = exams.maxOfOrNull { it.id }?.plus(1) ?: 1,
                        subject = subject,
                        emoji = emoji,
                        dateMs = System.currentTimeMillis() + daysUntil.toLong() * 24 * 3600 * 1000,
                        topics = topics.map { ExamTopic(it) },
                        color = examColors.random()
                    )
                    exams = exams + newExam
                    showAddDialog = false
                }
            )
        }

        // Detailed Exam Dialog
        if (liveDetailExam != null) {
            ExamDetailDialog(
                exam = liveDetailExam,
                onDismiss = { selectedExamIdForDetail = null },
                onTopicToggle = { topicName ->
                    exams = exams.map { e ->
                        if (e.id == liveDetailExam.id) {
                            e.copy(topics = e.topics.map { t ->
                                if (t.name == topicName) t.copy(isDone = !t.isDone) else t
                            })
                        } else e
                    }
                },
                onStartPomodoro = { topic ->
                    promptPomodoroTopicName = topic.name
                    promptPomodoroExamId = liveDetailExam.id
                },
                onDelete = {
                    exams = exams.filter { it.id != liveDetailExam.id }
                    selectedExamIdForDetail = null
                }
            )
        }

        // Pomodoro Confirmation
        if (promptPomodoroTopicName != null && promptPomodoroExamId != null) {
            PomodoroStartConfirmationDialog(
                topicName = promptPomodoroTopicName!!,
                onDismiss = {
                    promptPomodoroTopicName = null
                    promptPomodoroExamId = null
                },
                onStart = { isTest, durationMins ->
                    activePomodoroTopicName = promptPomodoroTopicName
                    activePomodoroExamId = promptPomodoroExamId
                    pomodoroIsTestMode = isTest
                    activePomodoroDurationMinutes = durationMins
                    promptPomodoroTopicName = null
                    promptPomodoroExamId = null
                }
            )
        }

        // Mandatory Pomodoro Dialog
        if (activePomodoroTopicName != null && activePomodoroExamId != null) {
            val pomodoroExam = exams.find { it.id == activePomodoroExamId }
            val pomodoroColor = pomodoroExam?.color ?: Coral
            val pomodoroSubject = pomodoroExam?.subject ?: "İmtahan"

            MandatoryPomodoroDialog(
                topicName = activePomodoroTopicName!!,
                subjectName = pomodoroSubject,
                accentColor = pomodoroColor,
                isTestMode = pomodoroIsTestMode,
                durationMinutes = activePomodoroDurationMinutes,
                onFinished = {
                    exams = exams.map { e ->
                        if (e.id == activePomodoroExamId) {
                            e.copy(topics = e.topics.map { t ->
                                if (t.name == activePomodoroTopicName) t.copy(isDone = true) else t
                            })
                        } else e
                    }
                    activePomodoroTopicName = null
                    activePomodoroExamId = null
                }
            )
        }
    }
}

// ─── Background Animated Blobs ─────────────────────────────────────────────────

@Composable
fun BackgroundBlobs() {
    val infiniteTransition = rememberInfiniteTransition(label = "blobs")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "offset"
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-30).dp, y = (offset - 10).dp)
                .size(200.dp)
                .background(Coral.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (120 + offset).dp)
                .size(150.dp)
                .background(Mint.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 20.dp, y = (-offset).dp)
                .size(180.dp)
                .background(Sky.copy(alpha = 0.08f), CircleShape)
        )
    }
}

// ─── Header ────────────────────────────────────────────────────────────────────

@Composable
fun ExamHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "İmtahan Rejimi",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Hazırlıq prosesini izlə 🎯",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Brush.linearGradient(listOf(Coral, Peach)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🎓", fontSize = 20.sp)
        }
    }
}

// ─── Countdown Banner ──────────────────────────────────────────────────────────

@Composable
fun CountdownBanner(exam: Exam, onClick: () -> Unit) {
    val daysLeft = ((exam.dateMs - System.currentTimeMillis()) / (24L * 3600 * 1000)).coerceAtLeast(0L)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(exam.color.copy(alpha = 0.35f), exam.color.copy(alpha = 0.15f))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Ən yaxın imtahan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${exam.emoji} ${exam.subject}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$daysLeft",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = exam.color
                    )
                    Text(
                        text = "gün qaldı",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ─── Realtime Countdown ────────────────────────────────────────────────────────

@Composable
fun RealtimeCountdown(examDateMs: Long, accentColor: Color) {
    var totalSecondsRemaining by remember(examDateMs) {
        val target = examDateMs / 1000L
        val now = System.currentTimeMillis() / 1000L
        mutableStateOf((target - now).coerceAtLeast(0L))
    }

    LaunchedEffect(examDateMs) {
        while (totalSecondsRemaining > 0) {
            delay(1000L)
            val target = examDateMs / 1000L
            val now = System.currentTimeMillis() / 1000L
            totalSecondsRemaining = (target - now).coerceAtLeast(0L)
        }
    }

    val days = totalSecondsRemaining / (24 * 3600)
    val hours = (totalSecondsRemaining % (24 * 3600)) / 3600
    val minutes = (totalSecondsRemaining % 3600) / 60
    val seconds = totalSecondsRemaining % 60

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CountdownUnitCard(value = days, label = "Gün", color = accentColor, modifier = Modifier.weight(1f))
        CountdownUnitCard(value = hours, label = "Saat", color = accentColor, modifier = Modifier.weight(1f))
        CountdownUnitCard(value = minutes, label = "Dəq", color = accentColor, modifier = Modifier.weight(1f))
        CountdownUnitCard(value = seconds, label = "San", color = accentColor, modifier = Modifier.weight(1f))
    }
}

@Composable
fun CountdownUnitCard(value: Long, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%02d", value),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Exam Card ─────────────────────────────────────────────────────────────────

@Composable
fun ExamCard(exam: Exam, onCardClick: () -> Unit) {
    val daysLeft = ((exam.dateMs - System.currentTimeMillis()) / (24L * 3600 * 1000)).coerceAtLeast(0L)
    val progress = if (exam.topics.isNotEmpty()) exam.topics.count { it.isDone }.toFloat() / exam.topics.size else 1.0f
    val urgencyColor = when {
        daysLeft <= 3 -> Coral
        daysLeft <= 7 -> Peach
        else -> Mint
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(exam.color.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(exam.emoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = exam.subject,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatEpochMs(exam.dateMs),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$daysLeft g",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = urgencyColor
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
 
