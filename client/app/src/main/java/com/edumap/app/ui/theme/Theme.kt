package com.edumap.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EduMapGreenLight,
    onPrimary = EduMapInk,
    primaryContainer = EduMapGreen,
    onPrimaryContainer = Color.White,
    secondary = EduMapBlueLight,
    onSecondary = EduMapInk,
    background = EduMapDarkBackground,
    onBackground = Color(0xFFE5E7EB),
    surface = EduMapDarkSurface,
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = EduMapDarkSurfaceSoft,
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = EduMapGreen,
    onPrimary = Color.White,
    primaryContainer = EduMapGreenLight,
    onPrimaryContainer = EduMapInk,
    secondary = EduMapBlue,
    onSecondary = Color.White,
    secondaryContainer = EduMapBlueLight,
    onSecondaryContainer = EduMapInk,
    background = Color(0xFFF8FAFC),
    onBackground = EduMapInk,
    surface = EduMapSurface,
    onSurface = EduMapInk,
    surfaceVariant = EduMapSurfaceSoft,
    onSurfaceVariant = EduMapMuted
)

@Composable
fun EduMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
