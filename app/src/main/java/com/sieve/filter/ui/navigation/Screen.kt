package com.sieve.filter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for Sieve bottom navigation.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object BlockLog : Screen("block_log", "Activity", Icons.Default.Shield)
    object AppRules : Screen("app_rules", "Apps", Icons.Default.Apps)
    object KeywordRules : Screen("keyword_rules", "Filters", Icons.Default.FilterAlt)
    object Stats : Screen("stats", "Insights", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val items = listOf(BlockLog, AppRules, KeywordRules, Stats, Settings)
    }
}
