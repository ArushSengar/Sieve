package com.sieve.filter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.sieve.filter.ui.theme.BlockRed

@Composable
fun AddKeywordDialog(
    onDismiss: () -> Unit,
    onConfirm: (pattern: String, action: RuleAction, packageName: String?) -> Unit
) {
    var pattern by remember { mutableStateOf("") }
    var action by remember { mutableStateOf(RuleAction.BLOCK) }
    var packageName by remember { mutableStateOf("") }
    var isPackageScoped by remember { mutableStateOf(false) }

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
                    label = { Text("Keyword or Phrase") },
                    placeholder = { Text("e.g. 50% off, cashback, flash sale") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
