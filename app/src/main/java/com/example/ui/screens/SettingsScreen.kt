package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffects
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.TealAccent
import com.example.viewmodel.KaizenUiState
import com.example.viewmodel.KaizenViewModel

@Composable
fun SettingsScreen(
    uiState: KaizenUiState,
    viewModel: KaizenViewModel,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val settings = uiState.settings

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Settings & Customization", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Tailor appearance, sounds & manage study data", color = Color(0xFF94A3B8), fontSize = 12.sp)
        }

        // 1. APPEARANCE & THEME
        item {
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Theme & Texture", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    // Mode chips: Dark Navy / Matte Dark / Matte Pastel / Light
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "dark_navy" to "Dark Navy",
                            "matte_dark" to "Matte Dark",
                            "matte_pastel" to "Matte Pastel",
                            "light" to "Light"
                        ).forEach { (mode, lbl) ->
                            val isSel = settings.themeMode == mode
                            Surface(
                                color = if (isSel) TealAccent.copy(alpha = 0.25f) else Color(0xFF1E2846),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) TealAccent else Color.Transparent),
                                modifier = Modifier.clickable { viewModel.updateSettings(settings.copy(themeMode = mode)) }
                            ) {
                                Text(lbl, color = if (isSel) TealAccent else Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                    }

                    // Simple Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Simple Mode (Flat)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Removes glows and gradients for zero distraction", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Switch(
                            checked = settings.simpleMode,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(simpleMode = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = TealAccent, checkedTrackColor = TealAccent.copy(alpha = 0.3f))
                        )
                    }

                    // Accent Colors
                    Text("Accent Color:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val accents = listOf(
                            "#14B8A6" to "Teal",
                            "#6366F1" to "Indigo",
                            "#F43F5E" to "Rose",
                            "#F59E0B" to "Amber",
                            "#10B981" to "Emerald",
                            "#06B6D4" to "Cyan",
                            "#8B5CF6" to "Violet"
                        )
                        accents.forEach { (hex, _) ->
                            val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { TealAccent }
                            val isSel = settings.accentColor.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(if (isSel) 2.5.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { viewModel.updateSettings(settings.copy(accentColor = hex)) }
                            )
                        }
                    }
                }
            }
        }

        // 2. SOUNDS & AUDIO FEEDBACK
        item {
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Completion Sound Effects", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Switch(
                            checked = settings.soundEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(soundEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = IndigoAccent, checkedTrackColor = IndigoAccent.copy(alpha = 0.3f))
                        )
                    }

                    if (settings.soundEnabled) {
                        Text("Style:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Classic", "Soft", "Retro", "Bell").forEach { style ->
                                val isSel = settings.soundStyle == style
                                Surface(
                                    color = if (isSel) IndigoAccent else Color(0xFF1E2846),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        viewModel.updateSettings(settings.copy(soundStyle = style))
                                        SoundEffects.playCompletionPop(style, settings.soundPitch, settings.soundVolume)
                                    }
                                ) {
                                    Text(style, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            }
                        }

                        // Pitch Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sound Pitch (${String.format("%.1f", settings.soundPitch)}x)", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Button(
                                onClick = { SoundEffects.playCompletionPop(settings.soundStyle, settings.soundPitch, settings.soundVolume) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2846)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TealAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Test", color = TealAccent, fontSize = 10.sp)
                            }
                        }

                        Slider(
                            value = settings.soundPitch,
                            onValueChange = { viewModel.updateSettings(settings.copy(soundPitch = it)) },
                            valueRange = 0.6f..1.8f,
                            colors = SliderDefaults.colors(thumbColor = TealAccent, activeTrackColor = TealAccent)
                        )
                    }
                }
            }
        }

        // 3. BACKUP & RESTORE & RESET
        item {
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Data Management", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val json = viewModel.exportBackup()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Kaizen_Backup", json)
                                clipboard.setPrimaryClip(clip)
                                viewModel.showMessage("Copied backup JSON to clipboard!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2846)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Backup", color = Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showImportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2846)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import JSON", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_all_data_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset All Data to Default", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        ConfirmDeleteDialog(
            title = "Reset All Data?",
            message = "This will restore initial sample subjects and lectures. Any newly authored content will be replaced. Continue?",
            onConfirm = { viewModel.resetAllData() },
            onDismiss = { showResetDialog = false }
        )
    }

    // Import Backup Dialog
    if (showImportDialog) {
        var jsonText by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = { Text("Import Backup JSON", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    placeholder = { Text("Paste exported JSON here...") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = viewModel.importBackup(jsonText.trim())
                        if (ok) showImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Restore", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel", color = Color(0xFF94A3B8)) }
            }
        )
    }
}
