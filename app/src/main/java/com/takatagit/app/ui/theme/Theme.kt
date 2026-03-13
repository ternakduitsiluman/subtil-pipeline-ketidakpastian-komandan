package com.takatagit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TakataGitColorScheme = darkColorScheme(
    primary = AccentBlue,
    background = Background,
    surface = SurfaceCard,
    surfaceContainer = SurfaceCard,
    onPrimary = Background,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMuted,
    outline = TerminalBorder,
)

@Composable
fun TakataGitTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TakataGitColorScheme,
        typography = TakataGitTypography,
        content = content,
    )
}
