package com.sieve.filter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sieve.filter.model.RuleAction
import com.sieve.filter.ui.theme.AppleBlue
import com.sieve.filter.ui.theme.AppleCardElevated
import com.sieve.filter.ui.theme.AppleCardSecondary
import com.sieve.filter.ui.theme.AppleGreen
import com.sieve.filter.ui.theme.AppleHairline
import com.sieve.filter.ui.theme.AppleRed
import com.sieve.filter.ui.theme.AppleTextPrimary
import com.sieve.filter.ui.theme.AppleTextSecondary
import com.sieve.filter.ui.theme.AppleTextTertiary

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
        containerColor = AppleCardElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Column {
                Text(
                    text = "Add Keyword Rule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
                Text(
                    text = "Filter notifications by matching phrases or regular expressions",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Keyword / Regex Input
                CupertinoDialogInputField(
                    label = "KEYWORD OR REGEX PATTERN",
                    placeholder = "e.g. 50% off, cashback, flash sale",
                    value = pattern,
                    onValueChange = { pattern = it }
                )

                // Quick Pattern Hint Chips
                Column {
                    Text(
                        text = "QUICK PRESETS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextTertiary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        examplePatterns.forEach { (hint, hintAction) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppleCardSecondary)
                                    .border(0.5.dp, AppleHairline, RoundedCornerShape(8.dp))
                                    .clickable {
                                        pattern = hint
                                        action = hintAction
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = hint,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppleTextSecondary
                                )
                            }
                        }
                    }
                }

                // Live Pattern Tester
                Column {
                    CupertinoDialogInputField(
                        label = "LIVE RULE TESTER",
                        placeholder = "Type sample notification text...",
                        value = testText,
                        onValueChange = { testText = it }
                    )
                    if (testText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        if (testMatchResult != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (testMatchResult == RuleAction.BLOCK) AppleRed.copy(alpha = 0.15f)
                                        else AppleGreen.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "✓ MATCHED → $testMatchResult",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (testMatchResult == RuleAction.BLOCK) AppleRed else AppleGreen
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppleCardSecondary)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "✗ NO MATCH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppleTextTertiary
                                )
                            }
                        }
                    }
                }

                // Action Selector (BLOCK vs ALLOW)
                Column {
                    Text(
                        text = "RULE ACTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextTertiary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // BLOCK Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (action == RuleAction.BLOCK) AppleRed.copy(alpha = 0.2f)
                                    else AppleCardSecondary
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (action == RuleAction.BLOCK) AppleRed else AppleHairline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { action = RuleAction.BLOCK }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = if (action == RuleAction.BLOCK) AppleRed else AppleTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BLOCK",
                                    fontWeight = FontWeight.Bold,
                                    color = if (action == RuleAction.BLOCK) AppleRed else AppleTextSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        // ALLOW Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (action == RuleAction.ALLOW) AppleGreen.copy(alpha = 0.2f)
                                    else AppleCardSecondary
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (action == RuleAction.ALLOW) AppleGreen else AppleHairline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { action = RuleAction.ALLOW }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (action == RuleAction.ALLOW) AppleGreen else AppleTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ALLOW",
                                    fontWeight = FontWeight.Bold,
                                    color = if (action == RuleAction.ALLOW) AppleGreen else AppleTextSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                // Scope Selector
                Column {
                    Text(
                        text = "APPLY TO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextTertiary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (!isPackageScoped) AppleBlue.copy(alpha = 0.2f)
                                    else AppleCardSecondary
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (!isPackageScoped) AppleBlue else AppleHairline,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { isPackageScoped = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Global (All Apps)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (!isPackageScoped) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isPackageScoped) AppleBlue else AppleTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isPackageScoped) AppleBlue.copy(alpha = 0.2f)
                                    else AppleCardSecondary
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (isPackageScoped) AppleBlue else AppleHairline,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { isPackageScoped = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Scoped to App",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isPackageScoped) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPackageScoped) AppleBlue else AppleTextSecondary
                            )
                        }
                    }
                }

                if (isPackageScoped) {
                    CupertinoDialogInputField(
                        label = "PACKAGE NAME",
                        placeholder = "e.g. com.application.zomato",
                        value = packageName,
                        onValueChange = { packageName = it }
                    )
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (pattern.isNotBlank()) AppleBlue else AppleBlue.copy(alpha = 0.4f))
                    .clickable(enabled = pattern.isNotBlank()) {
                        val pkg = if (isPackageScoped && packageName.isNotBlank()) packageName.trim() else null
                        onConfirm(pattern.trim(), action, pkg)
                    }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "Add Rule",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppleTextSecondary
                )
            }
        }
    )
}

@Composable
private fun CupertinoDialogInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppleTextTertiary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AppleCardSecondary)
                .border(0.5.dp, AppleHairline, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = AppleTextTertiary,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = AppleTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(AppleBlue),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
