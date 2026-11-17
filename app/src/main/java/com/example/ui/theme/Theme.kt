package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ColorErrorOn = androidx.compose.ui.graphics.Color(0xFF690005)
private val ColorErrorContainerOn = androidx.compose.ui.graphics.Color(0xFFFFDAD6)

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = VoidSlate,
    primaryContainer = ElectricTeal,
    onPrimaryContainer = VoidSlate,
    secondary = CyberViolet,
    onSecondary = PureWhite,
    secondaryContainer = VioletContainer,
    onSecondaryContainer = LavenderGlow,
    tertiary = NeonCyanSoft,
    onTertiary = VoidSlate,
    background = VoidSlate,
    onBackground = OnSurfaceLight,
    surface = CyberDeepSlate,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = MidSlate,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = MidSlate,
    outlineVariant = OutlineTeal,
    error = ErrorCoral,
    onError = ColorErrorOn,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ColorErrorContainerOn
)

val CyberShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun DailyAiMasteryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = Typography,
        shapes = CyberShapes,
        content = content
    )
}
