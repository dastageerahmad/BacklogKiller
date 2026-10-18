package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LibraryItem
import com.example.model.Subject
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceSubtle
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.TealAccent
import com.example.viewmodel.KaizenUiState
import com.example.viewmodel.KaizenViewModel

@Composable
fun LibraryScreen(
    uiState: KaizenUiState,
    viewModel: KaizenViewModel,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var pendingOnly by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showBulkAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<LibraryItem?>(null) }

    // Filter items
    val filteredItems = remember(uiState.libraryItems, selectedSubjectId, selectedType, searchQuery, pendingOnly) {
        uiState.libraryItems.filter { item ->
            val matchSub = selectedSubjectId == null || item.subjectId == selectedSubjectId
            val matchType = selectedType == null || item.type.equals(selectedType, ignoreCase = true)
            val matchPending = !pendingOnly || !item.done
            val matchSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.linkOrNote.contains(searchQuery, ignoreCase = true)
            matchSub && matchType && matchPending && matchSearch
        }.sortedWith(
            compareBy<LibraryItem> { it.subjectId }
                .thenBy { it.type }
                .thenBy { it.lectureNumber ?: 999 }
                .thenBy { it.title }
        )
    }

    val totalCount = uiState.libraryItems.size
    val doneCount = uiState.libraryItems.count { it.done }
    val progressPct = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f

    val nextUpItem = remember(uiState.libraryItems) {
        uiState.libraryItems.firstOrNull { !it.done }
    }

    val grouped = remember(filteredItems) {
        filteredItems.groupBy { it.subjectId }
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
            // Header & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resource Library",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$doneCount of $totalCount items completed",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showBulkAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bulk Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_library_single_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Overall Library Progress Bar
        item {
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Study Materials Solved", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("$doneCount/$totalCount done (${(progressPct * 100).toInt()}%)", color = TealAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progressPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TealAccent,
                        trackColor = Color(0xFF1E2846)
                    )
                }
            }
        }

        // "NEXT UP" CARD WITH OPEN BUTTON
        nextUpItem?.let { nextItem ->
            val nextSub = uiState.subjects.find { it.id == nextItem.subjectId }
            val nextSubColor = try { Color(android.graphics.Color.parseColor(nextSub?.colorHex ?: "#14B8A6")) } catch (_: Exception) { TealAccent }

            item {
                Surface(
                    color = DarkSurfaceSubtle,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = IndigoAccent.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "NEXT UP",
                                        color = Color(0xFFC7D2FE),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${nextSub?.name ?: ""} • ${nextItem.type}",
                                    color = nextSubColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextItem.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (nextItem.linkOrNote.startsWith("http")) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(nextItem.linkOrNote))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Search bar & Pending only switch
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search resources...", color = Color(0xFF64748B), fontSize = 13.sp) },
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pending", color = if (pendingOnly) TealAccent else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = pendingOnly,
                            onCheckedChange = { pendingOnly = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TealAccent,
                                checkedTrackColor = TealAccent.copy(alpha = 0.3f),
                                uncheckedTrackColor = Color(0xFF1E2846)
                            )
                        )
                    }
                }
            }
        }

        // Filter chips: Subjects & Types
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Subject chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChipPill("All Subjects", selectedSubjectId == null, TealAccent) { selectedSubjectId = null }
                    uiState.subjects.forEach { s ->
                        val subColor = try { Color(android.graphics.Color.parseColor(s.colorHex)) } catch (_: Exception) { TealAccent }
                        FilterChipPill(s.name, selectedSubjectId == s.id, subColor) { selectedSubjectId = s.id }
                    }
                }

                // Type chips (DPP, Class notes, HW, Extra Qs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChipPill("All Types", selectedType == null, IndigoAccent) { selectedType = null }
                    listOf("DPP", "Class notes", "HW", "Extra Qs").forEach { t ->
                        FilterChipPill(t, selectedType.equals(t, ignoreCase = true), IndigoAccent) { selectedType = t }
                    }
                }
            }
        }

        // Render sorted items grouped by Subject headings
        if (grouped.isEmpty()) {
            item {
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No resources found", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Use '+ Add' or 'Bulk Add' to add DPPs, Notes, and sheets.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
        } else {
            grouped.forEach { (subId, itemsInSub) ->
                val sub = uiState.subjects.find { it.id == subId }
                val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#14B8A6")) } catch (_: Exception) { TealAccent }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(subColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sub?.name ?: "Subject",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${itemsInSub.count { it.done }}/${itemsInSub.size})",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                items(itemsInSub, key = { it.id }) { item ->
                    Surface(
                        color = DarkCardBg,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (item.done) TealAccent.copy(alpha = 0.3f) else DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.done,
                                    onCheckedChange = { viewModel.toggleLibraryDone(item.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = TealAccent),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = IndigoAccent.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.type,
                                                color = Color(0xFFC7D2FE),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        item.chapter?.let { ch ->
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = ch,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.title,
                                        color = if (item.done) Color(0xFF94A3B8) else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.linkOrNote.isNotBlank()) {
                                        Text(
                                            text = item.linkOrNote,
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.linkOrNote.startsWith("http")) {
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.linkOrNote))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = TealAccent, modifier = Modifier.size(16.dp))
                                    }
                                }

                                IconButton(
                                    onClick = { itemToDelete = item },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
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

    // Delete Confirmation
    itemToDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Delete Library Item?",
            message = "Remove '${item.title}' from your library?",
            onConfirm = { viewModel.deleteLibraryItem(item.id) },
            onDismiss = { itemToDelete = null }
        )
    }

    // Single Add Dialog
    if (showAddDialog) {
        AddLibraryItemDialog(
            subjects = uiState.subjects,
            onConfirm = { title, link, subId, type, ch ->
                viewModel.addLibraryItem(title, link, subId, type, ch)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Bulk Add Dialog
    if (showBulkAddDialog) {
        BulkAddLibraryDialog(
            subjects = uiState.subjects,
            onConfirm = { subId, type, ch, raw ->
                viewModel.bulkAddLibrary(subId, type, ch, raw)
                showBulkAddDialog = false
            },
            onDismiss = { showBulkAddDialog = false }
        )
    }
}

@Composable
fun FilterChipPill(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) color.copy(alpha = 0.25f) else DarkCardBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else DarkCardBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun AddLibraryItemDialog(
    subjects: List<Subject>,
    onConfirm: (title: String, link: String, subId: String, type: String, ch: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var subId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "sub_phy") }
    var type by remember { mutableStateOf("DPP") }
    var chapter by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text("Add Study Resource", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type selector
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DPP", "Class notes", "HW", "Extra Qs").forEach { t ->
                        val sel = type == t
                        Surface(
                            color = if (sel) IndigoAccent else DarkCardBg,
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
                    label = { Text("Title") },
                    placeholder = { Text("e.g. DPP 04 - Friction") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Link / Note URL") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter (optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title, link, subId, type, chapter.ifBlank { null })
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
fun BulkAddLibraryDialog(
    subjects: List<Subject>,
    onConfirm: (subId: String, type: String, chapter: String?, rawLines: String) -> Unit,
    onDismiss: () -> Unit
) {
    var subId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "sub_phy") }
    var type by remember { mutableStateOf("DPP") }
    var chapter by remember { mutableStateOf("") }
    var rawText by remember { mutableStateOf("1 https://neetprep.com/dpp1\n2 https://neetprep.com/dpp2\n3 https://neetprep.com/dpp3") }

    // Live preview
    val previewLines = remember(rawText, type) {
        rawText.split("\n").filter { it.isNotBlank() }.take(4).map { line ->
            val parts = line.trim().split("\\s+".toRegex(), limit = 2)
            if (parts.size == 2 && parts[0].toIntOrNull() != null) {
                val num = String.format("%02d", parts[0].toInt())
                val domain = try { Uri.parse(parts[1]).host ?: "site.com" } catch (_: Exception) { "site.com" }
                "Lec $num <- $domain"
            } else {
                val domain = try { Uri.parse(line.trim()).host ?: "site.com" } catch (_: Exception) { "site.com" }
                "$type <- $domain"
            }
        }
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text("Bulk Add Links", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Type:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DPP", "Class notes", "HW", "Extra Qs").forEach { t ->
                        val sel = type == t
                        Surface(
                            color = if (sel) IndigoAccent else DarkCardBg,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { type = t }
                        ) {
                            Text(t, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter (optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Paste links (e.g. '1 https://...' or plain links):",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )

                // Live preview section
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Live Preview:", color = TealAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        previewLines.forEach { line ->
                            Text(line, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(subId, type, chapter.ifBlank { null }, rawText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Import All", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
