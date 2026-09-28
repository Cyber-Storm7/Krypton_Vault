package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Krypton Vault Theme Accent Palette:
 * Supports high-contrast Monochrome (Black & White) as default,
 * plus 6 vibrant custom color accents.
 */
enum class KryptonThemeAccent(
    val id: String,
    val title: String,
    val subtitle: String,
    val previewColor: Color,
    val secondaryPreviewColor: Color? = null
) {
    MONOCHROME(
        id = "monochrome",
        title = "Black & White",
        subtitle = "OLED Pitch Black & Pure White",
        previewColor = Color(0xFFFFFFFF)
    ),
    EMERALD(
        id = "emerald",
        title = "Emerald",
        subtitle = "Zero-Knowledge Green",
        previewColor = Color(0xFF10B981)
    ),
    CYBER_CYAN(
        id = "cyber_cyan",
        title = "Cyber Cyan",
        subtitle = "Electric Neon Cyan",
        previewColor = Color(0xFF06B6D4)
    ),
    NEON_PURPLE(
        id = "neon_purple",
        title = "Neon Purple",
        subtitle = "Deep Tech Amethyst",
        previewColor = Color(0xFFA855F7)
    ),
    CRIMSON_RED(
        id = "crimson_red",
        title = "Crimson Red",
        subtitle = "Intense Ruby Red",
        previewColor = Color(0xFFEF4444)
    ),
    AMBER_GOLD(
        id = "amber_gold",
        title = "Amber Gold",
        subtitle = "Warm Amber Bronze",
        previewColor = Color(0xFFF59E0B)
    ),
    SAPPHIRE_BLUE(
        id = "sapphire_blue",
        title = "Sapphire Blue",
        subtitle = "Cobalt Tech Blue",
        previewColor = Color(0xFF3B82F6)
    ),
    GREEN_WHITE(
        id = "green_white",
        title = "Green & White",
        subtitle = "Vibrant Emerald & Crisp White",
        previewColor = Color(0xFF10B981),
        secondaryPreviewColor = Color(0xFFFFFFFF)
    ),
    YELLOW_BROWN(
        id = "yellow_brown",
        title = "Yellow & Brown",
        subtitle = "Warm Gold & Earth Brown",
        previewColor = Color(0xFFFACC15),
        secondaryPreviewColor = Color(0xFF854D0E)
    ),
    RED_WHITE(
        id = "red_white",
        title = "Red & White",
        subtitle = "Vivid Crimson & Crisp White",
        previewColor = Color(0xFFEF4444),
        secondaryPreviewColor = Color(0xFFFFFFFF)
    ),
    CYAN_ORANGE(
        id = "cyan_orange",
        title = "Cyan & Orange",
        subtitle = "Electric Cyan & Sunset Orange",
        previewColor = Color(0xFF06B6D4),
        secondaryPreviewColor = Color(0xFFF97316)
    ),
    PURPLE_GOLD(
        id = "purple_gold",
        title = "Purple & Gold",
        subtitle = "Royal Amethyst & Warm Gold",
        previewColor = Color(0xFFA855F7),
        secondaryPreviewColor = Color(0xFFFBBF24)
    );

    companion object {
        fun fromId(id: String?): KryptonThemeAccent {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MONOCHROME
        }
    }
}
