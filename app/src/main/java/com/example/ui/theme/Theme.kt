package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = MedTealLight,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = MedTealDark,
    onPrimaryContainer = MedTealContainer,
    secondary = MedBlueSecondary,
    tertiary = VerifiedGreen,
    background = MedBackgroundDark,
    surface = MedSurfaceDark,
    onBackground = MedTextPrimaryDark,
    onSurface = MedTextPrimaryDark,
    error = TamperedRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MedTealPrimary,
    onPrimary = Color.White,
    primaryContainer = MedTealContainer,
    onPrimaryContainer = MedOnTealContainer,
    secondary = MedBlueSecondary,
    secondaryContainer = MedBlueContainer,
    onSecondaryContainer = MedOnBlueContainer,
    tertiary = VerifiedGreen,
    background = MedBackgroundLight,
    surface = MedSurfaceLight,
    onBackground = MedTextPrimaryLight,
    onSurface = MedTextPrimaryLight,
    error = TamperedRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
