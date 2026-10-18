package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Flashcard
import com.example.model.MistakeItem
import com.example.model.MockTest
import com.example.model.Subject
import com.example.ui.components.ConfirmDeleteDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MenuHubScreen(
    uiState: KaizenUiState,
    viewModel: KaizenViewModel,
    contentPadding: PaddingValues
) {
    if (uiState.menuSubScreen != null) {
        when (uiState.menuSubScreen) {
            MenuSubScreen.REVISE -> ReviseScreen(uiState, viewModel, contentPadding)
            MenuSubScreen.MISTAKES -> MistakesScreen(uiState, viewModel, contentPadding)
            MenuSubScreen.FLASHCARDS -> FlashcardsScreen(uiState, viewModel, contentPadding)
            MenuSubScreen.MOCK_TESTS -> MockTestsScreen(uiState, viewModel, contentPadding)
            MenuSubScreen.LIBRARY -> LibraryScreen(uiState, viewModel, contentPadding)
            MenuSubScreen.INSIGHTS -> InsightsScreen(uiState, viewModel, contentPadding)
            MenuSubScreen.WEEKLY_REPORT -> WeeklyReportScreen(uiState, viewModel, contentPadding)
            MenuSubScreen.STREAK_HEATMAP -> StreakHeatmapScreen(uiState, viewModel, contentPadding)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NEET Mastery Hub",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "High-yield revision, mistake log, mock analysis & analytics",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }

        val menuItems = listOf(
            Triple(MenuSubScreen.REVISE, "Spaced Revision", "Day 1, 3, 7, 21 retention tracker for completed lectures") to (Icons.Default.Repeat to TealAccent),
            Triple(MenuSubScreen.MISTAKES, "Mistakes Log", "Review silly errors, concept gaps, and formulas to never repeat") to (Icons.Default.ErrorOutline to Color(0xFFF43F5E)),
            Triple(MenuSubScreen.FLASHCARDS, "Flashcards", "High-yield formulas, NCERT biological facts & diagrams") to (Icons.Default.Style to Color(0xFFF59E0B)),
            Triple(MenuSubScreen.MOCK_TESTS, "Mock Tests Tracker", "Log scores out of 720 (Phy / Chem / Bio) & track rank gap") to (Icons.Default.School to IndigoAccent),
            Triple(MenuSubScreen.LIBRARY, "Resource Library", "Organize DPPs, class notes, and question sheets") to (Icons.Default.FolderOpen to Color(0xFF06B6D4)),
            Triple(MenuSubScreen.INSIGHTS, "Study Insights", "Subject velocity, completion percentage & backlog burndown") to (Icons.Default.PieChart to Color(0xFF8B5CF6)),
            Triple(MenuSubScreen.WEEKLY_REPORT, "Weekly Report", "Compare this week vs last week study hours & efficiency") to (Icons.Default.TrendingUp to Color(0xFF10B981)),
            Triple(MenuSubScreen.STREAK_HEATMAP, "Streak Calendar", "12-week consistency heatmap inspired by GitHub") to (Icons.Default.CalendarMonth to Color(0xFFFBBF24))
        )

        items(menuItems) { (info, visual) ->
            val (subScreen, title, desc) = info
            val (icon, color) = visual

            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateToMenuSubScreen(subScreen) }
                    .testTag("menu_item_${subScreen.name.lowercase()}")
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
                            .background(color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = desc, color = Color(0xFF94A3B8), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// 1. REVISE SCREEN
@Composable
fun ReviseScreen(uiState: KaizenUiState, viewModel: KaizenViewModel, contentPadding: PaddingValues) {
    val doneLectures = uiState.lectures.filter { it.status == "done" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateToMenuSubScreen(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Spaced Revision", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Scientific curve of forgetting: Day 1, 3, 7, 21", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }

        if (doneLectures.isEmpty()) {
            item {
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mark lectures as 'Done' in Backlog to track their revision cycles here!",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(doneLectures, key = { it.id }) { lec ->
                val sub = uiState.subjects.find { it.id == lec.subjectId }
                val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#14B8A6")) } catch (_: Exception) { TealAccent }

                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = subColor.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                Text(sub?.name ?: "", color = subColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(lec.chapter, color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(lec.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(10.dp))
                        // Day 1, 3, 7, 21 Checkbox Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RevisionPill("Day 1", lec.revisionDay1) { viewModel.toggleRevision(lec.id, 1) }
                            RevisionPill("Day 3", lec.revisionDay3) { viewModel.toggleRevision(lec.id, 3) }
                            RevisionPill("Day 7", lec.revisionDay7) { viewModel.toggleRevision(lec.id, 7) }
                            RevisionPill("Day 21", lec.revisionDay21) { viewModel.toggleRevision(lec.id, 21) }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun RevisionPill(label: String, isDone: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isDone) TealAccent.copy(alpha = 0.25f) else Color(0xFF1E2846),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDone) TealAccent else DarkCardBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = if (isDone) TealAccent else Color(0xFF64748B), modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = if (isDone) Color.White else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 2. MISTAKES LOG SCREEN
@Composable
fun MistakesScreen(uiState: KaizenUiState, viewModel: KaizenViewModel, contentPadding: PaddingValues) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateToMenuSubScreen(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Mistakes Log", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("${uiState.mistakes.size} logged errors to review", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Mistake", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (uiState.mistakes.isEmpty()) {
            item {
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No mistakes logged! Add problems you missed in DPPs or mock tests.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(uiState.mistakes, key = { it.id }) { mistake ->
                val sub = uiState.subjects.find { it.id == mistake.subjectId }
                val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#14B8A6")) } catch (_: Exception) { TealAccent }

                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = subColor.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text(sub?.name ?: "", color = subColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = Color(0xFFF43F5E).copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                    Text(mistake.mistakeType, color = Color(0xFFF43F5E), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteMistake(mistake.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(mistake.questionTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        if (mistake.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(mistake.notes, color = Color(0xFFCBD5E1), fontSize = 12.sp, lineHeight = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Logged on ${mistake.date}", color = Color(0xFF64748B), fontSize = 10.sp)
                            Button(
                                onClick = { viewModel.incrementMistakeSolved(mistake.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2846)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Re-solved: ${mistake.solvedCount}x ✓", color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showAddDialog) {
        var subId by remember { mutableStateOf(uiState.subjects.firstOrNull()?.id ?: "sub_phy") }
        var title by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("Calculation error") }
        var notes by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = { Text("Log Question Mistake", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Mistake Type:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Calculation error", "Concept gap", "Misread question", "Formula forgotten", "Time pressure").forEach { t ->
                            val sel = type == t
                            Surface(
                                color = if (sel) Color(0xFFF43F5E) else Color(0xFF1E2846),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { type = t }
                            ) {
                                Text(t, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Question / Concept") },
                        placeholder = { Text("e.g. Kinematics projectile peak height") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("What went wrong & correct solution") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addMistake(subId, title, type, notes)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = Color(0xFF94A3B8)) }
            }
        )
    }
}

// 3. FLASHCARDS SCREEN (WITH FLIP CARD ANIMATION)
@Composable
fun FlashcardsScreen(uiState: KaizenUiState, viewModel: KaizenViewModel, contentPadding: PaddingValues) {
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val cards = uiState.flashcards

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateToMenuSubScreen(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("NEET Flashcards", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Tap card to flip between Question and Answer", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }

        if (cards.isEmpty()) {
            item {
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("No flashcards found.", color = Color(0xFF94A3B8), modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
                }
            }
        } else {
            val safeIndex = currentIndex.coerceIn(0, cards.lastIndex)
            val currentCard = cards[safeIndex]
            val sub = uiState.subjects.find { it.id == currentCard.subjectId }
            val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#14B8A6")) } catch (_: Exception) { TealAccent }

            item {
                Text(
                    text = "Card ${safeIndex + 1} of ${cards.size}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Flip Card Container
            item {
                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(400),
                    label = "flip"
                )

                Surface(
                    color = if (isFlipped) DarkSurfaceElevated else DarkCardBg,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isFlipped) TealAccent else DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable { isFlipped = !isFlipped }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotation <= 90f) {
                            // FRONT
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Surface(color = subColor.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text("${sub?.name ?: ""} • ${currentCard.chapter}", color = subColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                                Text(
                                    text = currentCard.front,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text("Tap to see Answer", color = Color(0xFF64748B), fontSize = 11.sp)
                            }
                        } else {
                            // BACK (Mirror rotation so text is readable)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f }
                            ) {
                                Surface(color = TealAccent.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text("SOLUTION / ANSWER", color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                                Text(
                                    text = currentCard.back,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Text("Tap to flip back", color = Color(0xFF64748B), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Controls: Prev / Mastered / Next
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isFlipped = false
                            if (safeIndex > 0) currentIndex = safeIndex - 1
                        },
                        enabled = safeIndex > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("← Prev", color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.toggleFlashcardMastered(currentCard.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentCard.mastered) TealAccent else Color(0xFF1E2846)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (currentCard.mastered) "Mastered ✓" else "Needs Practice", color = if (currentCard.mastered) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isFlipped = false
                            if (safeIndex < cards.lastIndex) currentIndex = safeIndex + 1
                        },
                        enabled = safeIndex < cards.lastIndex,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next →", color = Color.White)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

// 4. MOCK TESTS SCREEN (SCORE OUT OF 720)
@Composable
fun MockTestsScreen(uiState: KaizenUiState, viewModel: KaizenViewModel, contentPadding: PaddingValues) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateToMenuSubScreen(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Mock Tests (720)", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Target: 680+ for Top Govt Medical College", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Score", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(uiState.mockTests, key = { it.id }) { mt ->
            val pct = mt.percentage
            val color = if (mt.totalScore >= 620) TealAccent else if (mt.totalScore >= 550) Color(0xFFFBBF24) else Color(0xFFF43F5E)

            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(mt.testName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(mt.date, color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("${mt.totalScore}/720", color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Text("${String.format("%.1f", pct)}%", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    // Section scores: Physics / 180, Chem / 180, Bio / 360
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SectionScorePill("Physics", "${mt.physicsScore}/180")
                        SectionScorePill("Chemistry", "${mt.chemistryScore}/180")
                        SectionScorePill("Biology", "${mt.biologyScore}/360")
                    }

                    if (mt.analysisNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(mt.analysisNotes, color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("Full Syllabus Mock 02") }
        var phy by remember { mutableStateOf("150") }
        var chem by remember { mutableStateOf("155") }
        var bio by remember { mutableStateOf("330") }
        var notes by remember { mutableStateOf("") }
        val dateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = { Text("Log Mock Test Score", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Test Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = phy,
                            onValueChange = { phy = it },
                            label = { Text("Phy (180)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = chem,
                            onValueChange = { chem = it },
                            label = { Text("Chem (180)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio (360)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Analysis Notes") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = phy.toIntOrNull() ?: 0
                        val c = chem.toIntOrNull() ?: 0
                        val b = bio.toIntOrNull() ?: 0
                        viewModel.addMockTest(name, dateStr, p, c, b, notes)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = Color(0xFF94A3B8)) }
            }
        )
    }
}

@Composable
fun SectionScorePill(subject: String, score: String) {
    Surface(
        color = Color(0xFF1E2846),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(subject, color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text(score, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 5. INSIGHTS SCREEN
@Composable
fun InsightsScreen(uiState: KaizenUiState, viewModel: KaizenViewModel, contentPadding: PaddingValues) {
    val totalLecs = uiState.lectures.size
    val doneLecs = uiState.lectures.count { it.status == "done" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateToMenuSubScreen(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Study Insights & Velocity", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Subject coverage & burndown rate", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }

        uiState.subjects.forEach { s ->
            val subLecs = uiState.lectures.filter { it.subjectId == s.id }
            val done = subLecs.count { it.status == "done" }
            val total = subLecs.size
            val pct = if (total > 0) done.toFloat() / total else 0f
            val subColor = try { Color(android.graphics.Color.parseColor(s.colorHex)) } catch (_: Exception) { TealAccent }

            item(key = s.id) {
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(s.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("$done/$total done (${(pct * 100).toInt()}%)", color = subColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { pct },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = subColor,
                            trackColor = Color(0xFF1E2846)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

// 6. WEEKLY REPORT SCREEN
@Composable
fun WeeklyReportScreen(uiState: KaizenUiState, viewModel: KaizenViewModel, contentPadding: PaddingValues) {
    val thisWeekHours = remember(uiState.studyLogs) {
        uiState.studyLogs.takeLast(7).sumOf { it.totalMinutes } / 60f
    }
    val targetWeekHours = uiState.settings.dailyStudyGoalHours * 7f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateToMenuSubScreen(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Weekly Report", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Past 7 days performance vs target", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }

        item {
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("THIS WEEK STUDY TIME", color = TealAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${String.format("%.1f", thisWeekHours)} Hours", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Target: ${targetWeekHours.toInt()}h (${String.format("%.0f", (thisWeekHours / targetWeekHours.coerceAtLeast(1f)) * 100)}% achieved)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

// 7. 12-WEEK STREAK HEATMAP SCREEN
@Composable
fun StreakHeatmapScreen(uiState: KaizenUiState, viewModel: KaizenViewModel, contentPadding: PaddingValues) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Build 12 weeks of days (84 days)
    val dayGrid = remember(uiState.studyLogs) {
        val list = mutableListOf<Pair<String, Int>>() // Date, minutes
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -83)
        for (i in 0 until 84) {
            val dStr = sdf.format(cal.time)
            val log = uiState.studyLogs.find { it.date == dStr }
            list.add(dStr to (log?.totalMinutes ?: 0))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateToMenuSubScreen(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("12-Week Streak Calendar", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Daily study activity heatmap", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }

        item {
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Heatmap Intensity", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // 12 columns x 7 rows
                    val weeks = dayGrid.chunked(7)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        weeks.forEach { weekDays ->
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                weekDays.forEach { (_, mins) ->
                                    val cellColor = when {
                                        mins >= 360 -> TealAccent
                                        mins >= 240 -> TealAccent.copy(alpha = 0.7f)
                                        mins >= 120 -> TealAccent.copy(alpha = 0.4f)
                                        mins > 0 -> TealAccent.copy(alpha = 0.2f)
                                        else -> Color(0xFF1E2846)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(cellColor)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Less", color = Color(0xFF64748B), fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf(Color(0xFF1E2846), TealAccent.copy(alpha = 0.2f), TealAccent.copy(alpha = 0.4f), TealAccent.copy(alpha = 0.7f), TealAccent).forEach { c ->
                            Box(modifier = Modifier.size(10.dp).background(c, RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text("More", color = Color(0xFF64748B), fontSize = 10.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}
