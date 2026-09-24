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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
                            text = "Configure custom rule behavior per application. 'Auto' applies smart keyword and heuristic filters, 'Allow' passes all notifications unconditionally, and 'Block' intercepts everything.",
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
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Authentic Apple Inset Grouped Table Row with 3-segment Cupertino Mode Switcher.
 */
@Composable
fun CupertinoAppRuleRow(
    app: AppInfo,
    onModeSelected: (AppRuleMode) -> Unit
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
            )
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(AppleBlue.copy(alpha = 0.2f)),
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
                .padding(end = 8.dp)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.blockCount > 0) {
                    CupertinoBadge(
                        text = "${app.blockCount} blocked",
                        color = AppleRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
