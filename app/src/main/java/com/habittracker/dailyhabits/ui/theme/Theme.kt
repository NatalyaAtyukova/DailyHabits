package com.habittracker.dailyhabits.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary.copy(alpha = 0.12f),
    onPrimaryContainer = Primary,
    
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Secondary.copy(alpha = 0.12f),
    onSecondaryContainer = Secondary,
    
    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Tertiary.copy(alpha = 0.12f),
    onTertiaryContainer = Tertiary,
    
    error = Error,
    onError = Color.White,
    errorContainer = Error.copy(alpha = 0.12f),
    onErrorContainer = Error,
    
    background = Background,
    onBackground = Color(0xFF1C1B1F),
    surface = Surface,
    onSurface = Color(0xFF1C1B1F),
    
    surfaceVariant = Secondary.copy(alpha = 0.08f),
    onSurfaceVariant = Color(0xFF1C1B1F).copy(alpha = 0.7f),
    outline = Color(0xFF1C1B1F).copy(alpha = 0.12f),

    surfaceTint = Primary
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryDark,
    
    secondary = SecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = SecondaryDark.copy(alpha = 0.12f),
    onSecondaryContainer = SecondaryDark,
    
    tertiary = TertiaryDark,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryDark.copy(alpha = 0.12f),
    onTertiaryContainer = TertiaryDark,
    
    error = ErrorDark,
    onError = Color.White,
    errorContainer = ErrorDark.copy(alpha = 0.12f),
    onErrorContainer = ErrorDark,
    
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    
    surfaceVariant = SecondaryDark.copy(alpha = 0.08f),
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    outline = Color.White.copy(alpha = 0.12f),

    surfaceTint = PrimaryDark
)

@Composable
fun DailyHabitsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Отключаем dynamic color по умолчанию
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
        typography = Typography,
        content = content
    )
}