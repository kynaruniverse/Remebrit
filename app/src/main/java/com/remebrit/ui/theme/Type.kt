package com.remebrit.ui.theme

import androidx.compose.material3.Typography

private val base = Typography()

val RemebritTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = ManropeFontFamily),
    displayMedium = base.displayMedium.copy(fontFamily = ManropeFontFamily),
    displaySmall = base.displaySmall.copy(fontFamily = ManropeFontFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = ManropeFontFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = ManropeFontFamily, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
    headlineSmall = base.headlineSmall.copy(fontFamily = ManropeFontFamily),
    titleLarge = base.titleLarge.copy(fontFamily = ManropeFontFamily),
    titleMedium = base.titleMedium.copy(fontFamily = ManropeFontFamily),
    titleSmall = base.titleSmall.copy(fontFamily = ManropeFontFamily),
    bodyLarge = base.bodyLarge.copy(fontFamily = ManropeFontFamily),
    bodyMedium = base.bodyMedium.copy(fontFamily = ManropeFontFamily),
    bodySmall = base.bodySmall.copy(fontFamily = ManropeFontFamily),
    labelLarge = base.labelLarge.copy(fontFamily = ManropeFontFamily),
    labelMedium = base.labelMedium.copy(fontFamily = ManropeFontFamily),
    labelSmall = base.labelSmall.copy(fontFamily = ManropeFontFamily)
)