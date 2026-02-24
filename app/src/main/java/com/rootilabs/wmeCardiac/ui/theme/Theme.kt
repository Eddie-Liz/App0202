package com.rootilabs.wmeCardiac.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Tag&Go 品牌色
val TagGoGreen = Color(0xFF8BC34A)
val TagGoDarkGreen = Color(0xFF689F38)
val TagGoLightGreen = Color(0xFFC5E1A5)
val TagGoCyan = Color(0xFF80CBC4)
val TagGoDarkCyan = Color(0xFF4DB6AC)
val TagGoRed = Color(0xFFB71C1C)
val TagGoLightRed = Color(0xFFD32F2F)
val TagGoGray = Color(0xFFE0E0E0)
val TagGoDarkGray = Color(0xFF424242)
val TagGoBottomBar = Color(0xFF333333)

private val LightColorScheme = lightColorScheme(
    primary = TagGoGreen,
    onPrimary = Color.White,
    primaryContainer = TagGoGreen,
    onPrimaryContainer = Color.White,
    secondary = TagGoCyan,
    onSecondary = Color.White,
    background = Color(0xFFEEEEEE),
    surface = Color.White,
    error = TagGoRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = TagGoRed
)

private val DarkColorScheme = darkColorScheme(
    primary = TagGoGreen,
    onPrimary = Color.White,
    primaryContainer = TagGoDarkGreen,
    onPrimaryContainer = Color.White,
    secondary = TagGoCyan,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = TagGoLightRed,
    onError = Color.White
)

@Composable
fun TagGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
