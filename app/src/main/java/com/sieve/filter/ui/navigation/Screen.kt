package com.sieve.filter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for Sieve bottom navigation.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object BlockLog : Screen("block_log", "Block Log", Icons.Default.Shield)
    object AppRules : Screen("app_rules", "App Rules", Icons.Default.Apps)
    object KeywordRules : Screen("keyword_rules", "Keywords", Icons.Default.FilterAlt)

    companion object {
        val items = listOf(BlockLog, AppRules, KeywordRules)
    }
}
