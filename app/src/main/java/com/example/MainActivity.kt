package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PlannerViewModel
import com.example.ui.components.*
import com.example.ui.theme.AppTheme
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PlannerViewModel = viewModel()
            val currentAppTheme by viewModel.appTheme.collectAsState()

            MyApplicationTheme(appTheme = currentAppTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlannerMainShell(viewModel)
                }
            }
        }
    }
}

@Composable
fun PlannerMainShell(viewModel: PlannerViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isRightPanelOpen by viewModel.isRightPanelOpen.collectAsState()
    val isFocusDimMode by viewModel.isPomodoroFocusMode.collectAsState()
    val timerRunning by viewModel.isPomodoroRunning.collectAsState()

    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp > 600
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Base background fill
                drawRect(color = colorScheme.background)

                // Large smooth radial glow - Indigo/Primary Top Left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * -0.1f, size.height * -0.1f),
                        radius = size.minDimension * 0.95f
                    ),
                    radius = size.minDimension * 0.95f,
                    center = Offset(size.width * -0.1f, size.height * -0.1f)
                )

                // Large smooth radial glow - Violet/Secondary Bottom Right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorScheme.secondary.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 1.1f, size.height * 0.9f),
                        radius = size.minDimension * 0.95f
                    ),
                    radius = size.minDimension * 0.95f,
                    center = Offset(size.width * 1.1f, size.height * 0.9f)
                )

                // Moderate smooth radial glow - Tertiary Left Center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorScheme.tertiary.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * -0.2f, size.height * 0.5f),
                        radius = size.minDimension * 0.7f
                    ),
                    radius = size.minDimension * 0.7f,
                    center = Offset(size.width * -0.2f, size.height * 0.5f)
                )
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT NAVIGATION SIDEBAR (Only displayed on Medium/Expanded screen widths)
            if (isLargeScreen) {
                PlannerSidebar(
                    activeTab = currentTab,
                    onTabSelected = { viewModel.currentTab.value = it }
                )
            }

            // PRIMARY DATA & VIEWPORT COLUMN
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(bottom = if (!isLargeScreen) 80.dp else 0.dp) // Bottom padding for Mobile BottomNav
            ) {
                // TOP ACTION AND USER DETAILS BAR
                PlannerTopBar(viewModel = viewModel)

                // ACTIVE VIEW CONTAINER CHOSEN
                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        "Today" -> TodayPlannerView(viewModel)
                        "Upcoming" -> UpcomingView(viewModel)
                        "Calendar" -> CalendarView(viewModel)
                        "Stats" -> StatsDashboard(viewModel)
                        "Settings" -> SettingsPanel(viewModel)
                    }
                }
            }

            // COLLAPSIBLE RIGHT WORKSPACE DETAILS/TIMER PANEL
            AnimatedVisibility(
                visible = isRightPanelOpen,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                TaskDetailsPanel(
                    viewModel = viewModel,
                    modifier = Modifier.width(if (isLargeScreen) 380.dp else 300.dp)
                )
            }
        }

        // MOBILE PERSISTENT BOTTOM TAB NAVIGATION BAR (Compact display modes)
        if (!isLargeScreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                PlannerBottomNavBar(
                    activeTab = currentTab,
                    onTabSelected = { viewModel.currentTab.value = it }
                )
            }
        }

        // IMMERSIVE COOLDOWN DIM OVERLAY (Focus Mode)
        if (isFocusDimMode && timerRunning) {
            FocusOverlayDimScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun PlannerTopBar(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    var isQuickAddOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "FlowPlan AI",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(8.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "tagizade6002@gmail.com",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Fast Quick-Add Task Button
        Button(
            onClick = { isQuickAddOpen = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp).testTag("quick_add_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Quick Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Theme Toggle Button
        IconButton(
            onClick = {
                val nextTheme = when (appTheme) {
                    AppTheme.LIGHT -> AppTheme.DARK
                    AppTheme.DARK -> AppTheme.SUNSET
                    AppTheme.SUNSET -> AppTheme.LIGHT
                }
                viewModel.changeTheme(nextTheme)
            },
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                .size(36.dp)
                .testTag("theme_toggle_btn")
        ) {
            val themeIcon = when (appTheme) {
                AppTheme.LIGHT -> Icons.Default.LightMode
                AppTheme.DARK -> Icons.Default.DarkMode
                AppTheme.SUNSET -> Icons.Default.WbTwilight
            }
            Icon(
                imageVector = themeIcon,
                contentDescription = "Toggle color scheme",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (isQuickAddOpen) {
        QuickAddTaskDialog(
            onDismiss = { isQuickAddOpen = false },
            onSave = { title, category, priority, min, timeSlot, emoji ->
                viewModel.addTaskFast(title, category, priority, min, timeSlot, emoji)
                isQuickAddOpen = false
            }
        )
    }
}

@Composable
fun TodayPlannerView(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.todayTasks.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTask by viewModel.selectedTask.collectAsState()
    val aiReview by viewModel.aiDailyReviewText.collectAsState()

    val completedTasks = tasks.filter { it.isCompleted }
    val totalCount = tasks.size
    val ratio = if (totalCount > 0) completedTasks.size.toFloat() / totalCount else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI SMART PARSER FIELD
        item {
            SmartTaskInputBar(viewModel = viewModel)
        }

        // MOTIVATIONAL TODAY RATIO BAR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Focus Target: $selectedDate",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Completed ${completedTasks.size} / $totalCount tasks today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = String.format(Locale.getDefault(), "%d%%", (ratio * 100).toInt()),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = ratio,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        // DAILY COMPLETED vs MISSED RECOGNITION PANEL
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "End of Day Review",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Button(
                    onClick = { viewModel.generateDailyReview() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Review Progress", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            aiReview?.let { review ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = review,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // DENSE TASKS LIST
        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAddCheck,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Inbox clear! Try writing or parsing tasks.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(tasks) { task ->
                val isSelected = selectedTask?.id == task.id
                TaskRowItem(
                    task = task,
                    isSelected = isSelected,
                    onToggleSelect = { viewModel.selectTask(task) },
                    onToggleComplete = { viewModel.toggleTaskCompletion(task) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FocusOverlayDimScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A).copy(alpha = 0.95f))
            .clickable(enabled = false) {}
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "IMMERSIVE FOCUS MODE",
                fontSize = 11.sp,
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Render central focused clock
            PomodoroCircularClock(viewModel = viewModel)

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { viewModel.togglePomodoroFocusMode() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Text("Exit Focus Screen", color = Color.White)
            }
        }
    }
}

@Composable
fun PlannerSidebar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        Pair("Today", Icons.Default.EventNote),
        Pair("Upcoming", Icons.Default.Splitscreen),
        Pair("Calendar", Icons.Default.CalendarMonth),
        Pair("Stats", Icons.Default.Analytics),
        Pair("Settings", Icons.Default.Settings)
    )

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        modifier = modifier
            .fillMaxHeight()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.outline
                    )
                ),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        navItems.forEach { (tab, icon) ->
            val isSelected = activeTab == tab
            NavigationRailItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$tab menu",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                            alpha = 0.6f
                        )
                    )
                },
                label = { Text(tab, fontSize = 11.sp) },
                alwaysShowLabel = true,
                modifier = Modifier.padding(vertical = 4.dp).testTag("rail_tab_$tab")
            )
        }
    }
}

@Composable
fun PlannerBottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        Pair("Today", Icons.Default.EventNote),
        Pair("Upcoming", Icons.Default.Splitscreen),
        Pair("Calendar", Icons.Default.CalendarMonth),
        Pair("Stats", Icons.Default.Analytics),
        Pair("Settings", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.outline,
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .navigationBarsPadding()
    ) {
        navItems.forEach { (tab, icon) ->
            val isSelected = activeTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                            alpha = 0.6f
                        )
                    )
                },
                label = { Text(tab, fontSize = 10.sp) },
                modifier = Modifier.testTag("bottom_tab_$tab")
            )
        }
    }
}

@Composable
fun QuickAddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, String?, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var priority by remember { mutableStateOf("Medium") }
    var duration by remember { mutableStateOf("25") }
    var timeSlot by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📝") }

    val categories = listOf("Personal", "Work", "Study", "Health", "Other")
    val priorities = listOf("Low", "Medium", "High", "Urgent")
    val emojis = listOf("📝", "💻", "📚", "📞", "🏃‍♂️", "🛒", "📖", "💡")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Add Task", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_title_input")
                )

                // Category select
                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                // Priority select
                Text("Priority:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    priorities.forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio, fontSize = 10.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Est. Mins") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        label = { Text("Time (HH:MM / empty)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Emoji picker
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    emojis.forEach { em ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (emoji == em) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { emoji = em }
                        ) {
                            Text(text = em, fontSize = 18.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val durationMinutes = duration.toIntOrNull() ?: 25
                        val finalTimeSlot = if (timeSlot.isNotBlank()) timeSlot else null
                        onSave(title, category, priority, durationMinutes, finalTimeSlot, emoji)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Insert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingsPanel(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    var apiKeyText by remember { mutableStateOf(viewModel.getCustomApiKey()) }
    var customGoal by remember { mutableStateOf(viewModel.pomodoroGoal.value.toString()) }
    var statusFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Workspace Configuration",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Fine-tune AI behavior and Pomodoro target rates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        // CUSTOM GEMINI API KEY SETUP
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Gemini API Connection",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter your custom Gemini API Key to enable raw natural language parsing. If left empty, the system automatically uses the preconfigured key or fallback local regex parser.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        label = { Text("GEMINI_API_KEY") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_apikey_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.setCustomApiKey(apiKeyText)
                            statusFeedback = "Configuration successfully saved!"
                        },
                        modifier = Modifier.align(Alignment.End).testTag("save_apikey_btn")
                    ) {
                        Text("Connect API")
                    }
                }
            }
        }

        // GOALS AND ARCHIVE PREFERENCE CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "System Preferences",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customGoal,
                        onValueChange = { customGoal = it },
                        label = { Text("Daily Pomodoro sessions target") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Automatic Task Archive", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Delete completed tasks older than 7 days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                        Button(
                            onClick = {
                                viewModel.archiveCompletedTasks(7)
                                statusFeedback = "Older completed tasks archived!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                        ) {
                            Text("Archive Now", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val target = customGoal.toIntOrNull() ?: 4
                            viewModel.pomodoroGoal.value = target
                            statusFeedback = "Preferences successfully updated!"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save System Preferences")
                    }
                }
            }
        }

        // ACCOUNT SUMMARY CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("FlowPlan Premium Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("tagizade6002@gmail.com (Verified User)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
            }
        }

        statusFeedback?.let { feedback ->
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(feedback, style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
                }
            }
        }
    }
}
