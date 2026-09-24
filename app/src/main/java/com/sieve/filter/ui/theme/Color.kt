package com.sieve.filter.ui.theme

import androidx.compose.ui.graphics.Color

// Apple iOS 18 Cupertino System Colors
val AppleBackground = Color(0xFF000000) // Pure OLED True Black
val AppleSecondaryBackground = Color(0xFF0D0D0E)
val AppleCard = Color(0xFF1C1C1E) // Inset Grouped Card
val AppleCardSecondary = Color(0xFF2C2C2E) // Nested Cell
val AppleCardElevated = Color(0xFF3A3A3C) // Active / Pressed
val AppleSeparator = Color(0x50545458) // 0.5dp Retina Divider
val AppleHairline = Color(0x14FFFFFF) // 0.5dp Specular Border

// Apple Semantic Accents
val AppleGreen = Color(0xFF34C759) // Shield Active / Verified Safe
val AppleGreenGlow = Color(0x3334C759)
val ApplePurple = Color(0xFFAF52DE) // Neural Engine / AI Filter
val ApplePurpleGlow = Color(0x33AF52DE)
val AppleBlue = Color(0xFF007AFF) // Commercial Shield / Actions
val AppleBlueGlow = Color(0x33007AFF)
val AppleOrange = Color(0xFFFF9500) // Flood Guard / Warning
val AppleOrangeGlow = Color(0x33FF9500)
val AppleRed = Color(0xFFFF3B30) // Spam Block / Danger
val AppleRedGlow = Color(0x33FF3B30)
val AppleYellow = Color(0xFFFFCC00) // Star / Highlight

// Apple Text Hierarchy
val AppleTextPrimary = Color(0xFFFFFFFF)
val AppleTextSecondary = Color(0x99EBEBF5)
val AppleTextTertiary = Color(0x4DEBEBF5)
val AppleTextQuaternary = Color(0x2EEBEBF5)

// Glassmorphism
val AppleFrostedGlass = Color(0xDD1C1C1E)
val AppleGlassHighlight = Color(0x1AFFFFFF)

// Sieve Primary Mappings
val SievePrimary = AppleGreen
val SievePrimaryVariant = Color(0xFF30D158)
val SieveSecondary = AppleBlue
val SieveTertiary = ApplePurple

// Dark Palette
val DarkBackground = AppleBackground
val DarkSurface = AppleCard
val DarkSurfaceVariant = AppleCardSecondary
val DarkOnBackground = AppleTextPrimary
val DarkOnSurface = AppleTextPrimary
val DarkOnSurfaceVariant = AppleTextSecondary

// Light Palette
val LightBackground = Color(0xFFF2F2F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE5E5EA)
val LightOnBackground = Color(0xFF000000)
val LightOnSurface = Color(0xFF000000)
val LightOnSurfaceVariant = Color(0xFF8E8E93)

// Legacy Accents & Badges
val BlockRed = AppleRed
val BlockRedBg = AppleRedGlow
val AllowGreen = AppleGreen
val AllowGreenBg = AppleGreenGlow
val AutoBlue = AppleBlue
val AutoBlueBg = AppleBlueGlow
val AiPurple = ApplePurple
val AiPurpleBg = ApplePurpleGlow
