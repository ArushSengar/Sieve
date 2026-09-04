package com.sieve.filter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sieve.filter.model.RuleAction
import com.sieve.filter.ui.theme.AllowGreen
import com.sieve.filter.ui.theme.AllowGreenBg
import com.sieve.filter.ui.theme.BlockRed
import com.sieve.filter.ui.theme.BlockRedBg

@Composable
fun AddKeywordDialog(
    initialPattern: String = "",
    initialPackageName: String? = null,
    initialAction: RuleAction = RuleAction.BLOCK,
    onDismiss: () -> Unit,
    onConfirm: (pattern: String, action: RuleAction, packageName: String?) -> Unit
) {
    var pattern by remember { mutableStateOf(initialPattern) }
    var action by remember { mutableStateOf(initialAction) }
    var packageName by remember { mutableStateOf(initialPackageName ?: "") }
    var isPackageScoped by remember { mutableStateOf(!initialPackageName.isNullOrBlank()) }
    var testText by remember { mutableStateOf("") }

    val examplePatterns = listOf(
        "% off" to RuleAction.BLOCK,
        "cashback" to RuleAction.BLOCK,
        "flash sale" to RuleAction.BLOCK,
        "exclusive offer" to RuleAction.BLOCK,
        "coins|spins|bonus" to RuleAction.BLOCK,
        "otp" to RuleAction.ALLOW,
        "delivered" to RuleAction.ALLOW,
        "order confirmed" to RuleAction.ALLOW
    )

    val testMatchResult = remember(pattern, testText, action) {
        if (pattern.isBlank() || testText.isBlank()) null
        else {
            try {
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                if (regex.containsMatchIn(testText)) action else null
            } catch (_: Exception) {
                if (testText.contains(pattern, ignoreCase = true)) action else null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Keyword Rule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Keyword or Regex Pattern") },
                    placeholder = { Text("e.g. 50% off, cashback, flash sale") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Pattern Hint Chips
                Column {
                    Text(
                        text = "Quick Suggestions (tap to fill)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        examplePatterns.forEach { (hint, hintAction) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.clickable {
                                    pattern = hint
                                    action = hintAction
                                }
                            ) {
                                Text(
                                    text = hint,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Live Pattern Tester
                Column {
                    OutlinedTextField(
                        value = testText,
                        onValueChange = { testText = it },
                        label = { Text("Live Rule Tester") },
                        placeholder = { Text("Type sample notification text...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (testText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        if (testMatchResult != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (testMatchResult == RuleAction.BLOCK) BlockRedBg else AllowGreenBg
                            ) {
                                Text(
                                    text = "✓ MATCHED → $testMatchResult",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (testMatchResult == RuleAction.BLOCK) BlockRed else AllowGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "✗ NO MATCH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Action",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // BLOCK chip/option
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (action == RuleAction.BLOCK) BlockRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { action = RuleAction.BLOCK }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = if (action == RuleAction.BLOCK) BlockRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = "BLOCK",
                                color = if (action == RuleAction.BLOCK) BlockRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // ALLOW chip/option
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (action == RuleAction.ALLOW) AllowGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { action = RuleAction.ALLOW }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (action == RuleAction.ALLOW) AllowGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = "ALLOW",
                                color = if (action == RuleAction.ALLOW) AllowGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Scope Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !isPackageScoped,
                        onClick = { isPackageScoped = false }
                    )
                    Text(
                        text = "Global (All Apps)",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { isPackageScoped = false }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = isPackageScoped,
                        onClick = { isPackageScoped = true }
                    )
                    Text(
                        text = "Scoped to App",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { isPackageScoped = true }
                    )
                }

                if (isPackageScoped) {
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = { packageName = it },
                        label = { Text("Package Name") },
                        placeholder = { Text("com.application.zomato") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pattern.isNotBlank()) {
                        val pkg = if (isPackageScoped && packageName.isNotBlank()) packageName.trim() else null
                        onConfirm(pattern.trim(), action, pkg)
                    }
                },
                enabled = pattern.isNotBlank()
            ) {
                Text("Add Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
