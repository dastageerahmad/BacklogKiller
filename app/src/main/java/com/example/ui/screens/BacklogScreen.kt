package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lecture
import com.example.model.LectureFlags
import com.example.model.LectureLinks
import com.example.model.Subject
import com.example.ui.components.ChipBadge
import com.example.ui.components.ChipLinkActionDialog
import com.example.ui.components.ConfirmDeleteDialog
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
fun BacklogScreen(
    uiState: KaizenUiState,
    viewModel: KaizenViewModel,
    contentPadding: PaddingValues
) {
    var selectedChipLecture by remember { mutableStateOf<Pair<Lecture, String>?>(null) }
    var lectureToDelete by remember { mutableStateOf<Lecture?>(null) }
    var chapterToDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // subjectId, chapterName
    var lectureToEdit by remember { mutableStateOf<Lecture?>(null) }
    var showAddLectureDialog by remember { mutableStateOf<String?>(null) } // default chapter name or empty

    // Collapsed chapters map (default all expanded)
    val collapsedChapters = remember { mutableStateMapOf<String, Boolean>() }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Filter lectures by subject, status, search query
    val filteredLectures = remember(
        uiState.lectures,
        uiState.backlogSubjectFilter,
        uiState.backlogStatusFilter,
        uiState.backlogSearchQuery
    ) {
        uiState.lectures.filter { lec ->
            val matchSubject = uiState.backlogSubjectFilter == null || lec.subjectId == uiState.backlogSubjectFilter
            val matchStatus = when (uiState.backlogStatusFilter) {
                "PENDING" -> lec.status == "pending"
                "IN_PROGRESS" -> lec.status == "in_progress"
                "DONE" -> lec.status == "done"
                else -> true
            }
            val matchSearch = uiState.backlogSearchQuery.isBlank() ||
                    lec.title.contains(uiState.backlogSearchQuery, ignoreCase = true) ||
                    lec.chapter.contains(uiState.backlogSearchQuery, ignoreCase = true)
            matchSubject && matchStatus && matchSearch
        }
    }

    // Group filtered lectures by Subject and Chapter
    val groupedChapters = remember(filteredLectures) {
        // Map<SubjectId, Map<ChapterName, List<Lecture>>>
        filteredLectures.groupBy { it.subjectId }.mapValues { (_, lecs) ->
            lecs.groupBy { it.chapter }
        }
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
            // Screen Title & Add Lecture button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Backlog Tracker",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredLectures.count { it.status != "done" }} pending lectures remaining",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { showAddLectureDialog = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_lecture_main_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Lecture", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.backlogSearchQuery,
                onValueChange = { viewModel.setBacklogSearchQuery(it) },
                placeholder = { Text("Search by chapter, lecture title...", color = Color(0xFF64748B), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg,
                    focusedBorderColor = TealAccent,
                    unfocusedBorderColor = DarkCardBorder
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backlog_search_input")
            )
        }

        // Subject Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val allCount = uiState.lectures.size
                SubjectFilterChip(
                    label = "All ($allCount)",
                    isSelected = uiState.backlogSubjectFilter == null,
                    color = TealAccent,
                    onClick = { viewModel.setBacklogSubjectFilter(null) }
                )

                uiState.subjects.forEach { sub ->
                    val count = uiState.lectures.count { it.subjectId == sub.id }
                    val subColor = try { Color(android.graphics.Color.parseColor(sub.colorHex)) } catch (_: Exception) { TealAccent }
                    SubjectFilterChip(
                        label = "${sub.name} ($count)",
                        isSelected = uiState.backlogSubjectFilter == sub.id,
                        color = subColor,
                        onClick = { viewModel.setBacklogSubjectFilter(sub.id) }
                    )
                }
            }
        }

        // Status Tabs (All, Pending, In Progress, Done)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardBg, RoundedCornerShape(12.dp))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val statuses = listOf("ALL" to "All", "PENDING" to "Pending", "IN_PROGRESS" to "In Progress", "DONE" to "Done")
                statuses.forEach { (key, label) ->
                    val isSelected = uiState.backlogStatusFilter == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TealAccent.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewModel.setBacklogStatusFilter(key) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TealAccent else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        if (groupedChapters.isEmpty()) {
            item {
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No matching lectures found", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Try clearing your search or filters to see all subjects.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
        } else {
            // Render Chapters grouped by Subject
            groupedChapters.forEach { (subId, chaptersMap) ->
                val subject = uiState.subjects.find { it.id == subId }
                val subColor = try { Color(android.graphics.Color.parseColor(subject?.colorHex ?: "#14B8A6")) } catch (_: Exception) { TealAccent }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(subColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = subject?.name ?: "Subject",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                chaptersMap.forEach { (chapterName, lecturesInChapter) ->
                    val chapterKey = "$subId:$chapterName"
                    val isCollapsed = collapsedChapters[chapterKey] == true
                    val doneCount = lecturesInChapter.count { it.status == "done" }
                    val totalInChapter = lecturesInChapter.size

                    item(key = chapterKey) {
                        Surface(
                            color = DarkCardBg,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Chapter Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { collapsedChapters[chapterKey] = !isCollapsed }
                                    ) {
                                        // Highlighted pill in subject colour
                                        Surface(
                                            color = subColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, subColor.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = chapterName,
                                                color = subColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$doneCount/$totalInChapter done",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { showAddLectureDialog = chapterName },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add lecture to chapter", tint = TealAccent, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(
                                            onClick = { chapterToDelete = subId to chapterName },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete chapter", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(
                                            onClick = { collapsedChapters[chapterKey] = !isCollapsed },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                                contentDescription = if (isCollapsed) "Expand" else "Collapse",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                // Collapsible Lecture list
                                AnimatedVisibility(
                                    visible = !isCollapsed,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 10.dp)
                                    ) {
                                        lecturesInChapter.forEach { lecture ->
                                            BacklogLectureCard(
                                                lecture = lecture,
                                                isToday = lecture.todayDate == todayStr,
                                                onToggleStatus = { viewModel.toggleLectureStatus(lecture.id) },
                                                onToggleToday = { viewModel.toggleTodayPick(lecture.id) },
                                                onChipClick = { chip -> selectedChipLecture = lecture to chip },
                                                onEditClick = { lectureToEdit = lecture },
                                                onDeleteClick = { lectureToDelete = lecture }
                                            )
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

    // Delete Lecture Confirmation
    lectureToDelete?.let { lec ->
        ConfirmDeleteDialog(
            title = "Delete Lecture?",
            message = "Are you sure you want to delete '${lec.title}'? You can undo this action afterwards.",
            onConfirm = { viewModel.deleteLecture(lec.id) },
            onDismiss = { lectureToDelete = null }
        )
    }

    // Delete Chapter Confirmation
    chapterToDelete?.let { (subId, chName) ->
        ConfirmDeleteDialog(
            title = "Delete Chapter?",
            message = "This will delete chapter '$chName' and all its lectures. Are you sure?",
            onConfirm = { viewModel.deleteChapter(subId, chName) },
            onDismiss = { chapterToDelete = null }
        )
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

    // Add Lecture Dialog
    showAddLectureDialog?.let { initialChapter ->
        AddLectureDialog(
            subjects = uiState.subjects,
            selectedSubjectId = uiState.backlogSubjectFilter ?: uiState.subjects.firstOrNull()?.id ?: "sub_phy",
            initialChapter = initialChapter,
            onConfirm = { subId, ch, title, dur, spd, pri ->
                viewModel.addLecture(subId, ch, title, dur, spd, pri)
                showAddLectureDialog = null
            },
            onDismiss = { showAddLectureDialog = null }
        )
    }

    // Edit Lecture Dialog
    lectureToEdit?.let { lecture ->
        EditLectureDialog(
            lecture = lecture,
            subjects = uiState.subjects,
            onConfirm = { updated ->
                viewModel.updateLecture(updated)
                lectureToEdit = null
            },
            onDismiss = { lectureToEdit = null }
        )
    }
}

@Composable
fun BacklogLectureCard(
    lecture: Lecture,
    isToday: Boolean,
    onToggleStatus: () -> Unit,
    onToggleToday: () -> Unit,
    onChipClick: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDone = lecture.status == "done"
    val priorityColor = when (lecture.priority.lowercase()) {
        "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Surface(
        color = DarkSurfaceSubtle,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDone) TealAccent.copy(alpha = 0.3f) else DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top row: Status Badge, Title (wraps to 2 lines, no text overlap), Priority Dot, Delete Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge (PENDING / DONE)
                    Surface(
                        color = if (isDone) TealAccent.copy(alpha = 0.2f) else Color(0xFF334155),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onToggleStatus() }
                    ) {
                        Text(
                            text = if (isDone) "DONE" else "PENDING",
                            color = if (isDone) TealAccent else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Priority Dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(priorityColor, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title
                    Text(
                        text = lecture.title,
                        color = if (isDone) Color(0xFF94A3B8) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Duration @Speed, Chips, "+ Today" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${lecture.durationMinutes}m @${lecture.speed}x = ${lecture.effectiveDurationMinutes}m",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ChipBadge("DPP", !lecture.links.dpp.isNullOrBlank(), lecture.flags.dpp) { onChipClick("DPP") }
                    ChipBadge("Notes", !lecture.links.notes.isNullOrBlank(), lecture.flags.notes) { onChipClick("Notes") }
                    ChipBadge("HW", !lecture.links.hw.isNullOrBlank(), lecture.flags.hw) { onChipClick("HW") }
                    ChipBadge("Extra", !lecture.links.extraQs.isNullOrBlank(), lecture.flags.extraQs) { onChipClick("ExtraQs") }

                    Spacer(modifier = Modifier.width(4.dp))

                    // "+ Today" / "In Today" button
                    Surface(
                        color = if (isToday) TealAccent.copy(alpha = 0.2f) else Color(0xFF1E2846),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isToday) TealAccent else Color(0xFF334155)),
                        modifier = Modifier.clickable { onToggleToday() }
                    ) {
                        Text(
                            text = if (isToday) "In Today ✓" else "+ Today",
                            color = if (isToday) TealAccent else Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) color.copy(alpha = 0.25f) else DarkCardBg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else DarkCardBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun AddLectureDialog(
    subjects: List<Subject>,
    selectedSubjectId: String,
    initialChapter: String,
    onConfirm: (subId: String, chapter: String, title: String, dur: Int, speed: Float, priority: String) -> Unit,
    onDismiss: () -> Unit
) {
    var subjectId by remember { mutableStateOf(selectedSubjectId) }
    var chapter by remember { mutableStateOf(initialChapter) }
    var title by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("90") }
    var speed by remember { mutableStateOf(1.25f) }
    var priority by remember { mutableStateOf("high") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text("Add Lecture to Backlog", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subject selection
                Text("Subject:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { s ->
                        val isSel = s.id == subjectId
                        val subColor = try { Color(android.graphics.Color.parseColor(s.colorHex)) } catch (_: Exception) { TealAccent }
                        Surface(
                            color = if (isSel) subColor.copy(alpha = 0.25f) else Color(0xFF1E2846),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) subColor else Color.Transparent),
                            modifier = Modifier.clickable { subjectId = s.id }
                        ) {
                            Text(
                                text = s.name,
                                color = if (isSel) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter Name") },
                    placeholder = { Text("e.g. Thermodynamics") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Lecture Title") },
                    placeholder = { Text("e.g. Lec 04 - Carnot Cycle") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Duration (min)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TealAccent,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Priority
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Priority", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("high" to "High", "medium" to "Med", "low" to "Low").forEach { (k, lbl) ->
                                val sel = priority == k
                                Surface(
                                    color = if (sel) TealAccent.copy(alpha = 0.2f) else Color(0xFF1E2846),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable { priority = k }
                                ) {
                                    Text(
                                        text = lbl,
                                        color = if (sel) TealAccent else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Speed buttons
                Text("Playback Speed:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { spd ->
                        val sel = speed == spd
                        Surface(
                            color = if (sel) IndigoAccent else Color(0xFF1E2846),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { speed = spd }
                        ) {
                            Text(
                                text = "${spd}x",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val dur = durationText.toIntOrNull() ?: 60
                        onConfirm(subjectId, chapter.ifBlank { "General" }, title, dur, speed, priority)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
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
fun EditLectureDialog(
    lecture: Lecture,
    subjects: List<Subject>,
    onConfirm: (Lecture) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(lecture.title) }
    var chapter by remember { mutableStateOf(lecture.chapter) }
    var durationText by remember { mutableStateOf(lecture.durationMinutes.toString()) }
    var speed by remember { mutableStateOf(lecture.speed) }
    var priority by remember { mutableStateOf(lecture.priority) }

    var dppLink by remember { mutableStateOf(lecture.links.dpp ?: "") }
    var notesLink by remember { mutableStateOf(lecture.links.notes ?: "") }
    var hwLink by remember { mutableStateOf(lecture.links.hw ?: "") }
    var extraQsLink by remember { mutableStateOf(lecture.links.extraQs ?: "") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text("Edit Lecture & Links", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Duration (m)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TealAccent,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = speed.toString(),
                        onValueChange = { speed = it.toFloatOrNull() ?: 1.0f },
                        label = { Text("Speed (x)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TealAccent,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Resource Links:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = dppLink,
                    onValueChange = { dppLink = it },
                    label = { Text("DPP URL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesLink,
                    onValueChange = { notesLink = it },
                    label = { Text("Notes URL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = durationText.toIntOrNull() ?: lecture.durationMinutes
                    val updated = lecture.copy(
                        title = title.trim(),
                        chapter = chapter.trim(),
                        durationMinutes = dur,
                        speed = speed,
                        priority = priority,
                        links = lecture.links.copy(
                            dpp = dppLink.ifBlank { null },
                            notes = notesLink.ifBlank { null },
                            hw = hwLink.ifBlank { null },
                            extraQs = extraQsLink.ifBlank { null }
                        )
                    )
                    onConfirm(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
