package com.sieve.filter.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
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
import com.sieve.filter.ui.viewmodel.BlockLogDisplayItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * P0 "Explain This Block" Bottom Sheet.
 * Displays the exact pipeline stage that fired, the matched rule/keyword pattern,
 * notification preview, and provides one-tap actions for best-effort restoration
 * and writing explicit ALWAYS_ALLOW / ALLOW rules.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplainBlockSheet(
    item: BlockLogDisplayItem,
    onDismiss: () -> Unit,
    onRestoreNotification: () -> Unit,
    onAlwaysAllowApp: () -> Unit,
    onAlwaysAllowPattern: (pattern: String) -> Unit,
    onDelete: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val dateFormat = remember {
        SimpleDateFormat("MMM dd, yyyy • hh:mm:ss a", Locale.getDefault())
    }
    val formattedDate = remember(item.entity.timestamp) {
        dateFormat.format(Date(item.entity.timestamp))
    }

    // Resolve stage details
    val (stageName, stageColor, stageIcon, stageExplanation) = resolveStageInfo(item.entity.stageId, item.entity.matchedRule)

    // Resolve pattern to allow
    val patternToAllow = item.entity.matchedPatternId
        ?: if (item.entity.matchedRule.startsWith("AI:") && item.entity.matchedRule.contains("(") && item.entity.matchedRule.endsWith(")")) {
            item.entity.matchedRule.substringAfter("(").substringBeforeLast(")").trim()
        } else if (item.entity.matchedRule.startsWith("Keyword:") || item.entity.matchedRule.startsWith("KEYWORD:")) {
            item.entity.matchedRule.substringAfter(":").trim()
        } else {
            null
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppleCardElevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(AppleSeparator)
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header with App Icon and Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.appIcon != null) {
                    Image(
                        bitmap = item.appIcon.toBitmap(56, 56).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppleBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.appName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = AppleBlue,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = item.entity.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AppleTextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Pipeline Stage Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppleCard)
                    .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(stageColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = stageIcon,
                                    contentDescription = null,
                                    tint = stageColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stageName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = stageColor
                            )
                        }

                        CupertinoBadge(
                            text = if (item.entity.stageId > 0) "Stage ${item.entity.stageId}" else "Pipeline",
                            color = stageColor
                        )
                    }

                    Text(
                        text = stageExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary,
                        lineHeight = 18.sp
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = AppleSeparator)

                    // Matched Rule / Keyword
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MATCHED RULE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextTertiary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = item.entity.matchedRule,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextPrimary
                        )
                    }

                    // Timestamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INTERCEPTED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextTertiary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppleTextSecondary
                        )
                    }
                }
            }

            // 3. Intercepted Notification Content Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppleCard)
                    .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NOTIFICATION CONTENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextTertiary,
                        letterSpacing = 0.5.sp
                    )

                    if (!item.entity.title.isNullOrBlank()) {
                        Text(
                            text = item.entity.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextPrimary
                        )
                    }

                    if (!item.entity.textSnippet.isNullOrBlank()) {
                        Text(
                            text = item.entity.textSnippet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppleTextSecondary,
                            lineHeight = 20.sp
                        )
                    }

                    if (item.entity.title.isNullOrBlank() && item.entity.textSnippet.isNullOrBlank()) {
                        Text(
                            text = "No text content (Custom RemoteView layout)",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleTextTertiary
                        )
                    }

                    if (!item.entity.channelId.isNullOrBlank()) {
                        Text(
                            text = "Channel: ${item.entity.channelId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppleTextTertiary
                        )
                    }
                }
            }

            // 4. One-Tap Actions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Restore notification (best-effort)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppleBlue)
                        .clickable(onClick = onRestoreNotification)
                        .padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Restore Notification",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Always allow this pattern (if pattern extracted)
                if (!patternToAllow.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppleCard)
                            .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(14.dp))
                            .clickable { onAlwaysAllowPattern(patternToAllow) }
                            .padding(vertical = 13.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppleGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Always Allow Pattern \"${patternToAllow.take(20)}\"",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Always allow this app
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppleCard)
                        .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(14.dp))
                        .clickable(onClick = onAlwaysAllowApp)
                        .padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AppleGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Always Allow App (${item.appName})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleGreen
                    )
                }

                // Delete log entry
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppleRed.copy(alpha = 0.1f))
                        .clickable(onClick = onDelete)
                        .padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = AppleRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete From Log",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleRed
                    )
                }
            }
        }
    }
}

private data class StageInfo(
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val explanation: String
)

private fun resolveStageInfo(stageId: Int, matchedRule: String): StageInfo {
    return when {
        stageId == 5 || matchedRule.startsWith("AppRule:") -> StageInfo(
            name = "App Rule Override",
            color = AppleRed,
            icon = Icons.Default.Shield,
            explanation = "Blocked because this application has been explicitly designated as 'Always Block' in your App Rules policy."
        )
        stageId == 6 || matchedRule.startsWith("Keyword:") || matchedRule.startsWith("KEYWORD:") || matchedRule.startsWith("Channel:") -> StageInfo(
            name = "Keyword Rule Match",
            color = AppleOrange,
            icon = Icons.Default.FilterAlt,
            explanation = "Intercepted because the notification title, body, or channel matched a deterministic keyword or regex rule configured on this device."
        )
        stageId == 7 || matchedRule.startsWith("AI:") -> StageInfo(
            name = "Smart AI Classifier",
            color = ApplePurple,
            icon = Icons.Default.AutoAwesome,
            explanation = "Evaluated on-device by Sieve's neural-heuristic NLP engine. Detected promotional bait, marketing discount traps, engagement nudges, or gambling solicitations."
        )
        stageId == 8 || matchedRule.startsWith("Anti-Flooding:") -> StageInfo(
            name = "Anti-Flooding Deduplication",
            color = AppleBlue,
            icon = Icons.Default.Repeat,
            explanation = "Suppressed to preserve focus. A notification with the identical title and message was already posted by this app within the past 10 minutes."
        )
        stageId == 9 || matchedRule.startsWith("Quiet Hours:") -> StageInfo(
            name = "Quiet Hours Snooze",
            color = AppleBlue,
            icon = Icons.Default.Bedtime,
            explanation = "Snoozed because this application has a quiet-hours window active right now. Notifications are queued and delivered at window end."
        )
        else -> StageInfo(
            name = "Sentinel Filtered",
            color = AppleOrange,
            icon = Icons.Default.Security,
            explanation = "Intercepted and dismissed by Sieve's multi-stage on-device pipeline."
        )
    }
}
