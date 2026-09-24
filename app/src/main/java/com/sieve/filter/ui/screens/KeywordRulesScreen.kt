package com.sieve.filter.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sieve.filter.data.local.entity.AiSuggestedRuleEntity
import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.model.RuleAction
import com.sieve.filter.ui.components.AddKeywordDialog
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
import com.sieve.filter.ui.viewmodel.KeywordFilterTab
import com.sieve.filter.ui.viewmodel.KeywordRulesViewModel

@Composable
fun KeywordRulesScreen(
    viewModel: KeywordRulesViewModel = viewModel()
) {
    val rules by viewModel.rules.collectAsState()
    val pendingSuggestions by viewModel.pendingAiSuggestions.collectAsState()
    val pendingCount by viewModel.pendingAiCount.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val totalRules = rules.size
    val blockRulesCount = remember(rules) { rules.count { it.getRuleAction() == RuleAction.BLOCK } }
    val allowRulesCount = remember(rules) { rules.count { it.getRuleAction() == RuleAction.ALLOW } }

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
            // 1. Cupertino Segmented Control (All, Block, Allow, AI Suggestions)
            item {
                CupertinoSegmentedControl(
                    items = listOf(
                        "All ($totalRules)",
                        "Block ($blockRulesCount)",
                        "Allow ($allowRulesCount)",
                        if (pendingCount > 0) "AI ($pendingCount)" else "AI Sparks"
                    ),
                    selectedIndex = selectedTab.ordinal,
                    onItemSelected = { index ->
                        viewModel.onTabSelected(KeywordFilterTab.entries[index])
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. AI Suggestions Alert Banner (if user is on another tab but pending suggestions exist)
            if (selectedTab != KeywordFilterTab.AI_SUGGESTIONS && pendingCount > 0) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ApplePurple.copy(alpha = 0.12f))
                            .border(width = 0.5.dp, color = ApplePurple.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
                            .clickable { viewModel.onTabSelected(KeywordFilterTab.AI_SUGGESTIONS) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ApplePurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "$pendingCount New AI Detection${if (pendingCount > 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleTextPrimary
                                    )
                                    Text(
                                        text = "Tap to review candidate spam keywords",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppleTextSecondary
                                    )
                                }
                            }
                            Text(
                                text = "Review →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ApplePurple
                            )
                        }
                    }
                }
            }

            // 3. Search Bar & Action Buttons Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CupertinoSearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        placeholder = if (selectedTab == KeywordFilterTab.AI_SUGGESTIONS) "Filter AI suggestions..." else "Filter keyword rules...",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // + New Rule Apple Pill Button
                    if (selectedTab != KeywordFilterTab.AI_SUGGESTIONS) {
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppleBlue)
                                .clickable { showAddDialog = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Reset Defaults Pill
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppleCardSecondary)
                                .clickable { viewModel.resetToDefaults() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Defaults",
                                tint = AppleTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 4. Content Area
            if (selectedTab == KeywordFilterTab.AI_SUGGESTIONS) {
                // AI Suggestions List
                if (pendingSuggestions.isEmpty()) {
                    item {
                        CupertinoEmptyAiSuggestionsView()
                    }
                } else {
                    items(
                        count = pendingSuggestions.size,
                        key = { pendingSuggestions[it].id }
                    ) { index ->
                        val suggestion = pendingSuggestions[index]
                        CupertinoAiSuggestedRuleCard(
                            suggestion = suggestion,
                            onVerifyAndAdd = { asGlobal ->
                                viewModel.acceptAiSuggestion(suggestion.id, asGlobal)
                            },
                            onDismiss = { viewModel.dismissAiSuggestion(suggestion.id) }
                        )
                    }
                }
            } else {
                // Inset Grouped Keyword Rules
                if (rules.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No rules match \"$searchQuery\"" else "No keyword rules defined",
                                color = AppleTextTertiary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppleCard)
                                .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(20.dp))
                        ) {
                            Column {
                                rules.forEachIndexed { index, rule ->
                                    CupertinoKeywordRuleRow(
                                        rule = rule,
                                        onDelete = { viewModel.deleteRule(rule.id) }
                                    )
                                    if (index < rules.size - 1) {
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
                }
            }
        }
    }

    if (showAddDialog) {
        AddKeywordDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { pattern, action, packageName ->
                viewModel.addRule(pattern, action, packageName)
                showAddDialog = false
            }
        )
    }
}

/**
 * Cupertino Inset Grouped Table Row for Keyword Rule.
 */
@Composable
fun CupertinoKeywordRuleRow(
    rule: KeywordRuleEntity,
    onDelete: () -> Unit
) {
    val isBlock = rule.getRuleAction() == RuleAction.BLOCK
    val badgeColor = if (isBlock) AppleRed else AppleGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon squircle (32dp, 8dp radius)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isBlock) Icons.Default.Block else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Pattern and Scope Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.pattern,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppleTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (rule.isGlobal) Icons.Default.Public else Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = AppleTextTertiary,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (rule.isGlobal) "Global (All Apps)" else "${rule.packageName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleTextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Action Badge (BLOCK / ALLOW)
        CupertinoBadge(
            text = if (isBlock) "BLOCK" else "ALLOW",
            color = badgeColor
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Delete Action
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete Rule",
                tint = AppleTextTertiary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/**
 * Cupertino Card for On-Device AI Candidate Suggestions.
 */
@Composable
fun CupertinoAiSuggestedRuleCard(
    suggestion: AiSuggestedRuleEntity,
    onVerifyAndAdd: (asGlobal: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppleCard)
            .border(width = 0.5.dp, color = AppleHairline, shape = RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header: Category badge & App name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                CupertinoBadge(
                    text = suggestion.category,
                    color = ApplePurple,
                    icon = Icons.Default.AutoAwesome
                )

                Text(
                    text = suggestion.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleTextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Suggested Keyword
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Candidate: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTextSecondary
                )
                Text(
                    text = "\"${suggestion.suggestedKeyword}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppleBlue
                )
            }

            // Notification Sample Snippet Context
            if (!suggestion.sampleTitle.isNullOrBlank() || !suggestion.sampleText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleCardSecondary)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (!suggestion.sampleTitle.isNullOrBlank()) {
                            Text(
                                text = suggestion.sampleTitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!suggestion.sampleText.isNullOrBlank()) {
                            Text(
                                text = suggestion.sampleText,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = AppleTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verify & Add Rule Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleGreen.copy(alpha = 0.15f))
                        .clickable { onVerifyAndAdd(true) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AppleGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Accept Rule",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleGreen
                        )
                    }
                }

                // Dismiss Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleCardSecondary)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppleTextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Cupertino Empty State for AI Suggestions.
 */
@Composable
fun CupertinoEmptyAiSuggestionsView() {
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
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ApplePurple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ApplePurple,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Pending AI Suggestions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppleTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "When promotional notifications slip through standard keywords, Sieve's on-device neural classifier blocks them and extracts suggested rules here for verification.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppleTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
