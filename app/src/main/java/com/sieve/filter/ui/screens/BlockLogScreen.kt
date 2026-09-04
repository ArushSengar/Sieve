package com.sieve.filter.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sieve.filter.model.RuleAction
import com.sieve.filter.ui.components.AddKeywordDialog
import com.sieve.filter.ui.components.PermissionBanner
import com.sieve.filter.ui.components.SimulateNotificationDialog
import com.sieve.filter.ui.theme.AiPurple
import com.sieve.filter.ui.theme.AiPurpleBg
import com.sieve.filter.ui.theme.AllowGreen
import com.sieve.filter.ui.theme.BlockRed
import com.sieve.filter.ui.theme.BlockRedBg
import com.sieve.filter.ui.viewmodel.BlockLogDisplayItem
import com.sieve.filter.ui.viewmodel.BlockLogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BlockLogScreen(
    viewModel: BlockLogViewModel = viewModel()
) {
    val logs by viewModel.logs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSimulateDialog by remember { mutableStateOf(false) }
    var selectedItemForDetail by remember { mutableStateOf<BlockLogDisplayItem?>(null) }
    var keywordDialogTarget by remember { mutableStateOf<Pair<String, String?>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Notification Access Health Banner
        PermissionBanner()

        // Search Bar, Simulate Test Button & Clear Action Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Search blocked logs...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { showSimulateDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = "Simulate & Test Notification",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (logs.isNotEmpty()) {
                IconButton(onClick = viewModel::clearAllLogs) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Clear All Logs",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

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

        if (selectedItemForDetail != null) {
            BlockLogDetailDialog(
                item = selectedItemForDetail!!,
                onDismiss = { selectedItemForDetail = null },
                onAlwaysAllow = {
                    viewModel.alwaysAllowApp(selectedItemForDetail!!.entity.packageName)
                    selectedItemForDetail = null
                },
                onVerifyKeyword = { pattern, pkg ->
                    keywordDialogTarget = Pair(pattern, pkg)
                    selectedItemForDetail = null
                },
                onDelete = {
                    viewModel.deleteLog(selectedItemForDetail!!.entity.id)
                    selectedItemForDetail = null
                }
            )
        }

        if (logs.isEmpty()) {
            EmptyBlockLogView()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = logs,
                    key = { it.entity.id }
                ) { item ->
                    BlockLogItemCard(
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
}

@Composable
fun BlockLogItemCard(
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
    val extractedKeyword = if (isAiBlocked) {
        if (item.entity.matchedRule.contains("(") && item.entity.matchedRule.endsWith(")")) {
            item.entity.matchedRule.substringAfter("(").substringBeforeLast(")").trim()
        } else {
            item.entity.title?.take(25) ?: ""
        }
    } else null

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: App icon, App name, time, delete
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
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.appName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.entity.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete log entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notification Title & Text
            if (!item.entity.title.isNullOrBlank()) {
                Text(
                    text = item.entity.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!item.entity.textSnippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.entity.textSnippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Matched Rule badge & Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Dismissed Reason badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAiBlocked) AiPurpleBg else BlockRedBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAiBlocked) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AiPurple,
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(BlockRed)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.entity.matchedRule,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAiBlocked) AiPurple else BlockRed,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isAiBlocked && !extractedKeyword.isNullOrBlank() && onVerifyKeyword != null) {
                        OutlinedButton(
                            onClick = { onVerifyKeyword(extractedKeyword, null) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AiPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verify Keyword",
                                style = MaterialTheme.typography.labelMedium,
                                color = AiPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Quick Action: Always Allow
                    OutlinedButton(
                        onClick = onAlwaysAllow,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AllowGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Always Allow",
                            style = MaterialTheme.typography.labelMedium,
                            color = AllowGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BlockLogDetailDialog(
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.appIcon != null) {
                    Image(
                        bitmap = item.appIcon.toBitmap(56, 56).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.entity.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.entity.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (!item.entity.textSnippet.isNullOrBlank()) {
                    Column {
                        Text(
                            text = "MESSAGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.entity.textSnippet,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAiBlocked) AiPurpleBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Matched: ${item.entity.matchedRule}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAiBlocked) AiPurple else BlockRed
                        )
                        if (isAiBlocked && !extractedKeyword.isNullOrBlank()) {
                            Text(
                                text = "Candidate Keyword: \"$extractedKeyword\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item.entity.channelId?.let {
                            Text(
                                text = "Channel: $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Logged: $formattedDate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row {
                if (isAiBlocked && !extractedKeyword.isNullOrBlank() && onVerifyKeyword != null) {
                    TextButton(
                        onClick = {
                            onVerifyKeyword(extractedKeyword, null)
                        }
                    ) {
                        Text("Verify Keyword", color = AiPurple, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                TextButton(
                    onClick = {
                        onAlwaysAllow()
                    }
                ) {
                    Text("Always Allow App")
                }
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        onDelete()
                    }
                ) {
                    Text("Delete Log", color = BlockRed)
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun EmptyBlockLogView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Blocked Notifications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sieve is quietly guarding your notification shade. Any promotional spam dismissed will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
