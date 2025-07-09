package com.example.mentalnote.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mentalnote.R


val CustomFontFamily = FontFamily(Font(R.font.dunggeunmo))

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal, fontSize = 80.sp),
    displayMedium = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal, fontSize = 64.sp),
    displaySmall = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    headlineLarge = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    headlineMedium = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    headlineSmall = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    titleLarge = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    titleMedium = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    titleSmall = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    bodyLarge = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    labelMedium = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal),
    labelSmall = TextStyle(fontFamily = CustomFontFamily, fontWeight = FontWeight.Normal)
)