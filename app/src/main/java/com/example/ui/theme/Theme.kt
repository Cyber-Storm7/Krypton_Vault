package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.util.SecurityPreferencesManager

@Composable
fun KryptonVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeAccent: KryptonThemeAccent? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val initialTheme = remember(context) { SecurityPreferencesManager.getSelectedTheme(context) }
    val storedTheme by SecurityPreferencesManager.themeAccentFlow.collectAsState(initial = initialTheme)
    val activeAccent = themeAccent ?: storedTheme

    val customColors = remember(activeAccent, darkTheme) {
        getKryptonCustomColors(activeAccent, darkTheme)
    }

    val colorScheme = remember(customColors, darkTheme, dynamicColor) {
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else if (darkTheme) {
            darkColorScheme(
                primary = customColors.primary,
                onPrimary = customColors.onPrimary,
                primaryContainer = customColors.primaryContainer,
                onPrimaryContainer = customColors.onPrimaryContainer,
                secondary = customColors.secondary,
                onSecondary = customColors.onSecondary,
                secondaryContainer = customColors.secondaryContainer,
                onSecondaryContainer = customColors.onSecondaryContainer,
                tertiary = customColors.secondary,
                onTertiary = customColors.onSecondary,
                background = customColors.background,
                onBackground = customColors.onBackground,
                surface = customColors.surface,
                onSurface = customColors.onSurface,
                surfaceVariant = customColors.surfaceElevated,
                onSurfaceVariant = customColors.textSecondary,
                outline = customColors.outline,
                error = DangerRed,
                onError = Color.White
            )
        } else {
            lightColorScheme(
                primary = customColors.primary,
                onPrimary = customColors.onPrimary,
                primaryContainer = customColors.primaryContainer,
                onPrimaryContainer = customColors.onPrimaryContainer,
                secondary = customColors.secondary,
                onSecondary = customColors.onSecondary,
                secondaryContainer = customColors.secondaryContainer,
                onSecondaryContainer = customColors.onSecondaryContainer,
                tertiary = customColors.secondary,
                onTertiary = customColors.onSecondary,
                background = customColors.background,
                onBackground = customColors.onBackground,
                surface = customColors.surface,
                onSurface = customColors.onSurface,
                surfaceVariant = customColors.surfaceElevated,
                onSurfaceVariant = customColors.textSecondary,
                outline = customColors.outline,
                error = DangerRed,
                onError = Color.White
            )
        }
    }

    CompositionLocalProvider(LocalKryptonColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    KryptonVaultTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
