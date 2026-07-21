package com.cyanogen.dailyrecord.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ColorWhite = androidx.compose.ui.graphics.Color(0xFFFFFFFF)

private val LightColors = lightColorScheme(
    primary = DeepBlueGray,
    onPrimary = Mist,
    secondary = SoftTeal,
    tertiary = SoftTealLight,
    background = Mist,
    surface = ColorWhite,
    onBackground = DeepBlueGray,
    onSurface = DeepBlueGray,
)

private val DarkColors = darkColorScheme(
    primary = SoftTealLight,
    onPrimary = DeepBlueGray,
    secondary = SoftTeal,
    background = NightSurface,
    surface = DeepBlueGray,
    onBackground = Mist,
    onSurface = Mist,
)

@Composable
fun DailyRecordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(colorScheme = colors, content = content)
}
