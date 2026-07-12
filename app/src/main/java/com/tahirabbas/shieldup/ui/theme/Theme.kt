package com.tahirabbas.shieldup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ShieldUpColorScheme = lightColorScheme(
    primary = TrustBlue,
    onPrimary = SurfaceLight,
    secondary = TrustBlueLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = DangerRed
)

@Composable
fun ShieldUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShieldUpColorScheme,
        typography = ShieldUpTypography,
        content = content
    )
}
