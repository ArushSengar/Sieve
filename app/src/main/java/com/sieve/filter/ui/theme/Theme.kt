package com.sieve.filter.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sieve.filter.data.local.AccentTheme
import com.sieve.filter.data.local.AppThemeMode

data class CustomThemeTokens(
    val cardBackground: Color,
    val cardCell: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentGlow: Color,
    val isDark: Boolean
)

val LocalCustomThemeTokens = staticCompositionLocalOf {
    CustomThemeTokens(
        cardBackground = DarkCardBg,
        cardCell = DarkCardCell,
        cardBorder = DarkCardBorder,
        textPrimary = DarkInkPrimary,
        textSecondary = DarkInkSecondary,
        textTertiary = DarkInkTertiary,
        accentGlow = EmeraldGlow,
        isDark = true
    )
}

val MaterialTheme.customTokens: CustomThemeTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomThemeTokens.current

fun buildLightColorScheme(accent: AccentTheme): ColorScheme {
    val primary = Color(accent.primaryColor)
    return lightColorScheme(
        primary = primary,
        secondary = primary,
        tertiary = Color(accent.glowColor),
        background = LightBg,
        surface = LightCardBg,
        surfaceVariant = LightCardCell,
        onBackground = LightInkPrimary,
        onSurface = LightInkPrimary,
        onSurfaceVariant = LightInkSecondary,
        outline = LightCardBorder,
        outlineVariant = Color(0xFFCBD5E1)
    )
}

fun buildDarkColorScheme(accent: AccentTheme): ColorScheme {
    val primary = Color(accent.primaryColor)
    return darkColorScheme(
        primary = primary,
        secondary = primary,
        tertiary = Color(accent.glowColor),
        background = DarkBg,
        surface = DarkCardBg,
        surfaceVariant = DarkCardCell,
        onBackground = DarkInkPrimary,
        onSurface = DarkInkPrimary,
        onSurfaceVariant = DarkInkSecondary,
        outline = DarkCardBorder,
        outlineVariant = Color(0xFF1E293B)
    )
}

fun buildAmoledColorScheme(accent: AccentTheme): ColorScheme {
    val primary = Color(accent.primaryColor)
    return darkColorScheme(
        primary = primary,
        secondary = primary,
        tertiary = Color(accent.glowColor),
        background = AmoledBg,
        surface = AmoledCardBg,
        surfaceVariant = AmoledCardCell,
        onBackground = AmoledInkPrimary,
        onSurface = AmoledInkPrimary,
        onSurfaceVariant = AmoledInkSecondary,
        outline = AmoledCardBorder,
        outlineVariant = Color(0xFF262626)
    )
}

@Composable
fun SieveTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentTheme: AccentTheme = AccentTheme.EMERALD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val effectiveMode = when {
        isAmoled && themeMode == AppThemeMode.SYSTEM -> AppThemeMode.AMOLED
        else -> themeMode
    }

    val isDark = when (effectiveMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.AMOLED -> true
        AppThemeMode.SYSTEM -> darkTheme
    }

    val colorScheme = when {
        effectiveMode == AppThemeMode.LIGHT -> buildLightColorScheme(accentTheme)
        effectiveMode == AppThemeMode.AMOLED -> buildAmoledColorScheme(accentTheme)
        effectiveMode == AppThemeMode.DARK -> buildDarkColorScheme(accentTheme)
        isDark -> buildDarkColorScheme(accentTheme)
        else -> buildLightColorScheme(accentTheme)
    }

    val customTokens = when {
        !isDark -> CustomThemeTokens(
            cardBackground = LightCardBg,
            cardCell = LightCardCell,
            cardBorder = LightCardBorder,
            textPrimary = LightInkPrimary,
            textSecondary = LightInkSecondary,
            textTertiary = LightInkTertiary,
            accentGlow = Color(accentTheme.glowColor),
            isDark = false
        )
        effectiveMode == AppThemeMode.AMOLED -> CustomThemeTokens(
            cardBackground = AmoledCardBg,
            cardCell = AmoledCardCell,
            cardBorder = AmoledCardBorder,
            textPrimary = AmoledInkPrimary,
            textSecondary = AmoledInkSecondary,
            textTertiary = AmoledInkTertiary,
            accentGlow = Color(accentTheme.glowColor),
            isDark = true
        )
        else -> CustomThemeTokens(
            cardBackground = DarkCardBg,
            cardCell = DarkCardCell,
            cardBorder = DarkCardBorder,
            textPrimary = DarkInkPrimary,
            textSecondary = DarkInkSecondary,
            textTertiary = DarkInkTertiary,
            accentGlow = Color(accentTheme.glowColor),
            isDark = true
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalCustomThemeTokens provides customTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
