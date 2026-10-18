package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CelebrationOverlay
import com.example.ui.components.KaizenFloatingNavBar
import com.example.ui.screens.BacklogScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MenuHubScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.ProfileDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.KaizenTheme
import com.example.ui.theme.TealAccent
import com.example.viewmodel.KaizenTab
import com.example.viewmodel.KaizenViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: KaizenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            // Snackbar listener
            LaunchedEffect(uiState.snackbarMessage) {
                uiState.snackbarMessage?.let { msg ->
                    val actionLabel = if (uiState.canUndo) "Undo" else null
                    val result = snackbarHostState.showSnackbar(
                        message = msg,
                        actionLabel = actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoLastDeletion()
                    }
                    viewModel.clearSnackbar()
                }
            }

            // Back button handling:
            // From other screens go Home; from Home show "Exit app?" with Stay / Exit
            BackHandler {
                if (uiState.menuSubScreen != null) {
                    viewModel.navigateToMenuSubScreen(null)
                } else if (uiState.currentTab != KaizenTab.HOME) {
                    viewModel.navigateToTab(KaizenTab.HOME)
                } else {
                    viewModel.setExitDialog(true)
                }
            }

            KaizenTheme(
                themeMode = uiState.settings.themeMode,
                accentHex = uiState.settings.accentColor
            ) {
                val accentColor = try {
                    Color(android.graphics.Color.parseColor(uiState.settings.accentColor))
                } catch (_: Exception) {
                    TealAccent
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        snackbarHost = {
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.padding(bottom = 70.dp)
                            )
                        },
                        bottomBar = {
                            KaizenFloatingNavBar(
                                currentTab = uiState.currentTab,
                                onTabSelected = { viewModel.navigateToTab(it) },
                                accentColor = accentColor
                            )
                        }
                    ) { innerPadding ->
                        when (uiState.currentTab) {
                            KaizenTab.HOME -> HomeScreen(uiState, viewModel, innerPadding)
                            KaizenTab.BACKLOG -> BacklogScreen(uiState, viewModel, innerPadding)
                            KaizenTab.PLANNER -> PlannerScreen(uiState, viewModel, innerPadding)
                            KaizenTab.TIMER -> TimerScreen(uiState, viewModel, innerPadding)
                            KaizenTab.MENU -> MenuHubScreen(uiState, viewModel, innerPadding)
                            KaizenTab.SETTINGS -> SettingsScreen(uiState, viewModel, innerPadding)
                        }
                    }

                    // Celebration Pop Confetti Overlay
                    CelebrationOverlay(triggerTimestamp = uiState.celebrationTimestamp)

                    // Profile Dialog
                    if (uiState.showProfileDialog) {
                        ProfileDialog(
                            uiState = uiState,
                            viewModel = viewModel,
                            onDismiss = { viewModel.setProfileDialog(false) }
                        )
                    }

                    // Exit App Dialog
                    if (uiState.showExitDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.setExitDialog(false) },
                            containerColor = DarkSurfaceElevated,
                            title = {
                                Text("Exit Kaizen?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            },
                            text = {
                                Text("Keep your study momentum going! Are you sure you want to exit?", color = Color(0xFFCBD5E1), fontSize = 14.sp)
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.setExitDialog(false)
                                        finish()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                ) {
                                    Text("Exit", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.setExitDialog(false) }) {
                                    Text("Stay", color = TealAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
