package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Minimalist Clean Color Palette - Legacy / Base tokens
val EmeraldPrimary = Color(0xFF10B981)
val ElectricMint = Color(0xFF00E5BE)
val MintLight = Color(0xFF6EE7B7)
val CyberCyan = Color(0xFF38BDF8)
val CyberBlue = Color(0xFF0284C7)

// Dark Minimalist Palette (Solid, crisp, zero GPU blur overhead)
val DarkOledBackground = Color(0xFF000000)
val DarkGlassSurface = Color(0xFF101010)
val DarkGlassSurfaceElevated = Color(0xFF18181A)
val DarkGlassCard = Color(0xFF121214)
val DarkGlassOutline = Color(0xFF27272A)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFA1A1AA)
val DarkTextTertiary = Color(0xFF71717A)

// Light Minimalist Palette (Crisp white, clear borders)
val LightPearlescentBackground = Color(0xFFFFFFFF)
val LightGlassSurface = Color(0xFFFFFFFF)
val LightGlassSurfaceElevated = Color(0xFFF4F4F5)
val LightGlassCard = Color(0xFFFFFFFF)
val LightGlassOutline = Color(0xFFE4E4E7)
val LightTextPrimary = Color(0xFF09090B)
val LightTextSecondary = Color(0xFF71717A)
val LightTextTertiary = Color(0xFFA1A1AA)

// Clean Borders & Gradients
val GlassIridescentBorderBrush = Brush.linearGradient(
    listOf(
        Color(0xFF27272A),
        Color(0xFF18181B)
    )
)

val PrimaryGradientBrush = Brush.horizontalGradient(
    listOf(
        Color(0xFFFFFFFF),
        Color(0xFFD4D4D8)
    )
)

val SecondaryGlassBorderBrush = Brush.linearGradient(
    listOf(
        Color(0xFF27272A),
        Color(0xFF27272A)
    )
)

val AmbientGlowEmerald = Color.Transparent
val AmbientGlowMint = Color.Transparent
val DangerRed = Color(0xFFF43F5E)
val WarningAmber = Color(0xFFF59E0B)
val InfoSky = Color(0xFF0EA5E9)

// Backward compatibility aliases
val KryptonGreen = EmeraldPrimary
val KryptonCyan = ElectricMint
val KryptonAmber = WarningAmber
val KryptonRed = DangerRed

/**
 * Complete Dynamic Theme Colors specification
 */
data class KryptonCustomColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceElevated: Color,
    val card: Color,
    val outline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val activeIndicatorBg: Color,
    val activeIndicatorBorder: Color,
    val primaryGradient: Brush,
    val isMonochrome: Boolean,
    val neuBackground: Color = background,
    val neuSurface: Color = surface,
    val neuSurfaceElevated: Color = surfaceElevated,
    val neuInsetSurface: Color = surface,
    val neuLightHighlight: Color = Color.White.copy(alpha = 0.08f),
    val neuDarkShadow: Color = Color.Black.copy(alpha = 0.7f),
    val neuBorderGradient: Brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Black.copy(alpha = 0.4f))),
    val neuInsetBorderGradient: Brush = Brush.linearGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.White.copy(alpha = 0.08f)))
) {
    val isDark: Boolean get() = background == NeutralDarkBackground
}

val LocalKryptonColors = compositionLocalOf<KryptonCustomColors> {
    error("No KryptonCustomColors provided")
}

// Neutral True Dark Tokens (Pure OLED black background, zero color shade on black!)
val NeutralDarkBackground = Color(0xFF000000)
val NeutralDarkSurface = Color(0xFF121214)
val NeutralDarkSurfaceElevated = Color(0xFF18181A)
val NeutralDarkCard = Color(0xFF121214)
val NeutralDarkOutline = Color(0xFF27272A)
val NeutralDarkTextPrimary = Color(0xFFFFFFFF)
val NeutralDarkTextSecondary = Color(0xFFA1A1AA)
val NeutralDarkTextTertiary = Color(0xFF71717A)
val NeutralDarkInsetSurface = Color(0xFF09090B)
val NeutralDarkBorderBrush = Brush.linearGradient(listOf(Color(0xFF2A2A2E), Color(0xFF18181A)))
val NeutralDarkInsetBorderBrush = Brush.linearGradient(listOf(Color(0xFF18181A), Color(0xFF2A2A2E)))

// Neutral Crisp Light Tokens (Clean white & neutral light gray, zero muddy shades)
val NeutralLightBackground = Color(0xFFF6F8FA)
val NeutralLightSurface = Color(0xFFFFFFFF)
val NeutralLightSurfaceElevated = Color(0xFFEFF1F4)
val NeutralLightCard = Color(0xFFFFFFFF)
val NeutralLightOutline = Color(0xFFE2E5E9)
val NeutralLightTextPrimary = Color(0xFF0F172A)
val NeutralLightTextSecondary = Color(0xFF64748B)
val NeutralLightTextTertiary = Color(0xFF94A3B8)
val NeutralLightInsetSurface = Color(0xFFEAECEF)
val NeutralLightBorderBrush = Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFDCE0E6)))
val NeutralLightInsetBorderBrush = Brush.linearGradient(listOf(Color(0xFFD5D9E0), Color(0xFFFFFFFF)))

fun getKryptonCustomColors(
    accent: KryptonThemeAccent,
    isDark: Boolean
): KryptonCustomColors {
    val bg = if (isDark) NeutralDarkBackground else NeutralLightBackground
    val surf = if (isDark) NeutralDarkSurface else NeutralLightSurface
    val surfElevated = if (isDark) NeutralDarkSurfaceElevated else NeutralLightSurfaceElevated
    val card = if (isDark) NeutralDarkCard else NeutralLightCard
    val outline = if (isDark) NeutralDarkOutline else NeutralLightOutline
    val txtPrimary = if (isDark) NeutralDarkTextPrimary else NeutralLightTextPrimary
    val txtSecondary = if (isDark) NeutralDarkTextSecondary else NeutralLightTextSecondary
    val txtTertiary = if (isDark) NeutralDarkTextTertiary else NeutralLightTextTertiary
    val neuInset = if (isDark) NeutralDarkInsetSurface else NeutralLightInsetSurface
    val neuBorder = if (isDark) NeutralDarkBorderBrush else NeutralLightBorderBrush
    val neuInsetBorder = if (isDark) NeutralDarkInsetBorderBrush else NeutralLightInsetBorderBrush
    val neuHighlight = if (isDark) Color(0xFF26262B) else Color.White.copy(alpha = 0.95f)
    val neuShadow = if (isDark) Color(0xFF000000) else Color(0xFFD0D5DD)

    return when (accent) {
        KryptonThemeAccent.MONOCHROME -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFFFFFFFF),
                    onPrimary = Color(0xFF000000),
                    primaryContainer = Color(0xFF27272A),
                    onPrimaryContainer = Color(0xFFFFFFFF),
                    secondary = Color(0xFFE4E4E7),
                    onSecondary = Color(0xFF09090B),
                    secondaryContainer = Color(0xFF1E1E22),
                    onSecondaryContainer = Color(0xFFE4E4E7),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFF3F3F46),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFD4D4D8))),
                    isMonochrome = true,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFF111215),
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFFE4E7EE),
                    onPrimaryContainer = Color(0xFF111215),
                    secondary = Color(0xFF272A32),
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFD6DBE4),
                    onSecondaryContainer = Color(0xFF18181B),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFFCBD2DE),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF111215), Color(0xFF272A32))),
                    isMonochrome = true,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.EMERALD -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = ElectricMint,
                    onPrimary = Color(0xFF00382E),
                    primaryContainer = Color(0xFF004337),
                    onPrimaryContainer = MintLight,
                    secondary = EmeraldPrimary,
                    onSecondary = Color(0xFF003824),
                    secondaryContainer = Color(0xFF004830),
                    onSecondaryContainer = Color(0xFF6EE7B7),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = ElectricMint.copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(ElectricMint, EmeraldPrimary)),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = EmeraldPrimary,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFD1FAE5),
                    onPrimaryContainer = Color(0xFF064E3B),
                    secondary = ElectricMint,
                    onSecondary = Color(0xFF00382E),
                    secondaryContainer = Color(0xFFA7F3D0),
                    onSecondaryContainer = Color(0xFF065F46),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = EmeraldPrimary.copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(EmeraldPrimary, Color(0xFF059669))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.CYBER_CYAN -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFF38BDF8),
                    onPrimary = Color(0xFF003548),
                    primaryContainer = Color(0xFF0C4A6E),
                    onPrimaryContainer = Color(0xFFBAE6FD),
                    secondary = Color(0xFF06B6D4),
                    onSecondary = Color(0xFF00363F),
                    secondaryContainer = Color(0xFF155E75),
                    onSecondaryContainer = Color(0xFFA5F3FC),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFF38BDF8).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFF0284C7),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFE0F2FE),
                    onPrimaryContainer = Color(0xFF0369A1),
                    secondary = Color(0xFF0EA5E9),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFBAE6FD),
                    onSecondaryContainer = Color(0xFF0284C7),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFF0284C7).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.NEON_PURPLE -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFFC084FC),
                    onPrimary = Color(0xFF2E0854),
                    primaryContainer = Color(0xFF4C1D95),
                    onPrimaryContainer = Color(0xFFF3E8FF),
                    secondary = Color(0xFFA855F7),
                    onSecondary = Color(0xFF3B0764),
                    secondaryContainer = Color(0xFF581C87),
                    onSecondaryContainer = Color(0xFFE9D5FF),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFFC084FC).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFC084FC), Color(0xFF9333EA))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFF9333EA),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFF3E8FF),
                    onPrimaryContainer = Color(0xFF581C87),
                    secondary = Color(0xFFA855F7),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFE9D5FF),
                    onSecondaryContainer = Color(0xFF6B21A8),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFF9333EA).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF9333EA), Color(0xFF7E22CE))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.CRIMSON_RED -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFFF87171),
                    onPrimary = Color(0xFF450A0A),
                    primaryContainer = Color(0xFF5C1010),
                    onPrimaryContainer = Color(0xFFFEE2E2),
                    secondary = Color(0xFFEF4444),
                    onSecondary = Color(0xFF500707),
                    secondaryContainer = Color(0xFF7F1D1D),
                    onSecondaryContainer = Color(0xFFFECACA),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFFF87171).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFF87171), Color(0xFFDC2626))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFFDC2626),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFEE2E2),
                    onPrimaryContainer = Color(0xFF7F1D1D),
                    secondary = Color(0xFFEF4444),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFFECACA),
                    onSecondaryContainer = Color(0xFF991B1B),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFFDC2626).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFB91C1C))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.AMBER_GOLD -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFFFBBF24),
                    onPrimary = Color(0xFF451A03),
                    primaryContainer = Color(0xFF572505),
                    onPrimaryContainer = Color(0xFFFEF3C7),
                    secondary = Color(0xFFF59E0B),
                    onSecondary = Color(0xFF451A03),
                    secondaryContainer = Color(0xFF78350F),
                    onSecondaryContainer = Color(0xFFFDE68A),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFFFBBF24).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFFD97706),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFEF3C7),
                    onPrimaryContainer = Color(0xFF78350F),
                    secondary = Color(0xFFF59E0B),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFFDE68A),
                    onSecondaryContainer = Color(0xFF92400E),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFFD97706).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFB45309))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.SAPPHIRE_BLUE -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFF60A5FA),
                    onPrimary = Color(0xFF0B2554),
                    primaryContainer = Color(0xFF172554),
                    onPrimaryContainer = Color(0xFFDBEAFE),
                    secondary = Color(0xFF3B82F6),
                    onSecondary = Color(0xFF0F2B66),
                    secondaryContainer = Color(0xFF1E3A8A),
                    onSecondaryContainer = Color(0xFFBFDBFE),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFF60A5FA).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFF2563EB),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFDBEAFE),
                    onPrimaryContainer = Color(0xFF1E3A8A),
                    secondary = Color(0xFF3B82F6),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFBFDBFE),
                    onSecondaryContainer = Color(0xFF1D4ED8),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFF2563EB).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.GREEN_WHITE -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFF10B981),
                    onPrimary = Color(0xFF003824),
                    primaryContainer = Color(0xFF004337),
                    onPrimaryContainer = Color(0xFFA7F3D0),
                    secondary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFF000000),
                    secondaryContainer = Color(0xFF27272A),
                    onSecondaryContainer = Color(0xFFFFFFFF),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFF10B981).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFFFFFFFF))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFF059669),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFD1FAE5),
                    onPrimaryContainer = Color(0xFF065F46),
                    secondary = Color(0xFF111215),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFE4E7EE),
                    onSecondaryContainer = Color(0xFF111215),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFF059669).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.YELLOW_BROWN -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFFFACC15),
                    onPrimary = Color(0xFF422006),
                    primaryContainer = Color(0xFF713F12),
                    onPrimaryContainer = Color(0xFFFEF08A),
                    secondary = Color(0xFFB45309),
                    onSecondary = Color(0xFF451A03),
                    secondaryContainer = Color(0xFF78350F),
                    onSecondaryContainer = Color(0xFFFDE68A),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFFFACC15).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFFACC15), Color(0xFFB45309))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFFCA8A04),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFEF08A),
                    onPrimaryContainer = Color(0xFF713F12),
                    secondary = Color(0xFF92400E),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFFDE68A),
                    onSecondaryContainer = Color(0xFF78350F),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFFCA8A04).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFCA8A04), Color(0xFF92400E))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.RED_WHITE -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFFEF4444),
                    onPrimary = Color(0xFF450A0A),
                    primaryContainer = Color(0xFF7F1D1D),
                    onPrimaryContainer = Color(0xFFFECACA),
                    secondary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFF000000),
                    secondaryContainer = Color(0xFF27272A),
                    onSecondaryContainer = Color(0xFFFFFFFF),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFFEF4444).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFFFFFFF))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFFDC2626),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFEE2E2),
                    onPrimaryContainer = Color(0xFF991B1B),
                    secondary = Color(0xFF111215),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFE4E7EE),
                    onSecondaryContainer = Color(0xFF111215),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFFDC2626).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFEF4444))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.CYAN_ORANGE -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFF06B6D4),
                    onPrimary = Color(0xFF083344),
                    primaryContainer = Color(0xFF164E63),
                    onPrimaryContainer = Color(0xFFCFFAFE),
                    secondary = Color(0xFFF97316),
                    onSecondary = Color(0xFF431407),
                    secondaryContainer = Color(0xFF7C2D12),
                    onSecondaryContainer = Color(0xFFFFEDD5),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFF06B6D4).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFFF97316))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFF0891B2),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFCFFAFE),
                    onPrimaryContainer = Color(0xFF155E75),
                    secondary = Color(0xFFEA580C),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFFFEDD5),
                    onSecondaryContainer = Color(0xFF9A3412),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFF0891B2).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF0891B2), Color(0xFFEA580C))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
        KryptonThemeAccent.PURPLE_GOLD -> {
            if (isDark) {
                KryptonCustomColors(
                    primary = Color(0xFFA855F7),
                    onPrimary = Color(0xFF3B0764),
                    primaryContainer = Color(0xFF581C87),
                    onPrimaryContainer = Color(0xFFF3E8FF),
                    secondary = Color(0xFFFBBF24),
                    onSecondary = Color(0xFF451A03),
                    secondaryContainer = Color(0xFF78350F),
                    onSecondaryContainer = Color(0xFFFEF3C7),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFF1E1E22),
                    activeIndicatorBorder = Color(0xFFA855F7).copy(alpha = 0.5f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFFFBBF24))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            } else {
                KryptonCustomColors(
                    primary = Color(0xFF9333EA),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFF3E8FF),
                    onPrimaryContainer = Color(0xFF6B21A8),
                    secondary = Color(0xFFD97706),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFFEF3C7),
                    onSecondaryContainer = Color(0xFF92400E),
                    background = bg,
                    onBackground = txtPrimary,
                    surface = surf,
                    onSurface = txtPrimary,
                    surfaceElevated = surfElevated,
                    card = card,
                    outline = outline,
                    textPrimary = txtPrimary,
                    textSecondary = txtSecondary,
                    textTertiary = txtTertiary,
                    activeIndicatorBg = Color(0xFFE2E5E9),
                    activeIndicatorBorder = Color(0xFF9333EA).copy(alpha = 0.6f),
                    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF9333EA), Color(0xFFD97706))),
                    isMonochrome = false,
                    neuBackground = bg,
                    neuSurface = surf,
                    neuSurfaceElevated = surfElevated,
                    neuInsetSurface = neuInset,
                    neuLightHighlight = neuHighlight,
                    neuDarkShadow = neuShadow,
                    neuBorderGradient = neuBorder,
                    neuInsetBorderGradient = neuInsetBorder
                )
            }
        }
    }
}
