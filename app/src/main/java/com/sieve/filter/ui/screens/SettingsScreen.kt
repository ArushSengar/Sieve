package com.sieve.filter.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sieve.filter.ui.components.CupertinoBadge
import com.sieve.filter.ui.components.CupertinoGroupedRow
import com.sieve.filter.ui.components.CupertinoInsetGroup
import com.sieve.filter.ui.components.CupertinoSegmentedControl
import com.sieve.filter.ui.components.CupertinoSwitch
import com.sieve.filter.ui.theme.AppleBlue
import com.sieve.filter.ui.theme.AppleCard
import com.sieve.filter.ui.theme.AppleCardElevated
import com.sieve.filter.ui.theme.AppleCardSecondary
import com.sieve.filter.ui.theme.AppleGreen
import com.sieve.filter.ui.theme.AppleHairline
import com.sieve.filter.ui.theme.AppleOrange
import com.sieve.filter.ui.theme.ApplePurple
import com.sieve.filter.ui.theme.AppleRed
import com.sieve.filter.ui.theme.AppleSeparator
import com.sieve.filter.ui.theme.AppleTextPrimary
import com.sieve.filter.ui.theme.AppleTextSecondary
import com.sieve.filter.ui.theme.AppleTextTertiary
import com.sieve.filter.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    snackbarHostState: SnackbarHostState? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val isFilterEnabled by viewModel.isFilterEnabled.collectAsState()
    val isAiFilterEnabled by viewModel.isAiFilterEnabled.collectAsState()
    val isCommercialShieldEnabled by viewModel.isCommercialShieldEnabled.collectAsState()
    val isQuietHoursEnabled by viewModel.isQuietHoursEnabled.collectAsState()
    val isDeduplicationEnabled by viewModel.isDeduplicationEnabled.collectAsState()
    val logRetentionDays by viewModel.logRetentionDays.collectAsState()
    val isAmoledBlackMode by viewModel.isAmoledBlackMode.collectAsState()
    val startHour by viewModel.quietHoursStartHour.collectAsState()
    val startMinute by viewModel.quietHoursStartMinute.collectAsState()
    val endHour by viewModel.quietHoursEndHour.collectAsState()
    val endMinute by viewModel.quietHoursEndMinute.collectAsState()

    val isListening by viewModel.isServiceListening.collectAsState()
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()
    val isBatteryIgnored by viewModel.isBatteryOptimizationIgnored.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val oem = viewModel.oemGuidance

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearLogsDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportCsvDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var exportedCsvText by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }

    // Re-check system permission states when user returns to app from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkSystemStates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState?.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Group: CORE PROTECTION
        item {
            CupertinoInsetGroup(
                title = "Core Protection Modules",
                footer = "Sieve evaluates incoming notifications 100% on-device. When disabled, all notifications pass through unaltered."
            ) {
                // Master Shield Switch
                CupertinoGroupedRow(
                    title = "Master Shield Protection",
                    subtitle = if (isFilterEnabled) "Actively filtering promotional spam" else "Paused — all notifications pass through",
                    icon = Icons.Default.PowerSettingsNew,
                    iconBg = if (isFilterEnabled) AppleGreen else Color(0xFF39393D),
                    trailing = {
                        CupertinoSwitch(
                            checked = isFilterEnabled,
                            onCheckedChange = viewModel::setFilterEnabled,
                            activeColor = AppleGreen
                        )
                    }
                )

                // AI Spam Blocker
                CupertinoGroupedRow(
                    title = "Smart AI Spam Blocker",
                    subtitle = if (isAiFilterEnabled) "Neural heuristic classifier with candidate rule extraction" else "Disabled — only exact keywords evaluated",
                    icon = Icons.Default.AutoAwesome,
                    iconBg = ApplePurple,
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CupertinoBadge(text = "ON-DEVICE", color = ApplePurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            CupertinoSwitch(
                                checked = isAiFilterEnabled,
                                onCheckedChange = viewModel::setAiFilterEnabled,
                                activeColor = ApplePurple
                            )
                        }
                    }
                )

                // Commercial & Shopping Shield
                CupertinoGroupedRow(
                    title = "Smart E-Commerce Shield",
                    subtitle = if (isCommercialShieldEnabled) "Silences marketing broadcasts. OTPs & deliveries pass through" else "Disabled — shopping apps bypass special heuristics",
                    icon = Icons.Default.ShoppingCart,
                    iconBg = AppleBlue,
                    trailing = {
                        CupertinoSwitch(
                            checked = isCommercialShieldEnabled,
                            onCheckedChange = viewModel::setCommercialShieldEnabled,
                            activeColor = AppleBlue
                        )
                    }
                )

                // Anti-Flooding / Deduplication
                CupertinoGroupedRow(
                    title = "Anti-Flooding Protection",
                    subtitle = if (isDeduplicationEnabled) "Silences identical duplicate notifications within 10 min" else "Disabled",
                    icon = Icons.Default.Repeat,
                    iconBg = AppleOrange,
                    showDivider = false,
                    trailing = {
                        CupertinoSwitch(
                            checked = isDeduplicationEnabled,
                            onCheckedChange = viewModel::setDeduplicationEnabled,
                            activeColor = AppleOrange
                        )
                    }
                )
            }
        }

        // 2. Group: FOCUS & SCHEDULE
        item {
            CupertinoInsetGroup(
                title = "Focus & Quiet Hours",
                footer = if (isQuietHoursEnabled) "Notifications received during quiet hours pass through unblocked." else null
            ) {
                CupertinoGroupedRow(
                    title = "Quiet Hours Schedule",
                    subtitle = if (isQuietHoursEnabled) "Filtering paused during sleep/focus schedule" else "Inactive",
                    icon = Icons.Default.Bedtime,
                    iconBg = ApplePurple,
                    showDivider = isQuietHoursEnabled,
                    trailing = {
                        CupertinoSwitch(
                            checked = isQuietHoursEnabled,
                            onCheckedChange = viewModel::setQuietHoursEnabled,
                            activeColor = ApplePurple
                        )
                    }
                )

                if (isQuietHoursEnabled) {
                    val formatTime: (Int, Int) -> String = { h, m ->
                        val ampm = if (h >= 12) "PM" else "AM"
                        val displayH = when {
                            h == 0 -> 12
                            h > 12 -> h - 12
                            else -> h
                        }
                        String.format("%02d:%02d %s", displayH, m, ampm)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Schedule: ${formatTime(startHour, startMinute)} → ${formatTime(endHour, endMinute)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ApplePurple
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppleCardSecondary)
                                    .clickable { viewModel.setQuietHours(22, 0, 7, 0) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Night (10pm - 7am)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = AppleTextPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppleCardSecondary)
                                    .clickable { viewModel.setQuietHours(23, 0, 8, 0) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Late (11pm - 8am)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = AppleTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Group: SYSTEM PERMISSIONS & HEALTH
        item {
            CupertinoInsetGroup(
                title = "System Diagnostics & Listener",
                footer = "Android requires Notification Listener permission and unrestricted battery background operation to shield notifications."
            ) {
                // Notification Access
                CupertinoGroupedRow(
                    title = "Notification Access",
                    subtitle = if (isPermissionGranted) "Granted in Android system settings" else "Permission missing — Sieve cannot intercept notifications",
                    icon = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                    iconBg = if (isPermissionGranted) AppleGreen else AppleRed,
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    },
                    trailing = {
                        CupertinoBadge(
                            text = if (isPermissionGranted) "Active" else "Grant Access",
                            color = if (isPermissionGranted) AppleGreen else AppleRed
                        )
                    }
                )

                // Battery Optimization
                CupertinoGroupedRow(
                    title = "Background Battery Exemption",
                    subtitle = if (isBatteryIgnored) "Unrestricted — OS will not terminate background listener" else "OS may kill listener in deep sleep",
                    icon = Icons.Default.BatterySaver,
                    iconBg = if (isBatteryIgnored) AppleGreen else AppleOrange,
                    onClick = {
                        if (!isBatteryIgnored && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    },
                    trailing = {
                        CupertinoBadge(
                            text = if (isBatteryIgnored) "Optimal" else "Exempt",
                            color = if (isBatteryIgnored) AppleGreen else AppleOrange
                        )
                    }
                )

                // Service Daemon Binding
                CupertinoGroupedRow(
                    title = "Notification Listener Daemon",
                    subtitle = if (isListening) "Service bound and actively processing notifications" else "Service connection standby",
                    icon = Icons.Default.Security,
                    iconBg = if (isListening) AppleGreen else AppleOrange,
                    onClick = viewModel::triggerRebind,
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CupertinoBadge(
                                text = if (isListening) "Connected" else "Rebind",
                                color = if (isListening) AppleGreen else AppleOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rebind",
                                tint = AppleTextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )

                // OEM Kill Guide
                CupertinoGroupedRow(
                    title = "OEM Optimization (${oem.brandName})",
                    subtitle = if (oem.isKnownAggressive) "Aggressive background killing detected. Tap to view guide." else "Standard Android background management",
                    icon = Icons.Default.Info,
                    iconBg = if (oem.isKnownAggressive) AppleOrange else AppleBlue,
                    showDivider = false,
                    onClick = {
                        try {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(oem.dontKillMyAppUrl))
                            context.startActivity(browserIntent)
                        } catch (_: Exception) {}
                    },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = AppleTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        // 4. Group: DISPLAY & POWER
        item {
            CupertinoInsetGroup(
                title = "Display & Power"
            ) {
                CupertinoGroupedRow(
                    title = "True Black OLED Mode",
                    subtitle = "Pure #000000 background shuts off OLED subpixels for infinite contrast and battery efficiency",
                    icon = Icons.Default.DarkMode,
                    iconBg = AppleBlue,
                    showDivider = false,
                    trailing = {
                        CupertinoSwitch(
                            checked = isAmoledBlackMode,
                            onCheckedChange = viewModel::setAmoledBlackMode,
                            activeColor = AppleBlue
                        )
                    }
                )
            }
        }

        // 5. Group: STORAGE & LOG RETENTION
        item {
            CupertinoInsetGroup(
                title = "Storage & Auto-Pruning",
                footer = "Automatically purges old block log entries to keep the SQLite database lightweight."
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Log Retention Policy",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppleTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val retentionOptions = listOf(7, 14, 30, -1)
                    val selectedIndex = retentionOptions.indexOf(logRetentionDays).coerceAtLeast(0)
                    CupertinoSegmentedControl(
                        items = listOf("7 Days", "14 Days", "30 Days", "Keep All"),
                        selectedIndex = selectedIndex,
                        onItemSelected = { index ->
                            viewModel.setLogRetentionDays(retentionOptions[index])
                        }
                    )
                }
            }
        }

        // 6. Group: DATA & RULES MANAGEMENT
        item {
            CupertinoInsetGroup(
                title = "Data & Rules Management"
            ) {
                // Export Rules JSON
                CupertinoGroupedRow(
                    title = "Export Filter Rules (JSON)",
                    subtitle = "Backup your custom keyword rules to a portable JSON file",
                    icon = Icons.Default.FileDownload,
                    iconBg = AppleBlue,
                    onClick = {
                        coroutineScope.launch {
                            exportedJsonText = viewModel.exportRulesJson()
                            showExportDialog = true
                        }
                    }
                )

                // Import Rules JSON
                CupertinoGroupedRow(
                    title = "Import Filter Rules (JSON)",
                    subtitle = "Restore or merge rules from a JSON backup",
                    icon = Icons.Default.FileUpload,
                    iconBg = AppleBlue,
                    onClick = {
                        importJsonText = ""
                        showImportDialog = true
                    }
                )

                // Export Block Log CSV
                CupertinoGroupedRow(
                    title = "Export Interception Log (CSV)",
                    subtitle = "Export all blocked notification records to a spreadsheet",
                    icon = Icons.Default.TableChart,
                    iconBg = AppleGreen,
                    onClick = {
                        coroutineScope.launch {
                            exportedCsvText = viewModel.exportLogsCsv()
                            showExportCsvDialog = true
                        }
                    }
                )

                // Reset Default Keywords
                CupertinoGroupedRow(
                    title = "Restore Curated Default Rules",
                    subtitle = "Reverts keyword rules to the curated high-accuracy default set",
                    icon = Icons.Default.RestartAlt,
                    iconBg = AppleOrange,
                    onClick = { showResetDialog = true }
                )

                // Clear Block History
                CupertinoGroupedRow(
                    title = "Clear Interception History",
                    subtitle = "Permanently deletes all historical notification log records from SQLite",
                    icon = Icons.Default.DeleteSweep,
                    iconBg = AppleRed,
                    showDivider = false,
                    onClick = { showClearLogsDialog = true }
                )
            }
        }

        // 7. Group: ABOUT & PRIVACY ARCHITECTURE
        item {
            CupertinoInsetGroup(
                title = "About Sieve Sentinel"
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppleGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AppleGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sieve v1.4.0 (Cupertino Sentinel)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary
                            )
                            Text(
                                text = "Zero Telemetry • No Internet Permission",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppleGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sieve operates 100% on your device. The android.permission.INTERNET flag is not declared in the Android manifest, guaranteeing that your notification data cannot ever leak or leave your hardware.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = AppleSeparator)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Engine: Room 2.6.1 + Compose M3",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppleTextTertiary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppleBlue.copy(alpha = 0.12f))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ArushSengar/Sieve"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "GitHub Repository ↗",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleBlue
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Export Rules JSON
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = AppleCardElevated,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Export Rules (JSON)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your keyword rules in JSON format. Copy this to clipboard for backup or sharing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppleCardSecondary)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = exportedJsonText,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleTextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppleBlue)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(exportedJsonText))
                            showExportDialog = false
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Copy to Clipboard", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showExportDialog = false }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Close", color = AppleTextSecondary)
                }
            }
        )
    }

    // Dialog: Import Rules JSON
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = AppleCardElevated,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Import Rules (JSON)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste your exported JSON rules below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("Paste JSON here...") }
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppleBlue)
                        .clickable {
                            viewModel.importRulesJson(importJsonText)
                            showImportDialog = false
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Import Rules", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showImportDialog = false }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Cancel", color = AppleTextSecondary)
                }
            }
        )
    }

    // Dialog: Export CSV
    if (showExportCsvDialog) {
        AlertDialog(
            onDismissRequest = { showExportCsvDialog = false },
            containerColor = AppleCardElevated,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Export Logs (CSV)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your blocked notification log in CSV spreadsheet format:",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppleCardSecondary)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = exportedCsvText,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleTextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppleGreen)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(exportedCsvText))
                            showExportCsvDialog = false
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Copy CSV", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showExportCsvDialog = false }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Close", color = AppleTextSecondary)
                }
            }
        )
    }

    // Dialog: Reset Defaults Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = AppleCardElevated,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Reset Default Rules?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will restore the curated default keyword filter rules. Custom keyword rules will be replaced.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTextSecondary
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppleOrange)
                        .clickable {
                            viewModel.resetDefaultKeywords()
                            showResetDialog = false
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Restore Defaults", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showResetDialog = false }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Cancel", color = AppleTextSecondary)
                }
            }
        )
    }

    // Dialog: Clear Logs Confirmation
    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            containerColor = AppleCardElevated,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Clear All Block History?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleRed
                )
            },
            text = {
                Text(
                    text = "This permanently removes all intercepted notification logs from local storage. This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTextSecondary
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppleRed)
                        .clickable {
                            viewModel.clearAllLogs()
                            showClearLogsDialog = false
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Delete Everything", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showClearLogsDialog = false }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Cancel", color = AppleTextSecondary)
                }
            }
        )
    }
}
