package com.sieve.filter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sieve.filter.SieveApplication
import com.sieve.filter.model.AppRuleMode
import com.sieve.filter.model.FilterDecision
import com.sieve.filter.service.NotificationClassifier
import com.sieve.filter.service.SieveNotificationListenerService
import com.sieve.filter.service.SmartAiClassifier
import com.sieve.filter.ui.theme.AppleBlue
import com.sieve.filter.ui.theme.AppleCardElevated
import com.sieve.filter.ui.theme.AppleCardSecondary
import com.sieve.filter.ui.theme.AppleGreen
import com.sieve.filter.ui.theme.AppleHairline
import com.sieve.filter.ui.theme.ApplePurple
import com.sieve.filter.ui.theme.AppleRed
import com.sieve.filter.ui.theme.AppleTextPrimary
import com.sieve.filter.ui.theme.AppleTextSecondary
import com.sieve.filter.ui.theme.AppleTextTertiary
import kotlinx.coroutines.launch

data class NotificationPreset(
    val label: String,
    val packageName: String,
    val title: String,
    val text: String,
    val channelId: String,
    val isSpamExpected: Boolean
)

val PRESETS = listOf(
    NotificationPreset(
        label = "PhonePe: Paid You (Safe)",
        packageName = "com.phonepe.app",
        title = "Payment Received",
        text = "Rahul Sharma paid you ₹1,500 via PhonePe UPI. Ref: UPI/938120.",
        channelId = "transactions",
        isSpamExpected = false
    ),
    NotificationPreset(
        label = "HDFC Bank: OTP (Safe)",
        packageName = "com.hdfc.bank",
        title = "Transaction OTP",
        text = "Your OTP is 492810 for debit card txn of INR 1,200. Do not share.",
        channelId = "transaction_alerts",
        isSpamExpected = false
    ),
    NotificationPreset(
        label = "WhatsApp: Message (Safe)",
        packageName = "com.whatsapp",
        title = "Mom",
        text = "Reached home safely? Call me when free.",
        channelId = "messages",
        isSpamExpected = false
    ),
    NotificationPreset(
        label = "Zomato: Delivered (Safe)",
        packageName = "com.application.zomato",
        title = "Order Delivered!",
        text = "Your order from Burger King has been delivered. Enjoy your meal!",
        channelId = "order_updates",
        isSpamExpected = false
    ),
    NotificationPreset(
        label = "Uber: Driver Arriving (Safe)",
        packageName = "com.ubercab",
        title = "Driver Arriving",
        text = "Your driver Ramesh in Swift Dzire (DL 1Y 9821) is arriving in 2 mins.",
        channelId = "trip_status",
        isSpamExpected = false
    ),
    NotificationPreset(
        label = "Flipkart: Flash Sale (Spam)",
        packageName = "com.flipkart.android",
        title = "⚡ Mega Flash Sale Live!",
        text = "Flat 80% off on premium smartphones & electronics. Hurry, limited time offer!",
        channelId = "promotions",
        isSpamExpected = true
    ),
    NotificationPreset(
        label = "super.money: Win iPhone 17 (Spam)",
        packageName = "money.super.payments",
        title = "Win an iPhone 17! 🤩 📱",
        text = "Just apply for your superCard & become our top spender to win. Tap to apply now! 🚀",
        channelId = "moe_default_channel",
        isSpamExpected = true
    ),
    NotificationPreset(
        label = "Dream11: Win ₹10 Lakhs (Spam)",
        packageName = "com.dream11.app",
        title = "🏏 Mega Contest Live! Win ₹10 Lakhs",
        text = "Make your team now for IND vs AUS. Claim free ₹50 bonus cash!",
        channelId = "marketing",
        isSpamExpected = true
    ),
    NotificationPreset(
        label = "MoneyView: Instant Loan (Spam)",
        packageName = "com.whizdm.moneyview",
        title = "Pre-approved Loan of ₹5,00,000!",
        text = "Zero paperwork, instant disbursal in 2 mins. Tap to claim before expiry.",
        channelId = "loan_promos",
        isSpamExpected = true
    ),
    NotificationPreset(
        label = "Navi: Cashback Bait (Spam)",
        packageName = "com.naviapp",
        title = "Rs. 12.00 🎉",
        text = "Congratulations! Get 𝗰𝗮𝘀𝗵𝗯𝗮𝗰𝗸 on your prepaid recharge.",
        channelId = "navi.channel",
        isSpamExpected = true
    )
)

@Composable
fun SimulateNotificationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SieveApplication).repository
    val scope = rememberCoroutineScope()

    var packageName by remember { mutableStateOf(PRESETS[0].packageName) }
    var title by remember { mutableStateOf(PRESETS[0].title) }
    var text by remember { mutableStateOf(PRESETS[0].text) }
    var channelId by remember { mutableStateOf(PRESETS[0].channelId) }
    var decisionResult by remember { mutableStateOf<FilterDecision?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppleCardElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Column {
                Text(
                    text = "Simulate & Test Sentinel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
                Text(
                    text = "Test notifications against live filters and AI heuristics",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PRESET SCENARIOS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextTertiary,
                    letterSpacing = 0.5.sp
                )

                // Horizontal Preset Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESETS.forEach { preset ->
                        val isSelected = packageName == preset.packageName && title == preset.title
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) AppleBlue.copy(alpha = 0.2f)
                                    else AppleCardSecondary
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (isSelected) AppleBlue else AppleHairline,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    packageName = preset.packageName
                                    title = preset.title
                                    text = preset.text
                                    channelId = preset.channelId
                                    decisionResult = null
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AppleBlue else AppleTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // App Package Field
                CupertinoSimulationInputField(
                    label = "PACKAGE NAME",
                    value = packageName,
                    onValueChange = { packageName = it; decisionResult = null }
                )

                // Title Field
                CupertinoSimulationInputField(
                    label = "NOTIFICATION TITLE",
                    value = title,
                    onValueChange = { title = it; decisionResult = null }
                )

                // Text Field
                CupertinoSimulationInputField(
                    label = "CONTENT MESSAGE",
                    value = text,
                    onValueChange = { text = it; decisionResult = null },
                    minLines = 2
                )

                // Channel Field
                CupertinoSimulationInputField(
                    label = "CHANNEL ID",
                    value = channelId,
                    onValueChange = { channelId = it; decisionResult = null }
                )

                // Decision Result Display
                AnimatedVisibility(
                    visible = decisionResult != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    decisionResult?.let { decision ->
                        val isAi = decision.matchedRule.startsWith("AI:")
                        val isBlocked = decision.shouldDismiss
                        val cardBg = when {
                            isAi -> ApplePurple.copy(alpha = 0.15f)
                            isBlocked -> AppleRed.copy(alpha = 0.15f)
                            else -> AppleGreen.copy(alpha = 0.15f)
                        }
                        val borderColor = when {
                            isAi -> ApplePurple.copy(alpha = 0.4f)
                            isBlocked -> AppleRed.copy(alpha = 0.4f)
                            else -> AppleGreen.copy(alpha = 0.4f)
                        }
                        val accentColor = when {
                            isAi -> ApplePurple
                            isBlocked -> AppleRed
                            else -> AppleGreen
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardBg)
                                .border(0.5.dp, borderColor, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when {
                                        isAi -> Icons.Default.AutoAwesome
                                        isBlocked -> Icons.Default.Block
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isBlocked) "DISMISS (SPAM DETECTED)" else "KEEP (ALLOWED / SAFE)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    Text(
                                        text = "Rule: ${decision.matchedRule}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppleTextPrimary
                                    )
                                    if (decision.reason.isNotBlank()) {
                                        Text(
                                            text = "Reason: ${decision.reason}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppleTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppleBlue)
                    .clickable {
                        scope.launch {
                            val prefs = (context.applicationContext as SieveApplication).preferencesManager

                            // 1. Check Protected Communication Packages
                            if (SieveNotificationListenerService.PROTECTED_COMMUNICATION_PACKAGES.contains(packageName)) {
                                decisionResult = FilterDecision.allow("Protected App (${packageName.substringAfterLast('.')})")
                                return@launch
                            }

                            // 2. Check Guaranteed Safe Financial & Transaction Content
                            if (SmartAiClassifier.isGuaranteedSafe(title, text, null)) {
                                decisionResult = FilterDecision.allow("Safe Financial/Transaction Content")
                                return@launch
                            }

                            val appRule = repository.getAppRuleSync(packageName)
                            val appMode = appRule?.getAppRuleMode() ?: AppRuleMode.AUTO
                            val rules = repository.getRulesForPackageSync(packageName)

                            val payload = NotificationClassifier.NotificationPayload(
                                packageName = packageName,
                                title = title,
                                text = text,
                                channelId = channelId
                            )

                            val decision = NotificationClassifier.classify(
                                payload = payload,
                                appRuleMode = appMode,
                                rules = rules
                            )

                            var finalDecision = decision
                            if (decision.isPassThrough && prefs.isAiFilterEnabled.value) {
                                val aiResult = SmartAiClassifier.classify(payload)
                                if (aiResult.isSpam) {
                                    val aiRule = "AI: ${aiResult.category} (${aiResult.primaryKeyword})"
                                    finalDecision = FilterDecision.block(
                                        matchedRule = aiRule,
                                        reason = aiResult.reason
                                    )
                                    repository.recordAiSuggestion(
                                        packageName = packageName,
                                        suggestedKeyword = aiResult.primaryKeyword,
                                        category = aiResult.category,
                                        sampleTitle = title,
                                        sampleText = text
                                    )
                                }
                            }

                            decisionResult = finalDecision

                            // If blocked, log to database so user can see it appear in real-time
                            if (finalDecision.shouldDismiss) {
                                repository.logBlockedNotification(
                                    packageName = packageName,
                                    title = title,
                                    textSnippet = text,
                                    channelId = channelId,
                                    matchedRule = finalDecision.matchedRule
                                )
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Run Test",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
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
                    text = "Close",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppleTextSecondary
                )
            }
        }
    )
}

@Composable
private fun CupertinoSimulationInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1
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
                minLines = minLines
            )
        }
    }
}
