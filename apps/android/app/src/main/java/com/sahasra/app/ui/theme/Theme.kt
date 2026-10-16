package com.sahasra.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SahasraDarkColorScheme = darkColorScheme(
    primary = SahasraAccent,
    onPrimary = SahasraBg,
    primaryContainer = SahasraElevated,
    onPrimaryContainer = SahasraAccentSecondary,
    secondary = SahasraAccentSecondary,
    onSecondary = SahasraBg,
    secondaryContainer = SahasraSurface,
    onSecondaryContainer = SahasraTextPrimary,
    tertiary = SahasraCrimson,
    onTertiary = SahasraTextPrimary,
    background = SahasraBg,
    onBackground = SahasraTextPrimary,
    surface = SahasraSurface,
    onSurface = SahasraTextPrimary,
    surfaceVariant = SahasraElevated,
    onSurfaceVariant = SahasraTextSecondary,
    outline = SahasraBorder,
    outlineVariant = SahasraBorder,
    error = SahasraCrimson,
    onError = SahasraTextPrimary
)

@Composable
fun SahasraTheme(
    darkTheme: Boolean = true, // Always default to SAHASRA dark aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = SahasraDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SahasraBg.toArgb()
                window.navigationBarColor = SahasraSurface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
