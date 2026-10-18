package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StudyLogDay
import com.example.model.Subject
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.TealAccent
import com.example.viewmodel.KaizenTab
import java.util.Random

@Composable
fun CircularProgressRing(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp,
    primaryColor: Color = TealAccent,
    secondaryColor: Color = IndigoAccent,
    trackColor: Color = Color(0xFF1E2846),
    centerContent: @Composable () -> Unit
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated progress arc
            val sweep = animatedProgress.value * 360f
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(primaryColor, secondaryColor, primaryColor),
                        center = Offset(size.width / 2, size.height / 2)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        centerContent()
    }
}

@Composable
fun StudyTrendBarChart(
    logs: List<StudyLogDay>,
    dailyGoalHours: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = TealAccent
) {
    val recent7Days = remember(logs) {
        logs.takeLast(7)
    }

    val maxHours = remember(recent7Days, dailyGoalHours) {
        val maxLogHours = recent7Days.maxOfOrNull { it.totalMinutes / 60f } ?: 0f
        maxOf(maxLogHours, dailyGoalHours, 6f) * 1.15f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCardBg)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "7-Day Study Trend",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Daily Goal: ${String.format("%.1f", dailyGoalHours)}h",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Consistent",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val chartHeight = maxHeight

            // Target line
            val goalYFraction = (1f - (dailyGoalHours / maxHours)).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height * goalYFraction
                drawLine(
                    color = Color(0xFFFBBF24).copy(alpha = 0.6f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                recent7Days.forEachIndexed { index, day ->
                    val isToday = index == recent7Days.lastIndex
                    val hours = day.totalMinutes / 60f
                    val heightRatio = (hours / maxHours).coerceIn(0.04f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (hours > 0) "${String.format("%.1f", hours)}h" else "0",
                            color = if (isToday) accentColor else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(if (isToday) 18.dp else 14.dp)
                                .height(chartHeight * heightRatio * 0.75f)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isToday) {
                                        Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.6f)))
                                    } else {
                                        Brush.verticalGradient(listOf(IndigoAccent.copy(alpha = 0.8f), IndigoAccent.copy(alpha = 0.3f)))
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val dayLabel = try {
                            day.date.takeLast(2)
                        } catch (_: Exception) { "D" }
                        Text(
                            text = if (isToday) "Today" else dayLabel,
                            color = if (isToday) Color.White else Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KaizenFloatingNavBar(
    currentTab: KaizenTab,
    onTabSelected: (KaizenTab) -> Unit,
    accentColor: Color = TealAccent
) {
    Surface(
        color = Color(0xEE0B1020),
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 12.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .fillMaxWidth()
            .border(1.dp, Color(0xFF232D4B), RoundedCornerShape(26.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Pair(KaizenTab.HOME, Icons.Default.Dashboard to "Home"),
                Pair(KaizenTab.BACKLOG, Icons.Default.FormatListBulleted to "Backlog"),
                Pair(KaizenTab.PLANNER, Icons.Default.CalendarMonth to "Planner"),
                Pair(KaizenTab.TIMER, Icons.Default.Alarm to "Timer"),
                Pair(KaizenTab.MENU, Icons.Default.Menu to "Menu"),
                Pair(KaizenTab.SETTINGS, Icons.Default.Settings to "Settings")
            )

            items.forEach { (tab, iconTitle) ->
                val (icon, title) = iconTitle
                val isSelected = currentTab == tab

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onTabSelected(tab) }
                        .testTag("nav_tab_${tab.name.lowercase()}")
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            } else {
                                Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isSelected) accentColor else Color(0xFF64748B),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = message,
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_delete_btn")
            ) {
                Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun ChipLinkActionDialog(
    chipName: String,
    currentLink: String?,
    isDone: Boolean,
    onToggleDone: () -> Unit,
    onSaveLink: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var linkInput by remember { mutableStateOf(currentLink ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$chipName Resource",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Surface(
                    color = if (isDone) TealAccent.copy(alpha = 0.2f) else Color(0xFF334155),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isDone) "COMPLETED" else "PENDING",
                        color = if (isDone) TealAccent else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Link or Note URL:",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = linkInput,
                    onValueChange = { linkInput = it },
                    placeholder = { Text("https://neetprep.com/dpp...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            onToggleDone()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isDone) Color.Gray else TealAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDone) "Mark Undone" else "Just Mark Done",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    if (!currentLink.isNullOrBlank()) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentLink))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveLink(linkInput.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Link", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun CelebrationOverlay(
    triggerTimestamp: Long,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(triggerTimestamp) {
        if (triggerTimestamp > 0L) {
            visible = true
            kotlinx.coroutines.delay(2200L)
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + scaleIn(tween(250)),
        exit = fadeOut(tween(400)) + scaleOut(tween(300)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x55000000)),
            contentAlignment = Alignment.Center
        ) {
            // Animated burst rings & confetti
            Canvas(modifier = Modifier.size(240.dp)) {
                val rnd = Random(triggerTimestamp)
                val colors = listOf(TealAccent, IndigoAccent, Color(0xFFFBBF24), Color(0xFFF43F5E), Color(0xFF10B981))
                for (i in 0 until 24) {
                    val angle = (i * (360f / 24f)) * (Math.PI / 180f)
                    val dist = rnd.nextFloat() * (size.width / 2.2f) + 30f
                    val x = (size.width / 2) + Math.cos(angle).toFloat() * dist
                    val y = (size.height / 2) + Math.sin(angle).toFloat() * dist
                    val pColor = colors[rnd.nextInt(colors.size)]
                    drawCircle(color = pColor, radius = rnd.nextFloat() * 6f + 4f, center = Offset(x, y))
                }
            }

            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, TealAccent),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(TealAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TealAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Lecture Completed!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Consistent effort leads to AIR 1 🩺",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ChipBadge(
    label: String,
    hasLink: Boolean,
    isDone: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isDone) TealAccent.copy(alpha = 0.2f) else if (hasLink) IndigoAccent.copy(alpha = 0.2f) else Color(0xFF1E2846),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDone) TealAccent.copy(alpha = 0.5f) else if (hasLink) IndigoAccent.copy(alpha = 0.5f) else Color(0xFF334155)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TealAccent, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = if (hasLink) "$label ↗" else label,
                color = if (isDone) TealAccent else if (hasLink) Color(0xFFC7D2FE) else Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

