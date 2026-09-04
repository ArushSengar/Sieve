package com.sieve.filter.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sieve.filter.SieveApplication
import com.sieve.filter.model.AppRuleMode
import com.sieve.filter.model.FilterDecision
import com.sieve.filter.service.NotificationClassifier
import com.sieve.filter.service.SmartAiClassifier
import com.sieve.filter.ui.theme.AiPurple
import com.sieve.filter.ui.theme.AiPurpleBg
import com.sieve.filter.ui.theme.AllowGreen
import com.sieve.filter.ui.theme.AllowGreenBg
import com.sieve.filter.ui.theme.BlockRed
import com.sieve.filter.ui.theme.BlockRedBg
import kotlinx.coroutines.launch

data class NotificationPreset(
    val label: String,
    val packageName: String,
    val title: String,
    val text: String,
    val channelId: String
)

val PRESETS = listOf(
    NotificationPreset(
        label = "super.money: Win iPhone 17 (AI Spam)",
        packageName = "money.super.payments",
        title = "Win an iPhone 17! 🤩 📱",
        text = "Just apply for your superCard & become our top spender to win. Tap to apply now! 🚀",
        channelId = "moe_default_channel"
    ),
    NotificationPreset(
        label = "Navi: Rs. 12 Cashback (AI Spam)",
        packageName = "com.naviapp",
        title = "Rs. 12.00 🎉",
        text = "Congratulations! Get 𝗰𝗮𝘀𝗵𝗯𝗮𝗰𝗸 on your prepaid recharge.",
        channelId = "navi.channel"
    ),
    NotificationPreset(
        label = "Jar: Save ₹10 Target (AI Spam)",
        packageName = "com.mysave.jar",
        title = "Save ₹10 to reach the target",
        text = "You are very close! Add ₹10 now.",
        channelId = "savings_nudges"
    ),
    NotificationPreset(
        label = "Truecaller: VIP Rewards (AI Spam)",
        packageName = "com.truecaller",
        title = "New profile views you missed this week 25 p...",
        text = "Introducing VIP Rewards 🎉 You're invited! Join now.",
        channelId = "engagement_push"
    ),
    NotificationPreset(
        label = "Bewakoof: Solid Joggers (AI Spam)",
        packageName = "com.bewakoof.bewakoof",
        title = "Solid Joggers, Plenty Of Colours",
        text = "Build your rotation one colour at a time 👀",
        channelId = "catalog_marketing"
    ),
    NotificationPreset(
        label = "YouTube: Win ₹1 CRORE (AI Spam)",
        packageName = "com.google.android.youtube",
        title = "Google Gemini Fund My Crazy: Complete 1-Min Task & Win ₹1 CRORE 💰",
        text = "DR abhishek.",
        channelId = "recommendations"
    ),
    NotificationPreset(
        label = "HDFC Bank: OTP (Keep)",
        packageName = "com.hdfc.bank",
        title = "Transaction OTP",
        text = "Your OTP is 492810 for debit card txn of INR 1,200. Do not share.",
        channelId = "transaction_alerts"
    ),
    NotificationPreset(
        label = "Zomato: Delivered (Keep)",
        packageName = "in.org.projecteka.zomato",
        title = "Order Delivered!",
        text = "Your order from Burger King has been delivered. Enjoy your meal!",
        channelId = "order_updates"
    ),
    NotificationPreset(
        label = "Uber: Ride Arriving (Keep)",
        packageName = "com.ubercab",
        title = "Driver Arriving",
        text = "Your driver Ramesh in Swift Dzire (DL 1Y 9821) is arriving in 2 mins.",
        channelId = "trip_status"
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
        title = {
            Text(
                text = "Simulate & Test Filter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PRESETS.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    packageName = preset.packageName
                                    title = preset.title
                                    text = preset.text
                                    channelId = preset.channelId
                                    decisionResult = null
                                }
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("App Package") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Notification Content") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = channelId,
                    onValueChange = { channelId = it },
                    label = { Text("Channel ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Decision result display
                if (decisionResult != null) {
                    val decision = decisionResult!!
                    val isAi = decision.matchedRule.startsWith("AI:")
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isAi -> AiPurpleBg
                                decision.shouldDismiss -> BlockRedBg
                                else -> AllowGreenBg
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    isAi -> Icons.Default.AutoAwesome
                                    decision.shouldDismiss -> Icons.Default.Block
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = when {
                                    isAi -> AiPurple
                                    decision.shouldDismiss -> BlockRed
                                    else -> AllowGreen
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (decision.shouldDismiss) "DECISION: DISMISS (SPAM)" else "DECISION: KEEP (IMPORTANT)",
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isAi -> AiPurple
                                        decision.shouldDismiss -> BlockRed
                                        else -> AllowGreen
                                    },
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Rule: ${decision.matchedRule}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val appRule = repository.getAppRuleSync(packageName)
                        val appMode = appRule?.getAppRuleMode() ?: AppRuleMode.AUTO
                        val rules = repository.getRulesForPackageSync(packageName)
                        val prefs = (context.applicationContext as SieveApplication).preferencesManager

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

                        // If blocked, log to database so user can see it appear in real-time!
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
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Test")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
