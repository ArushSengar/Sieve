package com.sieve.filter.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sieve.filter.data.local.entity.AiSuggestedRuleEntity
import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.model.RuleAction
import com.sieve.filter.ui.components.AddKeywordDialog
import com.sieve.filter.ui.theme.AiPurple
import com.sieve.filter.ui.theme.AiPurpleBg
import com.sieve.filter.ui.theme.AllowGreen
import com.sieve.filter.ui.theme.AllowGreenBg
import com.sieve.filter.ui.theme.BlockRed
import com.sieve.filter.ui.theme.BlockRedBg
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

    Scaffold(
        floatingActionButton = {
            if (selectedTab != KeywordFilterTab.AI_SUGGESTIONS) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Keyword Rule")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Scrollable Tabs: All | Block List | Allow List | AI Suggestions (Count)
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp
            ) {
                KeywordFilterTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (tab) {
                                        KeywordFilterTab.ALL -> "All"
                                        KeywordFilterTab.BLOCK -> "Block List"
                                        KeywordFilterTab.ALLOW -> "Allow List"
                                        KeywordFilterTab.AI_SUGGESTIONS -> "AI Suggestions"
                                    },
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )

                                if (tab == KeywordFilterTab.AI_SUGGESTIONS && pendingCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = AiPurple,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (pendingCount > 99) "99+" else "$pendingCount",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // AI Pending Banner (if pending suggestions exist and user is viewing other tabs)
            if (selectedTab != KeywordFilterTab.AI_SUGGESTIONS && pendingCount > 0) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AiPurpleBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { viewModel.onTabSelected(KeywordFilterTab.AI_SUGGESTIONS) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AiPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$pendingCount new AI spam detection${if (pendingCount > 1) "s" else ""}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap to verify and add candidate keywords to rules",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "Review →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AiPurple
                        )
                    }
                }
            }

            // Search Bar & Reset Defaults Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text(
                            if (selectedTab == KeywordFilterTab.AI_SUGGESTIONS) "Filter AI suggestions..."
                            else "Filter keywords..."
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                if (selectedTab != KeywordFilterTab.AI_SUGGESTIONS) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = viewModel::resetToDefaults
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset to Default Rules",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content Area
            if (selectedTab == KeywordFilterTab.AI_SUGGESTIONS) {
                // AI Suggestions List
                if (pendingSuggestions.isEmpty()) {
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
                                color = AiPurpleBg,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = AiPurple,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Pending AI Suggestions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "When promotional notifications slip through standard keywords, Sieve's on-device AI classifier automatically blocks them and extracts keyword suggestions here for verification.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = pendingSuggestions,
                            key = { it.id }
                        ) { suggestion ->
                            AiSuggestedRuleCard(
                                suggestion = suggestion,
                                onVerifyAndAdd = { asGlobal ->
                                    viewModel.acceptAiSuggestion(suggestion.id, asGlobal)
                                },
                                onDismiss = { viewModel.dismissAiSuggestion(suggestion.id) }
                            )
                        }
                    }
                }
            } else {
                // Standard Keyword Rules List
                if (rules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No keyword rules found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = rules,
                            key = { it.id }
                        ) { rule ->
                            KeywordRuleCard(
                                rule = rule,
                                onDelete = { viewModel.deleteRule(rule.id) }
                            )
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

@Composable
fun AiSuggestedRuleCard(
    suggestion: AiSuggestedRuleEntity,
    onVerifyAndAdd: (asGlobal: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category badge & App name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AiPurpleBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AiPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = suggestion.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = AiPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = suggestion.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Candidate Keyword Highlight
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Suggested Keyword: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = suggestion.suggestedKeyword,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Notification snippet context
            if (!suggestion.sampleTitle.isNullOrBlank() || !suggestion.sampleText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (!suggestion.sampleTitle.isNullOrBlank()) {
                            Text(
                                text = "\"${suggestion.sampleTitle}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!suggestion.sampleText.isNullOrBlank()) {
                            Text(
                                text = suggestion.sampleText,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: One-tap Verify & Add Rule, and Dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onVerifyAndAdd(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Verify & Add Rule", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dismiss", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun KeywordRuleCard(
    rule: KeywordRuleEntity,
    onDelete: () -> Unit
) {
    val isBlock = rule.getRuleAction() == RuleAction.BLOCK

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action Icon badge
            Surface(
                shape = CircleShape,
                color = if (isBlock) BlockRedBg else AllowGreenBg,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isBlock) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isBlock) BlockRed else AllowGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Keyword pattern + Scope
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.pattern,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (rule.isGlobal) Icons.Default.Public else Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (rule.isGlobal) "Global (All Apps)" else "App: ${rule.packageName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isBlock) BlockRedBg else AllowGreenBg
            ) {
                Text(
                    text = if (isBlock) "BLOCK" else "ALLOW",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isBlock) BlockRed else AllowGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Delete Action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Rule",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
