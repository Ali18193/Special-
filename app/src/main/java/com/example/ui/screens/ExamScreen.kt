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
    
    // Detailed Dialog configuration
    var selectedExamIdForDetail by remember { mutableStateOf<Int?>(null) }
    
    // Pomodoro flow configuration
    var promptPomodoroTopicName by remember { mutableStateOf<String?>(null) }
    var promptPomodoroExamId by remember { mutableStateOf<Int?>(null) }
    
    var activePomodoroTopicName by remember { mutableStateOf<String?>(null) }
    var activePomodoroExamId by remember { mutableStateOf<Int?>(null) }
    var pomodoroIsTestMode by remember { mutableStateOf(false) }
    var activePomodoroDurationMinutes by remember { mutableStateOf(25) }

    val liveDetailExam = exams.find { it.id == selectedExamIdForDetail }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Animated background blobs
        BackgroundBlobs()

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            ExamHeader()

            // Countdown for nearest exam
            val nearestExam = exams.filter { !it.topics.all { t -> t.isDone } || it.topics.isEmpty() }
                .minByOrNull { it.dateMs }
                ?: exams.minByOrNull { it.dateMs }

            nearestExam?.let {
                CountdownBanner(exam = it, onClick = { selectedExamIdForDetail = it.id })
            }

            // Exam list
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

        // Detailed Exam & Countdown Dialog
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
                }
            )
        }

        // Start Pomodoro Confirmation
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
                    
                    // Cleanup confirmation dialog
                    promptPomodoroTopicName = null
                    promptPomodoroExamId = null
                }
            )
        }

        // UNBREAKABLE POMODORO SESSION DIALOG (LOCKED SCREEN)
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
                    // Mark topic as done automatically
                    exams = exams.map { e ->
                        if (e.id == activePomodoroExamId) {
                            e.copy(topics = e.topics.map { t ->
                                if (t.name == activePomodoroTopicName) t.copy(isDone = true) else t
                            })
                        } else e
                    }
                    
                    // Close Pomodoro Dialog
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
                .background(
                    Brush.linearGradient(listOf(Coral, Peach)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🎓", fontSize = 20.sp)
        }
    }
}

// ─── Countdown Banner ──────────────────────────────────────────────────────────

@Composable
fun CountdownBanner(
    exam: Exam,
    onClick: () -> Unit
) {
    val daysLeft = ((exam.dateMs - System.currentTimeMillis()) / (24L * 3600 * 1000)).coerceAtLeast(0L)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            exam.color.copy(alpha = 0.35f),
                            exam.color.copy(alpha = 0.15f)
                        )
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
fun RealtimeCountdown(
    examDateMs: Long,
    accentColor: Color
) {
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CountdownUnitCard(value = days, label = "Gün", color = accentColor, modifier = Modifier.weight(1f))
        CountdownUnitCard(value = hours, label = "Saat", color = accentColor, modifier = Modifier.weight(1f))
        CountdownUnitCard(value = minutes, label = "Dəq", color = accentColor, modifier = Modifier.weight(1f))
        CountdownUnitCard(value = seconds, label = "San", color = accentColor, modifier = Modifier.weight(1f))
    }
}

@Composable
fun CountdownUnitCard(
    value: Long,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
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
fun ExamCard(
    exam: Exam,
    onCardClick: () -> Unit
) {
    val daysLeft = ((exam.dateMs - System.currentTimeMillis()) / (24L * 3600 * 1000)).coerceAtLeast(0L)
    val progress = if (exam.topics.isNotEmpty()) {
        exam.topics.count { it.isDone }.toFloat() / exam.topics.size
    } else {
        1.0f
    }

    val urgencyColor = when {
        daysLeft <= 3 -> Coral
        daysLeft <= 7 -> Peach
        else -> Mint
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(20.dp)
            )
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
                    // Colored dot + emoji
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
                            text = "Tarix: ${formatEpochMs(exam.dateMs)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Days badge
                Box(
                    modifier = Modifier
                        .background(urgencyColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${daysLeft}g",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = exam.color,
                    trackColor = exam.color.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = exam.color
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = exam.color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Geri sayım və Mövzular üçün klikləyin ⚡",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = exam.color.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ─── Exam Detail & Countdown Dialog ──────────────────────────────────────────

@Composable
fun ExamDetailDialog(
    exam: Exam,
    onDismiss: () -> Unit,
    onTopicToggle: (String) -> Unit,
    onStartPomodoro: (ExamTopic) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(exam.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(exam.emoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = exam.subject,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tarix: ${formatEpochMs(exam.dateMs)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Bağla",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // LARGE TIMER (GÜN SAAT SANİYƏ)
                Text(
                    text = "Geri Sayım Sayğacı ⏱️",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                RealtimeCountdown(examDateMs = exam.dateMs, accentColor = exam.color)

                Spacer(modifier = Modifier.height(24.dp))

                // TOPICS HEADER
                Text(
                    text = "Sənin hazırlıq planın 🎯",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (exam.topics.isEmpty()) {
                    Text(
                        text = "Mövzu əlavə edilməyib.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        exam.topics.forEach { topic ->
                            DetailTopicRow(
                                topic = topic,
                                accentColor = exam.color,
                                onToggle = { onTopicToggle(topic.name) },
                                onStartPomodoro = { onStartPomodoro(topic) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Detail Topic Row with Pomodoro Button ────────────────────────────────────

@Composable
fun DetailTopicRow(
    topic: ExamTopic,
    accentColor: Color,
    onToggle: () -> Unit,
    onStartPomodoro: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        if (topic.isDone) accentColor else Color.Transparent,
                        CircleShape
                    )
                    .border(2.dp, accentColor, CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (topic.isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = topic.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (topic.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (topic.isDone) TextDecoration.LineThrough else null
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // POMODORO FOCUS TRIGGER
        Button(
            onClick = onStartPomodoro,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (topic.isDone) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else accentColor
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(32.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = if (topic.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Dərs Pomodoro",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (topic.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.White
                )
            }
        }
    }
}

// ─── Pomodoro Start Confirmation Dialog ───────────────────────────────────────

@Composable
fun PomodoroStartConfirmationDialog(
    topicName: String,
    onDismiss: () -> Unit,
    onStart: (Boolean, Int) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(25) }
    var isTestMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Coral,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Məcburi Pomodoro ⚡",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mövzu: $topicName",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Diqqət: Bu rejim başladıqda, kəsilə və ya dayandırıla bilməz! Öyrənmə müddəti tamamlanana qədər ekran kilidli olacaqdır. 🔒",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // MÖVZUNUN HƏCMİ SEÇİMİ
                Text(
                    text = "Mövzunun həcminə görə müddəti seçin:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Row of preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        Triple(15, "Kiçik", "15 dq"),
                        Triple(25, "Orta", "25 dq"),
                        Triple(50, "Böyük", "50 dq")
                    )
                    presets.forEach { (mins, label, sub) ->
                        val isSelected = selectedMinutes == mins && !isTestMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedMinutes = mins
                                    isTestMode = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = sub,
                                    fontSize = 10.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stepper for custom duration
                Text(
                    text = "Sərbəst müddət tənzimlənməsi:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            selectedMinutes = (selectedMinutes - 5).coerceAtLeast(5)
                            isTestMode = false
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "$selectedMinutes dəqiqə",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.widthIn(min = 90.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = {
                            selectedMinutes = (selectedMinutes + 5).coerceIn(5, 180)
                            isTestMode = false
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Test vs Real toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .clickable {
                            isTestMode = !isTestMode
                            if (isTestMode) {
                                selectedMinutes = 5
                            } else {
                                selectedMinutes = 25
                            }
                        }
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(if (isTestMode) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Yoxlama Sınağı (10 saniyə)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Funksiyanı yoxlamaq və sürətli test etmək üçün",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Geri", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Button(
                        onClick = { onStart(isTestMode, selectedMinutes) },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sessiyaya Başla 🚀", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ─── Mandatory Pomodoro Dialog (Unbreakable Cover) ─────────────────────────────

@Composable
fun MandatoryPomodoroDialog(
    topicName: String,
    subjectName: String,
    accentColor: Color,
    isTestMode: Boolean,
    durationMinutes: Int,
    onFinished: () -> Unit
) {
    val totalSeconds = if (isTestMode) 10 else durationMinutes * 60
    var timeLeft by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(true) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            showSuccess = true
            isRunning = false
        }
    }

    Dialog(
        onDismissRequest = { /* Empty block to block dismiss */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            // Gradient blob background
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-50).dp)
                    .size(300.dp)
                    .background(accentColor.copy(alpha = 0.12f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!showSuccess) {
                    // Subject and Topic details
                    Text(
                        text = subjectName.uppercase(Locale.getDefault()),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = topicName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "mövzusu oxunur... Səbirlə oxuyun!",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(44.dp))

                    // Progress circular ring
                    val progressPct = timeLeft.toFloat() / totalSeconds
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(240.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progressPct },
                            modifier = Modifier.fillMaxSize(),
                            color = accentColor,
                            strokeWidth = 12.dp,
                            trackColor = Color.White.copy(alpha = 0.08f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val mins = timeLeft / 60
                            val secs = timeLeft % 60
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                                fontSize = 54.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Coral,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "MƏCBURİ KİLİDLİ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Coral,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(44.dp))

                    // Dynamic motivative messages
                    val quote = when {
                        timeLeft > totalSeconds * 0.75 -> "Telefonu cibinizdən çəkin, gözləriniz mətndə olsun! 📖"
                        timeLeft > totalSeconds * 0.50 -> "Diqqətiniz 100%! Hədəfinizə çox yaxınsınız! ⚡"
                        timeLeft > totalSeconds * 0.25 -> "İmtahan günü bu hazırlıq sizə böyük şans verəcək! 🔥"
                        else -> "Son saniyələr! Biliklərinizi möhkəmləndirin... 🚀"
                    }
                    Text(
                        text = quote,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    // Celebration success screen
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(Mint.copy(alpha = 0.2f), CircleShape)
                            .border(2.dp, Mint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Mint,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Möhtəşəm! 🎉",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "'$topicName' mövzusu üzrə hazırlığınız uğurla bitdi. Mövzu tamamlanmış olaraq qeyd edildi!",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    Button(
                        onClick = onFinished,
                        colors = ButtonDefaults.buttonColors(containerColor = Mint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Tamamla 🎓",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Add Exam Dialog ───────────────────────────────────────────────────────────

@Composable
fun AddExamDialog(onDismiss: () -> Unit, onAdd: (String, String, Int, List<String>) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📚") }
    var daysUntil by remember { mutableStateOf("7") }
    var topicsText by remember { mutableStateOf("") }

    val emojis = listOf("📚", "📐", "⚛️", "🌍", "💻", "🧬", "📊", "🎨", "⚗️", "🏛️")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Yeni İmtahan Siyahısı 🎓",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Emoji picker
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    emojis.take(5).forEach { e ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    if (emoji == e) Coral.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (emoji == e) Coral else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(e, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    emojis.drop(5).forEach { e ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    if (emoji == e) Coral.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (emoji == e) Coral else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(e, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Fənn adı", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = daysUntil,
                    onValueChange = { daysUntil = it },
                    label = { Text("Neçə gün qalıb?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = topicsText,
                    onValueChange = { topicsText = it },
                    label = { Text("Mövzular (vergüllə ayırın)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    placeholder = { Text("məs. Törəmə, İnteqral, Limit", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Ləğv et", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (subject.isNotBlank()) {
                                val topicsList = topicsText.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                onAdd(
                                    subject,
                                    emoji,
                                    daysUntil.toIntOrNull() ?: 7,
                                    topicsList
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Yarat", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
