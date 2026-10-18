package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lecture
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceSubtle
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.TealAccent
import com.example.viewmodel.KaizenUiState
import com.example.viewmodel.KaizenViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PlannerScreen(
    uiState: KaizenUiState,
    viewModel: KaizenViewModel,
    contentPadding: PaddingValues
) {
    var lectureToMove by remember { mutableStateOf<Lecture?>(null) }

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displaySdf = remember { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()) }

    val todayCalendar = remember { Calendar.getInstance() }
    val todayDateStr = remember { sdf.format(todayCalendar.time) }

    // Catch-up forecast calculation
    val pendingLectures = uiState.lectures.filter { it.status != "done" }
    val totalPendingMinutes = pendingLectures.sumOf { it.effectiveDurationMinutes }
    val dailyGoalMinutes = (uiState.settings.dailyStudyGoalHours * 60).toInt().coerceAtLeast(60)
    val daysNeeded = if (dailyGoalMinutes > 0) ((totalPendingMinutes + dailyGoalMinutes - 1) / dailyGoalMinutes) else 1

    val forecastCalendar = remember(daysNeeded) {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysNeeded) }
    }
    val forecastDateStr = remember(forecastCalendar) {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(forecastCalendar.time)
    }

    // Generate next 7 days for the Day-wise plan
    val dayList = remember(todayDateStr) {
        val list = mutableListOf<Triple<String, String, String>>() // key (YYYY-MM-DD), label ("Today", "Tomorrow", "Wed 21 Oct"), full date
        val cal = Calendar.getInstance()
        for (i in 0 until 7) {
            val key = sdf.format(cal.time)
            val label = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> displaySdf.format(cal.time)
            }
            val formatted = displaySdf.format(cal.time)
            list.add(Triple(key, label, formatted))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Smart Planner",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Auto-distribute backlog based on daily goal",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { viewModel.autoScheduleBacklog() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(TealAccent, IndigoAccent)))
                        .testTag("auto_schedule_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto Plan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // CATCH-UP FORECAST CARD
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
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(TealAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = TealAccent, modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BACKLOG CATCH-UP FORECAST",
                            color = TealAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (pendingLectures.isEmpty()) "Backlog Cleared! 🎉" else "Cleared by $forecastDateStr",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalPendingMinutes mins pending (~${String.format("%.1f", totalPendingMinutes / 60f)}h) at ${uiState.settings.dailyStudyGoalHours.toInt()}h/day",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // DAY-WISE PLANNER
        dayList.forEach { (dateKey, label, fullDateStr) ->
            val dayLectures = uiState.lectures.filter { it.plannedDate == dateKey || (dateKey == todayDateStr && it.todayDate == todayDateStr && it.plannedDate == null) }
            val dayTotalMins = dayLectures.sumOf { it.effectiveDurationMinutes }
            val dayDoneCount = dayLectures.count { it.status == "done" }
            val isToday = dateKey == todayDateStr

            item(key = dateKey) {
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isToday) TealAccent.copy(alpha = 0.5f) else DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isToday) TealAccent else IndigoAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isToday) Color.Black else Color(0xFFC7D2FE),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fullDateStr,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = "${String.format("%.1f", dayTotalMins / 60f)}h planned (${dayDoneCount}/${dayLectures.size})",
                                color = if (isToday) TealAccent else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (dayLectures.isEmpty()) {
                            Text(
                                text = "No lectures scheduled. Tap 'Auto Plan' or move from Backlog.",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                dayLectures.forEach { lec ->
                                    val sub = uiState.subjects.find { it.id == lec.subjectId }
                                    val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#14B8A6")) } catch (_: Exception) { TealAccent }
                                    val isDone = lec.status == "done"

                                    Surface(
                                        color = DarkSurfaceSubtle,
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDone) TealAccent.copy(alpha = 0.3f) else DarkCardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isDone,
                                                    onCheckedChange = { viewModel.toggleLectureStatus(lec.id) },
                                                    colors = CheckboxDefaults.colors(checkedColor = TealAccent),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(subColor, CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "${sub?.name ?: ""} • ${lec.chapter}",
                                                            color = subColor,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Text(
                                                        text = lec.title,
                                                        color = if (isDone) Color(0xFF94A3B8) else Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${lec.durationMinutes}m (${lec.effectiveDurationMinutes}m @${lec.speed}x)",
                                                        color = Color(0xFF64748B),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            // Move to other day button
                                            Surface(
                                                color = Color(0xFF1E2846),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.clickable { lectureToMove = lec }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.SwapHoriz, contentDescription = "Move day", tint = Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Move", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // Move Lecture Dialog
    lectureToMove?.let { lec ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { lectureToMove = null },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text("Move Lecture to Day", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Move '${lec.title.take(24)}' to:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    dayList.forEach { (dKey, lbl, fullDate) ->
                        Surface(
                            color = DarkCardBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.moveLectureToDate(lec.id, dKey)
                                    lectureToMove = null
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lbl, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(fullDate, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { lectureToMove = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
