package com.habitpulse.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light color scheme for the HabitPulse application.
 *
 * Uses a purple-based primary palette that conveys a sense of focus and productivity,
 * combined with neutral surface tones for comfortable extended reading.
 */
private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurplePrimaryContainer,
    onPrimaryContainer = PurpleOnPrimaryContainer,
    secondary = TealSecondary,
    onSecondary = TealOnSecondary,
    secondaryContainer = TealSecondaryContainer,
    onSecondaryContainer = TealOnSecondaryContainer,
    tertiary = PinkTertiary,
    onTertiary = PinkOnTertiary,
    tertiaryContainer = PinkTertiaryContainer,
    onTertiaryContainer = PinkOnTertiaryContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

/**
 * Dark color scheme for the HabitPulse application.
 *
 * Lighter primary tones on dark surfaces ensure accessibility and readability
 * while maintaining the purple brand identity.
 */
private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDark,
    onPrimary = PurpleOnPrimaryDark,
    primaryContainer = PurplePrimaryContainerDark,
    onPrimaryContainer = PurpleOnPrimaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

/**
 * Root composable theme for the HabitPulse application.
 *
 * Applies Material3 theming with support for:
 * - **Dynamic colors** on Android 12+ (Material You), falling back to the custom palette.
 * - **Dark mode** following the system preference.
 * - **Edge-to-edge** status bar configuration.
 *
 * @param darkTheme Whether to use the dark color scheme. Defaults to the system setting.
 * @param dynamicColor Whether to use Material You dynamic colors on supported devices.
 * @param content The composable content tree to theme.
 */
@Composable
fun HabitPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HabitPulseTypography,
        content = content
    )
}
