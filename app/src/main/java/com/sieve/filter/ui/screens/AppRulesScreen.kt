package com.sieve.filter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sieve.filter.model.AppInfo
import com.sieve.filter.model.AppRuleMode
import com.sieve.filter.ui.components.CupertinoBadge
import com.sieve.filter.ui.components.CupertinoSearchBar
import com.sieve.filter.ui.components.CupertinoSegmentedControl
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
import com.sieve.filter.ui.viewmodel.AppRulesViewModel

@Composable
fun AppRulesScreen(
    viewModel: AppRulesViewModel = viewModel()
) {
    val apps by viewModel.appList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedSegment by remember { mutableIntStateOf(0) }

    // Segment counts
    val totalApps = apps.size
    val autoCount = remember(apps) { apps.count { it.mode == AppRuleMode.AUTO } }
    val allowCount = remember(apps) { apps.count { it.mode == AppRuleMode.ALLOW } }
    val blockCount = remember(apps) { apps.count { it.mode == AppRuleMode.BLOCK } }

    val filteredApps = remember(apps, selectedSegment) {
        when (selectedSegment) {
            1 -> apps.filter { it.mode == AppRuleMode.AUTO }
            2 -> apps.filter { it.mode == AppRuleMode.ALLOW }
            3 -> apps.filter { it.mode == AppRuleMode.BLOCK }
            else -> apps
        }
    }

    var appForQuietHours by remember { mutableStateOf<AppInfo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Apple Inset Explanation Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AppleCard)
                        .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "APP NOTIFICATION POLICIES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextTertiary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Configure custom rule behavior per application. 'Auto' applies smart keyword and heuristic filters, 'Allow' passes all notifications unconditionally, and 'Block' intercepts everything. Tap the moon icon on any app to set custom Quiet Hours.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleTextSecondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CupertinoBadge(text = "Auto ($autoCount)", color = AppleBlue)
                            CupertinoBadge(text = "Allowed ($allowCount)", color = AppleGreen)
                            CupertinoBadge(text = "Blocked ($blockCount)", color = AppleRed)
                        }
                    }
                }
            }

            // 2. Cupertino Search Bar
            item {
                CupertinoSearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search installed applications...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Cupertino Segmented Filter (All, Auto, Allowed, Blocked)
            item {
                CupertinoSegmentedControl(
                    items = listOf("All ($totalApps)", "Auto", "Allowed", "Blocked"),
                    selectedIndex = selectedSegment,
                    onItemSelected = { selectedSegment = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Loading or Empty State
            if (isLoading && apps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppleBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else if (filteredApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No applications matching \"$searchQuery\"" else "No applications in this category",
                            color = AppleTextTertiary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // 5. Virtualized Cupertino App Rule Cards
                items(
                    items = filteredApps,
                    key = { it.packageName }
                ) { app ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppleCard)
                            .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(16.dp))
                    ) {
                        CupertinoAppRuleRow(
                            app = app,
                            onModeSelected = { mode ->
                                viewModel.setAppMode(app.packageName, mode)
                            },
                            onQuietHoursClick = {
                                appForQuietHours = app
                            }
                        )
                    }
                }
            }
        }
    }

    if (appForQuietHours != null) {
        CupertinoAppQuietHoursDialog(
            app = appForQuietHours!!,
            onDismiss = { appForQuietHours = null },
            onSave = { enabled, startMins, endMins ->
                viewModel.setAppQuietHours(appForQuietHours!!.packageName, enabled, startMins, endMins)
                appForQuietHours = null
            }
        )
    }
}

/**
 * Authentic Apple Inset Grouped Table Row with 3-segment Cupertino Mode Switcher
 * and Quiet Hours snooze configurator.
 */
@Composable
fun CupertinoAppRuleRow(
    app: AppInfo,
    onModeSelected: (AppRuleMode) -> Unit,
    onQuietHoursClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App squircle icon (38dp, 9dp corner radius)
        if (app.icon != null) {
            Image(
                bitmap = app.icon.toBitmap(64, 64).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .clickable(onClick = onQuietHoursClick)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(AppleBlue.copy(alpha = 0.2f))
                    .clickable(onClick = onQuietHoursClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.appName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = AppleBlue,
                    fontSize = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // App Details
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onQuietHoursClick
                )
                .padding(end = 6.dp)
        ) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppleTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (app.quietHoursEnabled) {
                    val sH = app.quietHoursStartMinutes / 60
                    val sM = app.quietHoursStartMinutes % 60
                    val eH = app.quietHoursEndMinutes / 60
                    val eM = app.quietHoursEndMinutes % 60
                    CupertinoBadge(
                        text = "🌙 %02d:%02d–%02d:%02d".format(sH, sM, eH, eM),
                        color = ApplePurple
                    )
                }
                if (app.blockCount > 0) {
                    CupertinoBadge(
                        text = "${app.blockCount} blocked",
                        color = AppleRed
                    )
                }
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleTextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Quiet Hours Moon Button
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (app.quietHoursEnabled) ApplePurple.copy(alpha = 0.2f) else AppleCardSecondary)
                .clickable(onClick = onQuietHoursClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Bedtime,
                contentDescription = "Quiet Hours",
                tint = if (app.quietHoursEnabled) ApplePurple else AppleTextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Apple 3-State Mode Pill Selector (Auto | Allow | Block)
        CupertinoModeSelectorPill(
            currentMode = app.mode,
            onModeSelected = onModeSelected
        )
    }
}

/**
 * Compact iOS Segmented Control for 3 Modes.
 */
@Composable
fun CupertinoModeSelectorPill(
    currentMode: AppRuleMode,
    onModeSelected: (AppRuleMode) -> Unit
) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppleCardSecondary)
            .padding(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CupertinoModeSegment(
                label = "Auto",
                isSelected = currentMode == AppRuleMode.AUTO,
                accentColor = AppleBlue,
                onClick = { onModeSelected(AppRuleMode.AUTO) }
            )
            CupertinoModeSegment(
                label = "Allow",
                isSelected = currentMode == AppRuleMode.ALLOW,
                accentColor = AppleGreen,
                onClick = { onModeSelected(AppRuleMode.ALLOW) }
            )
            CupertinoModeSegment(
                label = "Block",
                isSelected = currentMode == AppRuleMode.BLOCK,
                accentColor = AppleRed,
                onClick = { onModeSelected(AppRuleMode.BLOCK) }
            )
        }
    }
}

@Composable
fun CupertinoModeSegment(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(6.dp))
                        .background(AppleCardElevated)
                } else {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                }
            )
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) accentColor else AppleTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * P1 Quiet Hours / Snooze Per-App Dialog.
 * Configures time window where notifications queue and deliver as a batch at window end.
 */
@Composable
fun CupertinoAppQuietHoursDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    onSave: (enabled: Boolean, startMinutes: Int, endMinutes: Int) -> Unit
) {
    var isEnabled by remember { mutableStateOf(app.quietHoursEnabled) }
    var startMins by remember { mutableIntStateOf(if (app.quietHoursStartMinutes >= 0) app.quietHoursStartMinutes else 22 * 60) }
    var endMins by remember { mutableIntStateOf(if (app.quietHoursEndMinutes >= 0) app.quietHoursEndMinutes else 7 * 60) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppleCardElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(ApplePurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = ApplePurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Quiet Hours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Snooze notifications from ${app.appName} during this window. Notifications queue and deliver as a batch when window ends.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTextSecondary,
                    lineHeight = 18.sp
                )

                // Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppleCard)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Quiet Hours",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextPrimary
                    )
                    com.sieve.filter.ui.components.CupertinoSwitch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        activeColor = ApplePurple
                    )
                }

                if (isEnabled) {
                    // Time Range Selector Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppleCard)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Start Time (From)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Start Window",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppleTextSecondary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TimeAdjustButton(label = "-1h") { startMins = (startMins - 60 + 1440) % 1440 }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "%02d:%02d".format(startMins / 60, startMins % 60),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                TimeAdjustButton(label = "+1h") { startMins = (startMins + 60) % 1440 }
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = AppleSeparator)

                        // End Time (Deliver)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "End Window (Deliver)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppleTextSecondary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TimeAdjustButton(label = "-1h") { endMins = (endMins - 60 + 1440) % 1440 }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "%02d:%02d".format(endMins / 60, endMins % 60),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                TimeAdjustButton(label = "+1h") { endMins = (endMins + 60) % 1440 }
                            }
                        }
                    }

                    // Quick Presets
                    Text(
                        text = "QUICK PRESETS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextTertiary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetPill(
                            label = "Night\n22-07",
                            isSelected = startMins == 22 * 60 && endMins == 7 * 60,
                            modifier = Modifier.weight(1f)
                        ) {
                            startMins = 22 * 60
                            endMins = 7 * 60
                        }
                        PresetPill(
                            label = "Work\n09-17",
                            isSelected = startMins == 9 * 60 && endMins == 17 * 60,
                            modifier = Modifier.weight(1f)
                        ) {
                            startMins = 9 * 60
                            endMins = 17 * 60
                        }
                        PresetPill(
                            label = "Focus\n13-18",
                            isSelected = startMins == 13 * 60 && endMins == 18 * 60,
                            modifier = Modifier.weight(1f)
                        ) {
                            startMins = 13 * 60
                            endMins = 18 * 60
                        }
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppleBlue)
                    .clickable { onSave(isEnabled, startMins, endMins) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = AppleTextSecondary
                )
            }
        }
    )
}

@Composable
private fun TimeAdjustButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AppleCardSecondary)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AppleBlue
        )
    }
}

@Composable
private fun PresetPill(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ApplePurple.copy(alpha = 0.2f) else AppleCardSecondary)
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) ApplePurple else AppleHairline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) ApplePurple else AppleTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}
