package com.sieve.filter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// =============================================================================
// ACCENT THEMES (PRIMARY PALETTES)
// =============================================================================
// Emerald Sentinel (Default Mint/Emerald)
val EmeraldPrimary = Color(0xFF10B981)
val EmeraldPrimaryDark = Color(0xFF059669)
val EmeraldGlow = Color(0x3310B981)

// Cyber Cyan (Electric Cyan & Neon Sky)
val CyberCyanPrimary = Color(0xFF06B6D4)
val CyberCyanPrimaryDark = Color(0xFF0284C7)
val CyberCyanGlow = Color(0x3306B6D4)

// Royal Violet (Deep Violet & Lavender)
val RoyalVioletPrimary = Color(0xFF8B5CF6)
val RoyalVioletPrimaryDark = Color(0xFF7C3AED)
val RoyalVioletGlow = Color(0x338B5CF6)

// Sunset Amber (Warm Gold & Flame Orange)
val SunsetAmberPrimary = Color(0xFFF59E0B)
val SunsetAmberPrimaryDark = Color(0xFFD97706)
val SunsetAmberGlow = Color(0x33F59E0B)

// Rose Crimson (Vivid Rose & Ruby)
val RoseCrimsonPrimary = Color(0xFFF43F5E)
val RoseCrimsonPrimaryDark = Color(0xFFE11D48)
val RoseCrimsonGlow = Color(0x33F43F5E)

// Titanium Slate (Monochromatic Minimalist)
val TitaniumPrimary = Color(0xFF64748B)
val TitaniumPrimaryDark = Color(0xFF475569)
val TitaniumGlow = Color(0x3364748B)

// =============================================================================
// CLEAN LIGHT PALETTE (SOFT PEARL SLATE & CRISP WHITE CARDS)
// =============================================================================
val LightBg = Color(0xFFF8FAFC)           // Clean canvas
val LightBgSecondary = Color(0xFFF1F5F9)  // Secondary background
val LightCardBg = Color(0xFFFFFFFF)       // Crisp white cards
val LightCardCell = Color(0xFFF8FAFC)     // Nested cell
val LightCardBorder = Color(0xFFE2E8F0)   // Subtle border
val LightInkPrimary = Color(0xFF0F172A)   // High-contrast readable title/body
val LightInkSecondary = Color(0xFF475569) // Muted subtext
val LightInkTertiary = Color(0xFF94A3B8)  // Captions/dates

// =============================================================================
// MIDNIGHT DARK PALETTE (DEEP NAVY / SLATE)
// =============================================================================
val DarkBg = Color(0xFF0B0F19)            // Deep midnight
val DarkBgSecondary = Color(0xFF111827)   // Secondary dark
val DarkCardBg = Color(0xFF161F30)        // Midnight card
val DarkCardCell = Color(0xFF1E293B)      // Nested cell
val DarkCardBorder = Color(0xFF25334D)    // Dark border
val DarkInkPrimary = Color(0xFFF8FAFC)    // Bright white
val DarkInkSecondary = Color(0xFF94A3B8)  // Silver slate
val DarkInkTertiary = Color(0xFF64748B)   // Dim caption

// =============================================================================
// PURE AMOLED TRUE BLACK PALETTE
// =============================================================================
val AmoledBg = Color(0xFF000000)          // Zero-nit black
val AmoledBgSecondary = Color(0xFF080808)
val AmoledCardBg = Color(0xFF121214)      // Pure dark card
val AmoledCardCell = Color(0xFF1A1A1E)    // Nested cell
val AmoledCardBorder = Color(0xFF25252A)  // Subtle edge border
val AmoledInkPrimary = Color(0xFFFFFFFF)  // Maximum contrast white
val AmoledInkSecondary = Color(0xFFA0A0A8)
val AmoledInkTertiary = Color(0xFF585860)

// Backward Compatibility Aliases for Legacy Components - dynamically bound to current theme
val AppleBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val AppleSecondaryBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.cardCell

val AppleCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.cardBackground

val AppleCardSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.cardCell

val AppleCardElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.customTokens.isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)

val AppleSeparator: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.cardBorder.copy(alpha = 0.5f)

val AppleHairline: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.cardBorder

val AppleGreen = EmeraldPrimary
val AppleGreenGlow = EmeraldGlow
val ApplePurple = RoyalVioletPrimary
val ApplePurpleGlow = RoyalVioletGlow
val AppleBlue = Color(0xFF3B82F6)
val AppleBlueGlow = Color(0x333B82F6)
val AppleOrange = SunsetAmberPrimary
val AppleOrangeGlow = SunsetAmberGlow
val AppleRed = Color(0xFFEF4444)
val AppleRedGlow = Color(0x33EF4444)
val AppleYellow = Color(0xFFFBBF24)

val AppleTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.textPrimary

val AppleTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.textSecondary

val AppleTextTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.textTertiary

val AppleTextQuaternary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.textTertiary.copy(alpha = 0.6f)

val AppleFrostedGlass: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.customTokens.cardBackground.copy(alpha = 0.90f)
val AppleGlassHighlight = Color(0x1AFFFFFF)

val SievePrimary = EmeraldPrimary
val SievePrimaryVariant = EmeraldPrimaryDark
val SieveSecondary = AppleBlue
val SieveTertiary = RoyalVioletPrimary

val DarkBackground = DarkBg
val DarkSurface = DarkCardBg
val DarkSurfaceVariant = DarkCardCell
val DarkOnBackground = DarkInkPrimary
val DarkOnSurface = DarkInkPrimary
val DarkOnSurfaceVariant = DarkInkSecondary

val LightBackground = LightBg
val LightSurface = LightCardBg
val LightSurfaceVariant = LightCardCell
val LightOnBackground = LightInkPrimary
val LightOnSurface = LightInkPrimary
val LightOnSurfaceVariant = LightInkSecondary

val BlockRed = AppleRed
val BlockRedBg = AppleRedGlow
val AllowGreen = EmeraldPrimary
val AllowGreenBg = EmeraldGlow
val AutoBlue = AppleBlue
val AutoBlueBg = AppleBlueGlow
val AiPurple = RoyalVioletPrimary
val AiPurpleBg = RoyalVioletGlow
