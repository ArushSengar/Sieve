package com.sieve.filter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sieve.filter.SieveApplication
import com.sieve.filter.service.SieveNotificationListenerService
import com.sieve.filter.ui.navigation.Screen
import com.sieve.filter.ui.screens.AppRulesScreen
import com.sieve.filter.ui.screens.BlockLogScreen
import com.sieve.filter.ui.screens.KeywordRulesScreen
import com.sieve.filter.ui.screens.SettingsScreen
import com.sieve.filter.ui.screens.StatsScreen
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import com.sieve.filter.ui.theme.AllowGreen
import com.sieve.filter.ui.theme.AllowGreenBg
import com.sieve.filter.ui.theme.AppleBackground
import com.sieve.filter.ui.theme.AppleFrostedGlass
import com.sieve.filter.ui.theme.AppleGreen
import com.sieve.filter.ui.theme.AppleGreenGlow
import com.sieve.filter.ui.theme.AppleHairline
import com.sieve.filter.ui.theme.AppleTextPrimary
import com.sieve.filter.ui.theme.AppleTextSecondary
import com.sieve.filter.ui.theme.AppleTextTertiary
import com.sieve.filter.ui.theme.AutoBlue
import com.sieve.filter.ui.theme.AutoBlueBg
import com.sieve.filter.ui.theme.BlockRed
import com.sieve.filter.ui.theme.BlockRedBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SieveApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val isListening by SieveNotificationListenerService.isListening.collectAsState()
    val isFilterEnabled by SieveApplication.instance.preferencesManager.isFilterEnabled.collectAsState()
    val isQuietHours by remember {
        derivedStateOf { SieveApplication.instance.preferencesManager.isQuietHoursActive() }
    }

    val (statusText, statusBg, statusColor) = remember(isListening, isFilterEnabled, isQuietHours) {
        val hasPerm = SieveNotificationListenerService.isPermissionGranted(context)
        when {
            !hasPerm -> Triple("Setup", Color(0xFFEF4444).copy(alpha = 0.14f), Color(0xFFEF4444))
            !isFilterEnabled -> Triple("Paused", Color(0xFF94A3B8).copy(alpha = 0.15f), Color(0xFF64748B))
            isQuietHours -> Triple("Quiet", Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF8B5CF6))
            isListening -> Triple("Active", Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981))
            else -> Triple("Standby", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B))
        }
    }

    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Date caption
                val dateStr = remember {
                    java.text.SimpleDateFormat("EEEE, MMM d", java.util.Locale.getDefault())
                        .format(java.util.Date())
                        .uppercase()
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sieve",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp),
                            color = colors.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Live Shield Status Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(statusBg)
                            .border(width = 0.5.dp, color = statusColor.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp))
                            .clickable {
                                if (currentRoute != Screen.Settings.route) {
                                    navController.navigate(Screen.Settings.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Adaptive Frosted Glass Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface.copy(alpha = 0.95f))
                    .border(width = 0.5.dp, color = colors.outline.copy(alpha = 0.3f), shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    Screen.items.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                unselectedIconColor = colors.onSurfaceVariant,
                                unselectedTextColor = colors.onSurfaceVariant,
                                indicatorColor = colors.primary.copy(alpha = 0.16f)
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.BlockLog.route
            ) {
                composable(Screen.BlockLog.route) {
                    BlockLogScreen()
                }
                composable(Screen.AppRules.route) {
                    AppRulesScreen()
                }
                composable(Screen.KeywordRules.route) {
                    KeywordRulesScreen()
                }
                composable(Screen.Stats.route) {
                    StatsScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(snackbarHostState = snackbarHostState)
                }
            }
        }
    }
}
