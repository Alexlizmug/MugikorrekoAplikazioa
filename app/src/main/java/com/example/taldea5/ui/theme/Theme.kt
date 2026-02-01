package com.example.taldea5.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SushiRed,
    secondary = SushiRedDark,
    tertiary = SushiRedLight,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = SushiRedDark,
    onBackground = AppText,
    onSurface = AppText
)

@Composable
fun Taldea5Theme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
