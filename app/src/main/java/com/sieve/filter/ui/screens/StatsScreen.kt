package com.sieve.filter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sieve.filter.model.RuleMatchCount
import com.sieve.filter.ui.components.CupertinoActivityRing
import com.sieve.filter.ui.components.CupertinoBadge
import com.sieve.filter.ui.components.CupertinoInsetGroup
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
import com.sieve.filter.ui.viewmodel.StatsViewModel
import com.sieve.filter.ui.viewmodel.TopAppItem

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val maxAppBlocks = state.topApps.firstOrNull()?.count ?: 1
    val todayTarget = 20f
    val todayProgress = (state.blockedToday.toFloat() / todayTarget).coerceIn(0.05f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Apple Health Activity Hero Ring Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppleCard)
                    .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CupertinoActivityRing(
                            progress = todayProgress,
                            ringColor = AppleGreen,
                            strokeWidth = 10.dp,
                            modifier = Modifier.size(86.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${state.blockedToday}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleTextPrimary
                                )
                                Text(
                                    text = "TODAY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = AppleTextTertiary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Activity Target",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.blockedToday} promotional notifications intercepted today",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppleTextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CupertinoBadge(
                                    text = "${state.totalBlocked} All-Time",
                                    color = ApplePurple,
                                    icon = Icons.Default.Shield
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = AppleSeparator)
                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CupertinoMetricCard(
                            title = "Today",
                            value = "${state.blockedToday}",
                            icon = Icons.Default.Today,
                            accentColor = AppleGreen,
                            modifier = Modifier.weight(1f)
                        )
                        CupertinoMetricCard(
                            title = "This Week",
                            value = "${state.blockedThisWeek}",
                            icon = Icons.Default.DateRange,
                            accentColor = AppleBlue,
                            modifier = Modifier.weight(1f)
                        )
                        CupertinoMetricCard(
                            title = "All Time",
                            value = "${state.totalBlocked}",
                            icon = Icons.Default.Shield,
                            accentColor = ApplePurple,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Most Blocked Apps Inset Group
        item {
            CupertinoInsetGroup(
                title = "Most Blocked Applications",
                footer = if (state.topApps.isNotEmpty()) "Calculated locally from Room SQLite log events" else null
            ) {
                if (state.topApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No blocked apps yet. Stats appear as notifications are intercepted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleTextTertiary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    state.topApps.forEachIndexed { index, appItem ->
                        CupertinoTopAppRow(
                            item = appItem,
                            maxBlocks = maxAppBlocks
                        )
                        if (index < state.topApps.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 58.dp),
                                thickness = 0.5.dp,
                                color = AppleSeparator
                            )
                        }
                    }
                }
            }
        }

        // 3. Top Triggered Rules Inset Group
        if (state.topRules.isNotEmpty()) {
            item {
                CupertinoInsetGroup(
                    title = "Top Triggered Rules"
                ) {
                    state.topRules.forEachIndexed { index, ruleItem ->
                        CupertinoRuleMatchRow(item = ruleItem)
                        if (index < state.topRules.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 52.dp),
                                thickness = 0.5.dp,
                                color = AppleSeparator
                            )
                        }
                    }
                }
            }
        }

        // 4. Apple Privacy & Security Guarantee Footer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppleCardSecondary)
                    .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppleGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AppleGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% On-Device Privacy",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Zero telemetry. All activity and rule hit counts are computed strictly on-device.",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppleTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CupertinoMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppleCardSecondary)
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppleTextPrimary
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = AppleTextTertiary
            )
        }
    }
}

@Composable
fun CupertinoTopAppRow(
    item: TopAppItem,
    maxBlocks: Int
) {
    val fraction = if (maxBlocks > 0) (item.count.toFloat() / maxBlocks).coerceIn(0.05f, 1f) else 0.1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon (34dp squircle)
        if (item.icon != null) {
            Image(
                bitmap = item.icon.toBitmap(64, 64).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppleBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.appName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = AppleBlue,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${item.count} blocked",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleRed
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Apple Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AppleCardSecondary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(AppleRed)
                )
            }
        }
    }
}

@Composable
fun CupertinoRuleMatchRow(item: RuleMatchCount) {
    val isAi = item.matchedRule.startsWith("AI:")
    val isKeyword = item.matchedRule.startsWith("KEYWORD:")
    val badgeColor = if (isAi) ApplePurple else if (isKeyword) AppleRed else AppleOrange

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAi) Icons.Default.AutoAwesome else if (isKeyword) Icons.Default.Block else Icons.Default.Shield,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.matchedRule,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppleTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        CupertinoBadge(
            text = "${item.count} hits",
            color = badgeColor
        )
    }
}
