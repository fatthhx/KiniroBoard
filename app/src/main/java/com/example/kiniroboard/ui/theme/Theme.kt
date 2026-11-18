package com.example.kiniroboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KiniroColorScheme = lightColorScheme(
    primary = InkBlack,
    onPrimary = KiniroNeonGreen,
    secondary = LilacDeep,
    onSecondary = Color.White,
    tertiary = KiniroNeonGreen,
    background = PaperCream,
    onBackground = InkBlack,
    surface = Color.White,
    onSurface = InkBlack,
    surfaceVariant = LilacPastel,
    onSurfaceVariant = InkBlack
)

@Composable
fun KiniroBoardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KiniroColorScheme,
        typography = Typography,
        content = content
    )
}