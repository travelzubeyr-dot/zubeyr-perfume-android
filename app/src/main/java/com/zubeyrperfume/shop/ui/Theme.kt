package com.zubeyrperfume.shop.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The shop's own palette, straight from zubeyrperfume.com. */
val Ink = Color(0xFF0F0F0F)
val Gold = Color(0xFFC8A24A)
val Paper = Color(0xFFFFFFFF)
val Mist = Color(0xFFF7F7F7)
val Body = Color(0xFF222222)
val Faint = Color(0xFF6E6E6E)
val Line = Color(0xFFE6E3DC)
val Sale = Color(0xFF9C2B2B)

private val Scheme = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = Gold,
    onSecondary = Ink,
    tertiary = Gold,
    background = Paper,
    onBackground = Body,
    surface = Paper,
    onSurface = Body,
    surfaceVariant = Mist,
    onSurfaceVariant = Faint,
    outline = Line,
    error = Sale
)

private val Serif = FontFamily.Serif

private val ShopType = Typography(
    displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 21.sp, lineHeight = 27.sp),
    titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 19.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp)
)

val Gutter = 16.dp

@Composable
fun ZubeyrTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = ShopType, content = content)
}
