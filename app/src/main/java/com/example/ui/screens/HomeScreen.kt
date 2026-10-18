package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lecture
import com.example.model.Subject
import com.example.ui.components.ChipBadge
import com.example.ui.components.ChipLinkActionDialog
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.StudyTrendBarChart
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceSubtle
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.TealAccent
import com.example.viewmodel.KaizenTab
import com.example.viewmodel.KaizenUiState
import com.example.viewmodel.KaizenViewModel
import com.example.viewmodel.MenuSubScreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: KaizenUiState,
    viewModel: KaizenViewModel,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    var selectedChipLecture by remember { mutableStateOf<Pair<Lecture, String>?>(null) }
    var showPickDialog by remember { mutableStateOf(false) }

    // Exam countdown tick
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val examTimeDiff = remember(currentTime, uiState.settings.examDate) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val examDateObj = try { sdf.parse(uiState.settings.examDate) } catch (_: Exception) { null }
        val target = examDateObj?.time ?: (currentTime + 86400000L * 300)
        maxOf(0L, target - currentTime)
    }

    val daysLeft = examTimeDiff / (1000 * 60 * 60 * 24)
    val hoursLeft = (examTimeDiff / (1000 * 60 * 60)) % 24
    val minutesLeft = (examTimeDiff / (1000 * 60)) % 60
    val secondsLeft = (examTimeDiff / 1000) % 60

    // Stats calculations
    val totalLectures = uiState.lectures.size
    val doneLectures = uiState.lectures.count { it.status == "done" }
    val pendingLectures = totalLectures - doneLectures
    val overallPercent = if (totalLectures > 0) (doneLectures.toFloat() / totalLectures) else 0f

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val doneToday = uiState.lectures.count { lec ->
        lec.status == "done" && lec.completedAt != null &&
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(lec.completedAt)) == todayStr
    }

    val todayLog = uiState.studyLogs.find { it.date == todayStr }
    val todayStudyMinutes = todayLog?.totalMinutes ?: 0
    val todayStudyHours = todayStudyMinutes / 60f
    val goalHours = uiState.settings.dailyStudyGoalHours

    // Streak calculation (consecutive active days)
    val streakDays = remember(uiState.studyLogs) {
        var count = 0
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        while (true) {
            val dStr = sdf.format(cal.time)
            val log = uiState.studyLogs.find { it.date == dStr }
            if (log != null && log.totalMinutes > 0) {
                count++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else if (dStr == todayStr) {
                // If today hasn't been logged yet, check yesterday
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        maxOf(1, count)
    }

    // Dynamic greeting based on time of day
    val greeting = remember(currentTime) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    // Today's Must Do lectures
    val todayMustDo = uiState.lectures.filter { it.todayDate == todayStr }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // 1. TOP PROFILE ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setProfileDialog(true) }
                    .testTag("home_profile_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val initials = uiState.settings.userName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(TealAccent, IndigoAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (initials.isNotEmpty()) initials else "K",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    Column {
                        Text(
                            text = "$greeting,",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Text(
                            text = uiState.settings.userName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // TARGET BADGE
                Surface(
                    color = Color(0x336366F1),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "TARGET: MAY 2028",
                        color = Color(0xFFC7D2FE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 2. COUNTDOWN & PROGRESS CARD
        item {
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.settings.examName.uppercase(),
                            color = TealAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$daysLeft DAYS",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${hoursLeft}h ${minutesLeft}m ${secondsLeft}s to exam",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$doneLectures of $totalLectures lectures completed",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    }

                    CircularProgressRing(
                        progress = overallPercent,
                        modifier = Modifier.size(92.dp),
                        strokeWidth = 10.dp,
                        primaryColor = TealAccent,
                        secondaryColor = IndigoAccent
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(overallPercent * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "done",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. STATS TILE (4 COLUMNS SPLIT BY THIN DIVIDERS)
        item {
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatColumn(label = "Pending", value = "$pendingLectures")
                    StatDivider()
                    StatColumn(label = "Done Today", value = "$doneToday")
                    StatDivider()
                    StatColumn(label = "Study Today", value = "${String.format("%.1f", todayStudyHours)}/${goalHours.toInt()}h")
                    StatDivider()
                    StatColumn(label = "Streak", value = "${streakDays}d 🔥")
                }
            }
        }

        // 4. 7-DAY STUDY TREND BAR CHART
        item {
            StudyTrendBarChart(
                logs = uiState.studyLogs,
                dailyGoalHours = uiState.settings.dailyStudyGoalHours,
                accentColor = TealAccent
            )
        }

        // 5. SUBJECT TILES (3 per row grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "NEET Subjects",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                val subjects = uiState.subjects
                val rows = subjects.chunked(3)
                rows.forEach { rowSubjects ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSubjects.forEach { sub ->
                            val subLecs = uiState.lectures.filter { it.subjectId == sub.id }
                            val totalSub = subLecs.size
                            val doneSub = subLecs.count { it.status == "done" }
                            val pendingSub = totalSub - doneSub
                            val pct = if (totalSub > 0) (doneSub.toFloat() / totalSub) else 0f
                            val subColor = try { Color(android.graphics.Color.parseColor(sub.colorHex)) } catch (_: Exception) { TealAccent }

                            Surface(
                                color = DarkCardBg,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.setBacklogSubjectFilter(sub.id)
                                        viewModel.navigateToTab(KaizenTab.BACKLOG)
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(subColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = sub.name,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$pendingSub left",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${(pct * 100).toInt()}% done",
                                        color = subColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = subColor,
                                        trackColor = Color(0xFF1E2846)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. FOUR QUICK BUTTONS (Timer, Backlog, Mistakes, Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionBtn(
                    title = "Timer",
                    icon = Icons.Default.Alarm,
                    color = TealAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateToTab(KaizenTab.TIMER) }
                )
                QuickActionBtn(
                    title = "Backlog",
                    icon = Icons.Default.FormatListBulleted,
                    color = IndigoAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateToTab(KaizenTab.BACKLOG) }
                )
                QuickActionBtn(
                    title = "Mistakes",
                    icon = Icons.Default.ErrorOutline,
                    color = Color(0xFFF43F5E),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.navigateToTab(KaizenTab.MENU)
                        viewModel.navigateToMenuSubScreen(MenuSubScreen.MISTAKES)
                    }
                )
                QuickActionBtn(
                    title = "Cards",
                    icon = Icons.Default.Style,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.navigateToTab(KaizenTab.MENU)
                        viewModel.navigateToMenuSubScreen(MenuSubScreen.FLASHCARDS)
                    }
                )
            }
        }

        // 7. TODAY'S MUST-DO HEADER & CARDS
        item {
            val mustDoCount = todayMustDo.size
            val mustDoDone = todayMustDo.count { it.status == "done" }
            val mustDoPct = if (mustDoCount > 0) (mustDoDone.toFloat() / mustDoCount) else 0f

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Today's Must-Do",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF1E2846),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$mustDoDone/$mustDoCount",
                                    color = TealAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (mustDoCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { mustDoPct },
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = TealAccent,
                                trackColor = Color(0xFF1E2846)
                            )
                        }
                    }

                    // "+ Pick" gradient button
                    Button(
                        onClick = { showPickDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(TealAccent, IndigoAccent)))
                            .testTag("pick_today_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Pick", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        if (todayMustDo.isEmpty()) {
            item {
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No lectures picked for today yet",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Pick' to choose high-yield lectures to conquer today!",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(todayMustDo, key = { it.id }) { lecture ->
                val subject = uiState.subjects.find { it.id == lecture.subjectId }
                val subColor = try { Color(android.graphics.Color.parseColor(subject?.colorHex ?: "#14B8A6")) } catch (_: Exception) { TealAccent }
                val isDone = lecture.status == "done"

                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDone) TealAccent.copy(alpha = 0.4f) else DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_lecture_${lecture.id}")
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Colour strip on left
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(104.dp)
                                .background(subColor)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = subColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = subject?.name ?: "NEET",
                                            color = subColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = lecture.chapter,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Checkbox(
                                    checked = isDone,
                                    onCheckedChange = { viewModel.toggleLectureStatus(lecture.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = TealAccent,
                                        uncheckedColor = Color(0xFF64748B)
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lecture.title,
                                color = if (isDone) Color(0xFF94A3B8) else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${lecture.durationMinutes}m @${lecture.speed}x (${lecture.effectiveDurationMinutes}m)",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ChipBadge(label = "DPP", hasLink = !lecture.links.dpp.isNullOrBlank(), isDone = lecture.flags.dpp) {
                                        selectedChipLecture = lecture to "DPP"
                                    }
                                    ChipBadge(label = "Notes", hasLink = !lecture.links.notes.isNullOrBlank(), isDone = lecture.flags.notes) {
                                        selectedChipLecture = lecture to "Notes"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    // Chip Link Action Dialog
    selectedChipLecture?.let { (lecture, chipType) ->
        val link = when (chipType.uppercase()) {
            "DPP" -> lecture.links.dpp
            "NOTES" -> lecture.links.notes
            "HW" -> lecture.links.hw
            else -> lecture.links.extraQs
        }
        val isDone = when (chipType.uppercase()) {
            "DPP" -> lecture.flags.dpp
            "NOTES" -> lecture.flags.notes
            "HW" -> lecture.flags.hw
            else -> lecture.flags.extraQs
        }
        ChipLinkActionDialog(
            chipName = chipType,
            currentLink = link,
            isDone = isDone,
            onToggleDone = { viewModel.updateLectureChip(lecture.id, chipType, null, toggleDone = true) },
            onSaveLink = { newUrl -> viewModel.updateLectureChip(lecture.id, chipType, newUrl, toggleDone = false) },
            onDismiss = { selectedChipLecture = null }
        )
    }

    // Pick for Today Dialog
    if (showPickDialog) {
        val pendingLecturesList = uiState.lectures.filter { it.status != "done" }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPickDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Pick for Today's Must-Do",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pendingLecturesList.isEmpty()) {
                        item {
                            Text(
                                text = "All backlog cleared! Great job.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        items(pendingLecturesList, key = { it.id }) { lec ->
                            val sub = uiState.subjects.find { it.id == lec.subjectId }
                            val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#14B8A6")) } catch (_: Exception) { TealAccent }
                            val isPicked = lec.todayDate == todayStr

                            Surface(
                                color = if (isPicked) TealAccent.copy(alpha = 0.15f) else DarkCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isPicked) TealAccent else DarkCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleTodayPick(lec.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${sub?.name ?: ""} • ${lec.chapter}",
                                            color = subColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = lec.title,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${lec.durationMinutes}m (${lec.effectiveDurationMinutes}m @${lec.speed}x)",
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    }
                                    Checkbox(
                                        checked = isPicked,
                                        onCheckedChange = { viewModel.toggleTodayPick(lec.id) },
                                        colors = CheckboxDefaults.colors(checkedColor = TealAccent)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPickDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 10.sp
        )
    }
}

@Composable
fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color(0xFF232D4B))
    )
}

@Composable
fun QuickActionBtn(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = DarkCardBg,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
