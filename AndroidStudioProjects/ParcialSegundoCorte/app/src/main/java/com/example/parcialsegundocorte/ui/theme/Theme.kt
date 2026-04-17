package com.example.parcialsegundocorte.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = Color(0xFFE94560),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1628),
    secondary = Color(0xFF4776E6),
    onSecondary = Color.White,
    background = Color(0xFF0A0A14),
    onBackground = Color.White,
    surface = Color(0xFF1F2544),
    onSurface = Color.White,
    error = Color(0xFFFF6B6B),
    onError = Color.White
)

@Composable
fun ParcialSegundoCorteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
