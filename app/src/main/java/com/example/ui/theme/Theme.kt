package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val LocalIsDarkMode = compositionLocalOf { false }
val LocalThemeMode = compositionLocalOf { "jour" }

/**
 * 60 FPS Solid Bento background brush:
 * Light: #FAF8F9 to #F6E9EC
 * Dark: Noir pur #000000 to Gris profond #0A0204
 * Animated smoothly with tween(150) without heavy shaders.
 */
@Composable
fun rememberSolidBentoBrush(isDark: Boolean): Brush {
    val startColor by animateColorAsState(
        targetValue = if (isDark) DarkBackgroundPure else LightBackgroundStart,
        animationSpec = tween(150),
        label = "solid_bento_bg_start"
    )
    val endColor by animateColorAsState(
        targetValue = if (isDark) DarkBackgroundDeep else LightBackgroundEnd,
        animationSpec = tween(150),
        label = "solid_bento_bg_end"
    )
    return Brush.verticalGradient(listOf(startColor, endColor))
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: String = if (darkTheme) "nuit" else "jour",
    content: @Composable () -> Unit
) {
    // 60 FPS ultra-smooth 150ms animated colors without GPU shader overload
    val primary by animateColorAsState(
        targetValue = if (darkTheme) DarkTextAccent else LightTextAccent,
        animationSpec = tween(150),
        label = "theme_primary"
    )
    val surface by animateColorAsState(
        targetValue = if (darkTheme) DarkSurface else LightSurface,
        animationSpec = tween(150),
        label = "theme_surface"
    )
    val background by animateColorAsState(
        targetValue = if (darkTheme) DarkBackgroundDeep else LightBackgroundStart,
        animationSpec = tween(150),
        label = "theme_background"
    )
    val onSurface by animateColorAsState(
        targetValue = if (darkTheme) DarkTextPrimary else LightTextPrimary,
        animationSpec = tween(150),
        label = "theme_onSurface"
    )
    val outline by animateColorAsState(
        targetValue = if (darkTheme) DarkBorder else LightBorder,
        animationSpec = tween(150),
        label = "theme_outline"
    )
    val secondary by animateColorAsState(
        targetValue = if (darkTheme) DarkSurfaceSecondary else LightSurfaceSecondary,
        animationSpec = tween(150),
        label = "theme_secondary"
    )

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primary,
            secondary = secondary,
            surface = surface,
            background = background,
            onSurface = onSurface,
            outline = outline,
            onPrimary = Color.White
        )
    } else {
        lightColorScheme(
            primary = primary,
            secondary = secondary,
            surface = surface,
            background = background,
            onSurface = onSurface,
            outline = outline,
            onPrimary = Color.White
        )
    }

    CompositionLocalProvider(
        LocalIsDarkMode provides darkTheme,
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
