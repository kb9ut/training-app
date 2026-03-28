package com.kb9ut.pror.ui.theme

import androidx.compose.ui.graphics.Color

// ===== Industrial Neo-Brutalism + Premium Dark Mode Palette =====

// Accent: Blood Orange
val Accent = Color(0xFFFF4500)
val AccentDim = Color(0xFFCC3700)
val AccentMuted = Color(0xFFCC3700)          // Ember — disabled/inactive states
val AccentSubtle = Color(0x1AFF4500)          // 10% opacity
val AccentGlow = Color(0x33FF4500)            // 20% opacity
val OnAccent = Color(0xFF0A0A0A)

// Primary: Blood Orange
val Primary = Color(0xFFFF4500)
val OnPrimary = Color(0xFF0A0A0A)
val PrimaryContainer = Color(0xFF2E0E00)
val OnPrimaryContainer = Color(0xFFFFB299)

// Secondary: Brushed Steel (industrial, utilitarian)
val Secondary = Color(0xFFA0A0A0)
val OnSecondary = Color(0xFF0A0A0A)
val SecondaryContainer = Color(0xFF222222)
val OnSecondaryContainer = Color(0xFFC0C0C0)

// Tertiary: PR Gold (achievement, celebration)
val Tertiary = Color(0xFFFFD54F)
val OnTertiary = Color(0xFF3E2E00)
val TertiaryContainer = Color(0xFF2A2000)
val OnTertiaryContainer = Color(0xFFFFE89A)

// Error
val Error = Color(0xFFFF3333)
val OnError = Color(0xFF601410)
val ErrorContainer = Color(0xFF2D0A0A)
val OnErrorContainer = Color(0xFFFFB4AB)

// Dark Surfaces (OLED-first, industrial layered depth)
val SurfaceDark = Color(0xFF0A0A0A)                    // Near-black base
val SurfaceDim = Color(0xFF111111)                     // Nav bar, subtle separation
val SurfaceContainerDark = Color(0xFF1A1A1A)           // Visible lift
val SurfaceContainerHighDark = Color(0xFF222222)       // Card backgrounds
val SurfaceContainerHighestDark = Color(0xFF2A2A2A)    // Elevated elements
val SurfaceBright = Color(0xFF333333)                  // Pressed/hover states
val OnSurfaceDark = Color(0xFFF0F0F0)
val OnSurfaceVariantDark = Color(0xFFA0A0A0)

// Steel colors (Industrial Neo-Brutalism)
val BrushedSteel = Color(0xFFA0A0A0)
val DarkSteel = Color(0xFF666666)

// Neon Red (glow effects)
val NeonRed = Color(0xFFFF1744)

// Metal/Industrial border and surface colors
val MetalBorder = Color(0x33666666)                    // 20% dark steel border
val MetalBorderAccent = Color(0x33FF4500)              // 20% accent border
val MetalBackground = Color(0xFF161616)                // Dark metallic fill
val MetalBackgroundElevated = Color(0xFF1E1E1E)        // Elevated metallic fill

// Glass effect colors (kept for backward compat, now steel-based)
val GlassBorder = MetalBorder
val GlassBorderAccent = MetalBorderAccent
val GlassBackground = Color(0xFF161616)
val GlassBackgroundElevated = Color(0xFF1E1E1E)

// Legacy aliases (for backward compatibility)
val DeepTeal = Accent
val DeepTealLight = AccentDim
val DeepTealDark = PrimaryContainer
val GoldPR = Tertiary
val GoldPRContainer = TertiaryContainer
val GoldPRText = Color(0xFFB8860B)
