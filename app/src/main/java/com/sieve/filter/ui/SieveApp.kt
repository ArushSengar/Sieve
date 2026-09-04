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
import com.sieve.filter.ui.theme.AllowGreen
import com.sieve.filter.ui.theme.AllowGreenBg
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
            !hasPerm -> Triple("Setup", BlockRedBg, BlockRed)
            !isFilterEnabled -> Triple("Paused", Color(0xFFEEEEEE), Color(0xFF757575))
            isQuietHours -> Triple("Quiet", AutoBlueBg, AutoBlue)
            isListening -> Triple("Active", AllowGreenBg, AllowGreen)
            else -> Triple("Standby", Color(0xFFFFE0B2), Color(0xFFE65100))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sieve",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "On-Device Notification Filter",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Live Status Chip in Top Bar
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusBg,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
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
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Screen.items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = isSelected,
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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
