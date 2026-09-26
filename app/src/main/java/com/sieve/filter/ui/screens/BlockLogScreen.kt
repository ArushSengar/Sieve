package com.sieve.filter.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sieve.filter.model.RuleAction
import com.sieve.filter.ui.components.AddKeywordDialog
import com.sieve.filter.ui.components.CupertinoActivityRing
import com.sieve.filter.ui.components.CupertinoBadge
import com.sieve.filter.ui.components.CupertinoSearchBar
import com.sieve.filter.ui.components.CupertinoSegmentedControl
import com.sieve.filter.ui.components.PermissionBanner
import com.sieve.filter.ui.components.SimulateNotificationDialog
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
import com.sieve.filter.ui.theme.customTokens
import com.sieve.filter.ui.viewmodel.BlockLogDisplayItem
import com.sieve.filter.ui.viewmodel.BlockLogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BlockLogScreen(
    viewModel: BlockLogViewModel = viewModel()
) {
    val logs by viewModel.logs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedFilterSegment by remember { mutableIntStateOf(0) }
    var showSimulateDialog by remember { mutableStateOf(false) }
    var selectedItemForDetail by remember { mutableStateOf<BlockLogDisplayItem?>(null) }
    var keywordDialogTarget by remember { mutableStateOf<Pair<String, String?>?>(null) }

    // Categorized stats for Cupertino Activity Ring Hero
    val totalCount = logs.size
    val aiCount = remember(logs) { logs.count { it.entity.matchedRule.startsWith("AI:") } }
    val keywordCount = remember(logs) { logs.count { it.entity.matchedRule.startsWith("KEYWORD:") } }
    val floodCount = totalCount - aiCount - keywordCount

    // Filter logs according to the selected Cupertino segmented pill
    val filteredLogs = remember(logs, selectedFilterSegment) {
        when (selectedFilterSegment) {
            1 -> logs.filter { it.entity.matchedRule.startsWith("AI:") }
            2 -> logs.filter { it.entity.matchedRule.startsWith("KEYWORD:") }
            3 -> logs.filter { !it.entity.matchedRule.startsWith("AI:") && !it.entity.matchedRule.startsWith("KEYWORD:") }
            else -> logs
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        // Notification Access Health Banner
        PermissionBanner()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Cupertino Sentinel Activity Hero
            item {
                CupertinoSentinelHeroCard(
                    totalBlocked = totalCount,
                    aiCount = aiCount,
                    keywordCount = keywordCount,
                    onSimulateClick = { showSimulateDialog = true },
                    onClearClick = if (logs.isNotEmpty()) {
                        { viewModel.clearAllLogs() }
                    } else null
                )
            }

            // 2. Cupertino Search Bar
            item {
                CupertinoSearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search intercepted notifications...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Cupertino Segmented Filter (All, AI Spam, Keywords, Flood)
            item {
                CupertinoSegmentedControl(
                    items = listOf("All ($totalCount)", "AI ($aiCount)", "Keywords ($keywordCount)", "Other ($floodCount)"),
                    selectedIndex = selectedFilterSegment,
                    onItemSelected = { selectedFilterSegment = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Log Items or Cupertino Empty View
            if (filteredLogs.isEmpty()) {
                item {
                    CupertinoEmptyBlockLogView(
                        isFiltered = searchQuery.isNotEmpty() || selectedFilterSegment != 0
                    )
                }
            } else {
                items(
                    items = filteredLogs,
                    key = { it.entity.id }
                ) { item ->
                    CupertinoBlockLogItemCard(
                        item = item,
                        onClick = { selectedItemForDetail = item },
                        onAlwaysAllow = { viewModel.alwaysAllowApp(item.entity.packageName) },
                        onVerifyKeyword = { pattern, pkg ->
                            keywordDialogTarget = Pair(pattern, pkg)
                        },
                        onDelete = { viewModel.deleteLog(item.entity.id) }
                    )
                }
            }
        }
    }

    // P2: Floating Undo Banner with 5s Auto-dismiss
        val lastBlockedEvent by viewModel.lastBlockedEvent.collectAsState()
        com.sieve.filter.ui.components.UndoToast(
            event = lastBlockedEvent,
            onUndo = {
                lastBlockedEvent?.let { viewModel.undoLastBlock(it) }
            },
            onDismiss = {
                viewModel.dismissUndoToast()
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Dialogs
    if (showSimulateDialog) {
        SimulateNotificationDialog(
            onDismiss = { showSimulateDialog = false }
        )
    }

    if (keywordDialogTarget != null) {
        AddKeywordDialog(
            initialPattern = keywordDialogTarget!!.first,
            initialPackageName = keywordDialogTarget!!.second,
            initialAction = RuleAction.BLOCK,
            onDismiss = { keywordDialogTarget = null },
            onConfirm = { pattern, _, packageName ->
                viewModel.addKeywordBlockRule(pattern, packageName)
                keywordDialogTarget = null
            }
        )
    }

    // P0: Explain This Block Bottom Sheet
    if (selectedItemForDetail != null) {
        com.sieve.filter.ui.components.ExplainBlockSheet(
            item = selectedItemForDetail!!,
            onDismiss = { selectedItemForDetail = null },
            onRestoreNotification = {
                viewModel.restoreNotification(selectedItemForDetail!!)
                selectedItemForDetail = null
            },
            onAlwaysAllowApp = {
                viewModel.alwaysAllowApp(selectedItemForDetail!!.entity.packageName)
                selectedItemForDetail = null
            },
            onAlwaysAllowPattern = { pattern ->
                viewModel.alwaysAllowPattern(pattern)
                selectedItemForDetail = null
            },
            onDelete = {
                viewModel.deleteLog(selectedItemForDetail!!.entity.id)
                selectedItemForDetail = null
            }
        )
    }
}

/**
 * Apple iOS 18 Sentinel Activity Hero Card.
 * Displays concentric telemetry and Apple Activity Ring with live interception metrics.
 */
@Composable
fun CupertinoSentinelHeroCard(
    totalBlocked: Int,
    aiCount: Int,
    keywordCount: Int,
    onSimulateClick: () -> Unit,
    onClearClick: (() -> Unit)? = null
) {
    val progress = if (totalBlocked == 0) 0.05f else ((totalBlocked.coerceAtMost(50)) / 50f)
    val accent = MaterialTheme.colorScheme.primary
    val isDark = MaterialTheme.customTokens.isDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = if (isDark) 0.12f else 0.06f),
                        AppleCard
                    )
                )
            )
            .border(
                width = 1.dp,
                color = accent.copy(alpha = if (isDark) 0.25f else 0.15f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Apple Activity Ring styled with active accent color
                CupertinoActivityRing(
                    progress = progress,
                    ringColor = accent,
                    strokeWidth = 10.dp,
                    modifier = Modifier.size(82.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (totalBlocked > 999) "999+" else "$totalBlocked",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextPrimary
                        )
                        Text(
                            text = "SHIELDED",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = AppleTextTertiary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Telemetry & Status
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sentinel Active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "100% on-device heuristic & zero telemetry",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CupertinoBadge(
                            text = "AI: $aiCount",
                            color = ApplePurple,
                            icon = Icons.Default.AutoAwesome
                        )
                        CupertinoBadge(
                            text = "Rules: $keywordCount",
                            color = accent,
                            icon = Icons.Default.Shield
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = AppleSeparator)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Notification Simulator Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .clickable(onClick = onSimulateClick)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simulate & Test",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                }

                // Clear All Logs Pill
                if (onClearClick != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppleRed.copy(alpha = 0.10f))
                            .clickable(onClick = onClearClick)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = null,
                            tint = AppleRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Clear History",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleRed
                        )
                    }
                }
            }
        }
    }
}

/**
 * Authentic Apple Inset Grouped Notification Card.
 */
@Composable
fun CupertinoBlockLogItemCard(
    item: BlockLogDisplayItem,
    onClick: () -> Unit,
    onAlwaysAllow: () -> Unit,
    onVerifyKeyword: ((pattern: String, packageName: String?) -> Unit)? = null,
    onDelete: () -> Unit
) {
    val timeAgo = DateUtils.getRelativeTimeSpanString(
        item.entity.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    val isAiBlocked = item.entity.matchedRule.startsWith("AI:")
    val isKeywordBlocked = item.entity.matchedRule.startsWith("KEYWORD:")
    val extractedKeyword = if (isAiBlocked) {
        if (item.entity.matchedRule.contains("(") && item.entity.matchedRule.endsWith(")")) {
            item.entity.matchedRule.substringAfter("(").substringBeforeLast(")").trim()
        } else {
            item.entity.title?.take(25) ?: ""
        }
    } else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppleCard)
            .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            // Header: App squircle icon, title, time, delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (item.appIcon != null) {
                    Image(
                        bitmap = item.appIcon.toBitmap(64, 64).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(9.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(AppleBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.appName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = AppleBlue,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.entity.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppleTextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleTextTertiary
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = AppleTextTertiary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notification Content
            if (!item.entity.title.isNullOrBlank()) {
                Text(
                    text = item.entity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!item.entity.textSnippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.entity.textSnippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Cupertino Badges & Quick Action Pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Rule Badge
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    if (isAiBlocked) {
                        CupertinoBadge(
                            text = item.entity.matchedRule,
                            color = ApplePurple,
                            icon = Icons.Default.AutoAwesome
                        )
                    } else if (isKeywordBlocked) {
                        CupertinoBadge(
                            text = item.entity.matchedRule,
                            color = AppleRed,
                            icon = Icons.Default.Block
                        )
                    } else {
                        CupertinoBadge(
                            text = item.entity.matchedRule,
                            color = AppleOrange,
                            icon = Icons.Default.Shield
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isAiBlocked && !extractedKeyword.isNullOrBlank() && onVerifyKeyword != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ApplePurple.copy(alpha = 0.15f))
                                .clickable { onVerifyKeyword(extractedKeyword, null) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Verify Rule",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ApplePurple,
                                softWrap = false
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppleGreen.copy(alpha = 0.15f))
                            .clickable(onClick = onAlwaysAllow)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Always Allow",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleGreen,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cupertino Action Sheet / Modal Details Dialog.
 */
@Composable
fun CupertinoBlockLogDetailDialog(
    item: BlockLogDisplayItem,
    onDismiss: () -> Unit,
    onAlwaysAllow: () -> Unit,
    onVerifyKeyword: ((pattern: String, packageName: String?) -> Unit)? = null,
    onDelete: () -> Unit
) {
    val dateFormat = remember {
        SimpleDateFormat("MMM dd, yyyy • hh:mm:ss a", Locale.getDefault())
    }
    val formattedDate = remember(item.entity.timestamp) {
        dateFormat.format(Date(item.entity.timestamp))
    }

    val isAiBlocked = item.entity.matchedRule.startsWith("AI:")
    val extractedKeyword = if (isAiBlocked) {
        if (item.entity.matchedRule.contains("(") && item.entity.matchedRule.endsWith(")")) {
            item.entity.matchedRule.substringAfter("(").substringBeforeLast(")").trim()
        } else {
            item.entity.title?.take(25) ?: ""
        }
    } else null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppleCardElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.appIcon != null) {
                    Image(
                        bitmap = item.appIcon.toBitmap(56, 56).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(9.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = item.entity.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppleTextTertiary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!item.entity.title.isNullOrBlank()) {
                    Column {
                        Text(
                            text = "TITLE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextTertiary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.entity.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = AppleTextPrimary
                        )
                    }
                }

                if (!item.entity.textSnippet.isNullOrBlank()) {
                    Column {
                        Text(
                            text = "MESSAGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextTertiary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.entity.textSnippet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppleTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppleCardSecondary)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Matched Rule: ${item.entity.matchedRule}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAiBlocked) ApplePurple else AppleRed
                        )
                        if (isAiBlocked && !extractedKeyword.isNullOrBlank()) {
                            Text(
                                text = "Candidate Keyword: \"$extractedKeyword\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleBlue
                            )
                        }
                        item.entity.channelId?.let {
                            Text(
                                text = "Channel: $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppleTextTertiary
                            )
                        }
                        Text(
                            text = "Intercepted: $formattedDate",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppleTextTertiary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isAiBlocked && !extractedKeyword.isNullOrBlank() && onVerifyKeyword != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ApplePurple.copy(alpha = 0.15f))
                            .clickable { onVerifyKeyword(extractedKeyword, null) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Verify Rule",
                            color = ApplePurple,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleGreen.copy(alpha = 0.15f))
                        .clickable(onClick = onAlwaysAllow)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Always Allow",
                        color = AppleGreen,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleRed.copy(alpha = 0.15f))
                        .clickable(onClick = onDelete)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Delete",
                        color = AppleRed,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleCardSecondary)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Close",
                        color = AppleTextPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    )
}

/**
 * Cupertino Empty State.
 */
@Composable
fun CupertinoEmptyBlockLogView(
    isFiltered: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AppleGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFiltered) Icons.Default.NotificationsOff else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppleGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFiltered) "No Matching Logs" else "Clean Notification Shade",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppleTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isFiltered)
                    "No blocked notifications match your current search or segment filter."
                else
                    "Sieve is actively guarding your notification tray. Any intercepted promotional spam will be cataloged here.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppleTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
