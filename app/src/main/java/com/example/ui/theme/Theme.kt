package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldMint,
    onPrimary = EmeraldDeep,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = EmeraldSoft,
    secondary = GoldAccent,
    onSecondary = Color(0xFF1C1917),
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = GoldSoft,
    tertiary = Color(0xFF818CF8),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1E1B4B),
    onTertiaryContainer = TransferSoft,
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF881337),
    onErrorContainer = ExpenseSoft,
    background = BackgroundDark,
    onBackground = OnSurfaceLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceMutedDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoft,
    onPrimaryContainer = EmeraldDeep,
    secondary = GoldPrimary,
    onSecondary = Color.White,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = Color(0xFF451A03),
    tertiary = TransferIndigo,
    onTertiary = Color.White,
    tertiaryContainer = TransferSoft,
    onTertiaryContainer = Color(0xFF1E1B4B),
    error = ExpenseCoral,
    onError = Color.White,
    errorContainer = ExpenseSoft,
    onErrorContainer = Color(0xFF881337),
    background = BackgroundLight,
    onBackground = OnSurfaceDark,
    surface = SurfaceLight,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceMuted,
    outline = OutlineLight
)

val DanaFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = DanaFlowShapes,
        content = content
    )
}
