package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.DiceSoundManager
import com.example.model.DieType
import com.example.sensor.ShakeDetector
import com.example.ui.theme.CleanAccentPurple
import com.example.ui.theme.CleanBg
import com.example.ui.theme.CleanCardBorder
import com.example.ui.theme.CleanDockBg
import com.example.ui.theme.CleanPillBg
import com.example.ui.theme.CleanPillText
import com.example.ui.theme.CleanTextPrimary
import com.example.ui.theme.CleanTextSecondary
import com.example.ui.theme.CleanWhite
import com.example.viewmodel.DiceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceScreen(
    viewModel: DiceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val currentView = LocalView.current
    val soundManager = remember(context) { DiceSoundManager(context) }

    // Dialog & Bottom Sheet States
    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDiceCountDialog by remember { mutableStateOf(false) }
    var showDieTypeDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Haptic pulse trigger
    val performHapticPulse = remember(context, uiState.hapticEnabled) {
        {
            if (uiState.hapticEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        vibratorManager?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    }
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(30)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // Landed sound effect trigger respecting system volume
    val performLandingClatter = remember(soundManager, currentView, uiState.soundEnabled) {
        {
            if (uiState.soundEnabled) {
                soundManager.playClatterSound(currentView)
            }
        }
    }

    val triggerRoll = remember(viewModel, performHapticPulse, performLandingClatter) {
        {
            viewModel.rollDice(
                onHapticPulse = performHapticPulse,
                onLanded = performLandingClatter
            )
        }
    }

    // Shake Detector setup with lifecycle
    DisposableEffect(uiState.shakeSensitivity, triggerRoll) {
        val detector = ShakeDetector(context) {
            triggerRoll()
        }
        detector.shakeThreshold = uiState.shakeSensitivity
        detector.start()

        onDispose {
            detector.stop()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CleanBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header matching the Clean Minimalism HTML
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .testTag("menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu & Info",
                        tint = CleanTextPrimary
                    )
                }

                Text(
                    text = "Dice Roller",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp,
                    color = CleanTextPrimary,
                    modifier = Modifier.testTag("header_title")
                )

                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options & Settings",
                        tint = CleanTextPrimary
                    )
                }
            }

            // Main Display Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Last Result Label & Number
                Column(
                    modifier = Modifier.padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LAST RESULT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CleanAccentPurple,
                        letterSpacing = 2.sp,
                        modifier = Modifier.testTag("last_result_label")
                    )

                    Text(
                        text = if (uiState.isRolling) "..." else uiState.lastResult.toString(),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleanTextPrimary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .testTag("last_result_value")
                    )

                    if (uiState.dice.size > 1 && !uiState.isRolling) {
                        Text(
                            text = "(" + uiState.dice.joinToString(" + ") {
                                if (it.isHeld) "[${it.value}]" else it.value.toString()
                            } + ")",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = CleanTextSecondary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Dice Cards Arena (3x3 pips or polyhedral number)
                DiceArena(
                    dice = uiState.dice,
                    dieType = uiState.dieType,
                    isRolling = uiState.isRolling,
                    onDieClick = { dieId ->
                        // Holding a die
                        viewModel.toggleHold(dieId)
                    },
                    modifier = Modifier
                        .padding(bottom = 36.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            triggerRoll()
                        }
                )

                // Shake Indicator / Tap Action Pill
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CleanPillBg)
                            .clickable {
                                triggerRoll()
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                            .testTag("shake_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Motion detection",
                            tint = CleanPillText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (uiState.isRolling) "Rolling..." else "Shake to Roll",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = CleanPillText
                        )
                    }

                    Text(
                        text = "Roll detection enabled • or tap to roll",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = CleanTextSecondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Navigation Dock (matches Clean Minimalism HTML design)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color.Black.copy(alpha = 0.04f),
                            spotColor = Color.Black.copy(alpha = 0.06f)
                        ),
                    shape = RoundedCornerShape(26.dp),
                    color = CleanDockBg,
                    border = BorderStroke(1.dp, CleanCardBorder.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Dice Count Button
                        DockItem(
                            icon = Icons.Default.Casino,
                            label = "${uiState.diceCount} ${if (uiState.diceCount == 1) "Die" else "Dice"}",
                            isActive = true,
                            onClick = { showDiceCountDialog = true },
                            tag = "dock_dice_count"
                        )

                        // 2. Die Type Button
                        DockItem(
                            icon = Icons.Default.Refresh,
                            label = uiState.dieType.label,
                            isActive = false,
                            onClick = { showDieTypeDialog = true },
                            tag = "dock_die_type"
                        )

                        // 3. History Button
                        DockItem(
                            icon = Icons.Default.History,
                            label = "History",
                            isActive = false,
                            onClick = { showHistorySheet = true },
                            tag = "dock_history"
                        )

                        // 4. Settings Button
                        DockItem(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            isActive = false,
                            onClick = { showSettingsDialog = true },
                            tag = "dock_settings"
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Roll History
    if (showHistorySheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = sheetState,
            containerColor = CleanWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Roll History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleanTextPrimary
                    )
                    if (uiState.history.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearHistory() },
                            colors = ButtonDefaults.textButtonColors(contentColor = CleanAccentPurple)
                        ) {
                            Text("Clear")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No rolls recorded yet.\nShake phone or tap to roll!",
                            fontSize = 14.sp,
                            color = CleanTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.history, key = { it.id }) { item ->
                            HistoryRow(item = item)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Dice Count Selector Dialog
    if (showDiceCountDialog) {
        AlertDialog(
            onDismissRequest = { showDiceCountDialog = false },
            title = {
                Text(
                    text = "Select Number of Dice",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CleanTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Choose how many dice to roll at once (1 to 6):",
                        fontSize = 14.sp,
                        color = CleanTextSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..6).forEach { count ->
                            val selected = uiState.diceCount == count
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) CleanAccentPurple else CleanPillBg)
                                    .clickable {
                                        viewModel.setDiceCount(count)
                                        showDiceCountDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) CleanWhite else CleanPillText
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDiceCountDialog = false }) {
                    Text("Done", color = CleanAccentPurple)
                }
            },
            containerColor = CleanWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Die Type Selector Dialog
    if (showDieTypeDialog) {
        AlertDialog(
            onDismissRequest = { showDieTypeDialog = false },
            title = {
                Text(
                    text = "Choose Die Type",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CleanTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DieType.entries.forEach { type ->
                        val isSelected = uiState.dieType == type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CleanPillBg else Color.Transparent)
                                .clickable {
                                    viewModel.setDieType(type)
                                    showDieTypeDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (type.isCoin) "Coin (Heads/Tails)" else "${type.label} (${type.sides} Sides)",
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = CleanTextPrimary
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CleanAccentPurple)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDieTypeDialog = false }) {
                    Text("Close", color = CleanAccentPurple)
                }
            },
            containerColor = CleanWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CleanTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Shake Sensitivity
                    Text(
                        text = "Shake Sensitivity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CleanTextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low", "Normal", "High").forEach { level ->
                            val isSelected = uiState.sensitivityLabel == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CleanAccentPurple else CleanPillBg)
                                    .clickable { viewModel.setSensitivity(level) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) CleanWhite else CleanPillText
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = CleanCardBorder.copy(alpha = 0.6f))

                    // Haptic Feedback Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Haptic Vibration",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = CleanTextPrimary
                            )
                            Text(
                                text = "Tactile roll pulses",
                                fontSize = 12.sp,
                                color = CleanTextSecondary
                            )
                        }
                        Switch(
                            checked = uiState.hapticEnabled,
                            onCheckedChange = { viewModel.toggleHaptic() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CleanWhite,
                                checkedTrackColor = CleanAccentPurple
                            ),
                            modifier = Modifier.testTag("haptic_toggle")
                        )
                    }

                    HorizontalDivider(color = CleanCardBorder.copy(alpha = 0.6f))

                    // Dice Landing Clatter Sound Effect Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Landing Sound Effect",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = CleanTextPrimary
                            )
                            Text(
                                text = "Subtle clatter, respects system volume",
                                fontSize = 12.sp,
                                color = CleanTextSecondary
                            )
                        }
                        Switch(
                            checked = uiState.soundEnabled,
                            onCheckedChange = { viewModel.toggleSound() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CleanWhite,
                                checkedTrackColor = CleanAccentPurple
                            ),
                            modifier = Modifier.testTag("sound_effect_toggle")
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Done", color = CleanAccentPurple)
                }
            },
            containerColor = CleanWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Info Dialog (triggered by Menu button)
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Dice Roller Instructions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CleanTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "• Motion Detection: Gently shake your phone to roll the dice at any time.",
                        fontSize = 14.sp,
                        color = CleanTextPrimary
                    )
                    Text(
                        text = "• Hold Dice: Tap on any individual die to lock it between rolls (ideal for Yahtzee or board games).",
                        fontSize = 14.sp,
                        color = CleanTextPrimary
                    )
                    Text(
                        text = "• Polyhedral Dice: Switch from standard D6 to D4, D8, D10, D12, D20, or a quick coin flip.",
                        fontSize = 14.sp,
                        color = CleanTextPrimary
                    )
                    Text(
                        text = "• Touch Fallback: Tap on the dice arena or the 'Shake to Roll' pill to roll if the phone is resting on a flat surface.",
                        fontSize = 14.sp,
                        color = CleanTextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got It", color = CleanAccentPurple)
                }
            },
            containerColor = CleanWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun DockItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) CleanPillBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) CleanPillText else CleanTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) CleanPillText else CleanTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun HistoryRow(item: com.example.model.RollHistoryItem) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeString = remember(item.timestamp) { timeFormat.format(Date(item.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CleanBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Total: ${item.total} (${item.dieType.label})",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = CleanTextPrimary
            )
            Text(
                text = "Dice: " + item.values.joinToString(", "),
                fontSize = 12.sp,
                color = CleanTextSecondary
            )
        }
        Text(
            text = timeString,
            fontSize = 11.sp,
            color = CleanTextSecondary.copy(alpha = 0.8f)
        )
    }
}
