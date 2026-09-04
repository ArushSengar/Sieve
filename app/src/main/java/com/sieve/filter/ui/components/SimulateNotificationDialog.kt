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
        label = "Zomato: 50% Promo (Spam)",
        packageName = "in.org.projecteka.zomato",
        title = "Hungry? 🍕 Flat 50% OFF",
        text = "Use code PIZZA50 to get 50% off + ₹100 cashback on your lunch!",
        channelId = "offers_and_promotions"
    ),
    NotificationPreset(
        label = "Zomato: Order Delivered (Keep)",
        packageName = "in.org.projecteka.zomato",
        title = "Order Delivered!",
        text = "Your order from Burger King has been delivered. Enjoy your meal!",
        channelId = "order_updates"
    ),
    NotificationPreset(
        label = "HDFC Bank: OTP (Keep)",
        packageName = "com.hdfc.bank",
        title = "Transaction OTP",
        text = "Your OTP is 492810 for debit card txn of INR 1,200. Do not share.",
        channelId = "transaction_alerts"
    ),
    NotificationPreset(
        label = "Swiggy: Flash Sale (Spam)",
        packageName = "in.swiggy.android",
        title = "⚡ Flash Sale Ends in 1 Hour!",
        text = "Limited time offer: Buy 1 Get 1 Free on top biryanis in your city.",
        channelId = "promotional_deals"
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
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (decision.shouldDismiss) BlockRedBg else AllowGreenBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (decision.shouldDismiss) Icons.Default.Block else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (decision.shouldDismiss) BlockRed else AllowGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (decision.shouldDismiss) "DECISION: DISMISS (SPAM)" else "DECISION: KEEP (IMPORTANT)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (decision.shouldDismiss) BlockRed else AllowGreen,
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

                        decisionResult = decision

                        // If blocked, log to database so user can see it appear in real-time!
                        if (decision.shouldDismiss) {
                            repository.logBlockedNotification(
                                packageName = packageName,
                                title = title,
                                textSnippet = text,
                                channelId = channelId,
                                matchedRule = decision.matchedRule
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
