package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CircularProgressRing
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceSubtle
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.TealAccent
import com.example.viewmodel.KaizenUiState
import com.example.viewmodel.KaizenViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimerScreen(
    uiState: KaizenUiState,
    viewModel: KaizenViewModel,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    var showCustomPresetDialog by remember { mutableStateOf(false) }

    // Keep screen awake effect
    DisposableEffect(uiState.timerKeepScreenAwake, uiState.timerIsRunning) {
        val activity = context as? Activity
        if (activity != null && uiState.timerKeepScreenAwake && uiState.timerIsRunning) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Today's study time
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayLog = uiState.studyLogs.find { it.date == todayStr }
    val todayStudyMinutes = todayLog?.totalMinutes ?: 0
    val todayStudyHours = todayStudyMinutes / 60f
    val goalHours = uiState.settings.dailyStudyGoalHours
    val goalProgress = if (goalHours > 0) (todayStudyHours / goalHours).coerceIn(0f, 1f) else 0f

    // Timer ring values
    val (displayMinutes, displaySeconds, progressFraction) = if (uiState.timerMode == "COUNTDOWN") {
        val rem = uiState.timerRemainingSeconds
        val total = (uiState.timerPresetMinutes * 60).coerceAtLeast(1)
        val m = rem / 60
        val s = rem % 60
        val frac = (rem.toFloat() / total).coerceIn(0f, 1f)
        Triple(m, s, frac)
    } else {
        val el = uiState.timerElapsedSeconds
        val m = el / 60
        val s = el % 60
        val frac = ((el % 3600) / 3600f)
        Triple(m, s, frac)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP: Mode Switcher & Presets
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Mode Selector: Countdown / Stopwatch
            Row(
                modifier = Modifier
                    .background(DarkCardBg, RoundedCornerShape(14.dp))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isCountdown = uiState.timerMode == "COUNTDOWN"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCountdown) TealAccent.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { viewModel.setTimerMode("COUNTDOWN") }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Countdown",
                        color = if (isCountdown) TealAccent else Color(0xFF94A3B8),
                        fontWeight = if (isCountdown) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isCountdown) TealAccent.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { viewModel.setTimerMode("STOPWATCH") }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Stopwatch",
                        color = if (!isCountdown) TealAccent else Color(0xFF94A3B8),
                        fontWeight = if (!isCountdown) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Presets row (only for countdown)
            if (uiState.timerMode == "COUNTDOWN") {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25, 50, 90).forEach { mins ->
                        val isSel = uiState.timerPresetMinutes == mins
                        Surface(
                            color = if (isSel) IndigoAccent else DarkCardBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) IndigoAccent else DarkCardBorder),
                            modifier = Modifier.clickable { viewModel.setTimerPreset(mins) }
                        ) {
                            Text(
                                text = "${mins}m",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Custom preset button
                    Surface(
                        color = DarkCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.clickable { showCustomPresetDialog = true }
                    ) {
                        Text(
                            text = "Custom",
                            color = TealAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.subjects.forEach { s ->
                    val isSel = s.id == uiState.timerSubjectId
                    val subColor = try { Color(android.graphics.Color.parseColor(s.colorHex)) } catch (_: Exception) { TealAccent }
                    Surface(
                        color = if (isSel) subColor.copy(alpha = 0.25f) else DarkCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) subColor else DarkCardBorder),
                        modifier = Modifier.clickable { viewModel.setTimerSubject(s.id) }
                    ) {
                        Text(
                            text = s.name,
                            color = if (isSel) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // CENTER: BIG PROGRESS RING & TIMER DISPLAY
        CircularProgressRing(
            progress = progressFraction,
            modifier = Modifier.size(240.dp),
            strokeWidth = 14.dp,
            primaryColor = TealAccent,
            secondaryColor = IndigoAccent
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val timeFormatted = String.format("%02d:%02d", displayMinutes, displaySeconds)
                Text(
                    text = timeFormatted,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                val selSubject = uiState.subjects.find { it.id == uiState.timerSubjectId }
                Text(
                    text = selSubject?.name ?: "Physics",
                    color = TealAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (uiState.timerIsRunning) "Focusing..." else "Paused",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        // BOTTOM CONTROLS & EXTRAS
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Action Buttons (Play / Pause / Reset / Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                Surface(
                    color = DarkCardBg,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { viewModel.resetTimer() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Play / Pause FAB
                Surface(
                    color = TealAccent,
                    shape = CircleShape,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(68.dp)
                        .clickable {
                            if (uiState.timerIsRunning) viewModel.pauseTimer() else viewModel.startTimer()
                        }
                        .testTag("timer_toggle_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (uiState.timerIsRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.timerIsRunning) "Pause" else "Start",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Save to Log Button
                Surface(
                    color = IndigoAccent,
                    shape = CircleShape,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { viewModel.saveTimerSessionToLog() }
                        .testTag("timer_save_log_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Save, contentDescription = "Save to log", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Focus Ambient Sound & Screen Awake Row
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Focus Sound:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Sound choices: Off, White, Brown, Rain
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("none" to "Off", "white" to "White", "brown" to "Brown", "rain" to "Rain").forEach { (type, lbl) ->
                                val isSel = uiState.timerAmbientSound == type
                                Surface(
                                    color = if (isSel) TealAccent.copy(alpha = 0.25f) else Color(0xFF1E2846),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable { viewModel.setAmbientSound(type) }
                                ) {
                                    Text(
                                        text = lbl,
                                        color = if (isSel) TealAccent else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.StayCurrentPortrait, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Keep screen awake (WakeLock)", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }

                        Switch(
                            checked = uiState.timerKeepScreenAwake,
                            onCheckedChange = { viewModel.toggleKeepScreenAwake() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TealAccent,
                                checkedTrackColor = TealAccent.copy(alpha = 0.3f),
                                uncheckedTrackColor = Color(0xFF1E2846)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Today's Study vs Daily Goal Progress Bar
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Today's Progress", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(
                            "${String.format("%.1f", todayStudyHours)}h / ${goalHours.toInt()}h goal",
                            color = TealAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TealAccent,
                        trackColor = Color(0xFF1E2846)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Custom Preset Dialog
    if (showCustomPresetDialog) {
        var customMinsText by remember { mutableStateOf("45") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCustomPresetDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Custom Timer Preset", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                OutlinedTextField(
                    value = customMinsText,
                    onValueChange = { customMinsText = it },
                    label = { Text("Minutes") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TealAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = customMinsText.toIntOrNull() ?: 25
                        viewModel.setTimerPreset(mins.coerceIn(1, 300))
                        showCustomPresetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Set", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomPresetDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
