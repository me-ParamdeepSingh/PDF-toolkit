package com.paramdeep.pdftoolkit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Spacing scale: 4 / 8 / 12 / 16 / 24 / 32 / 48 px
@Immutable
data class Spacing(
    val space1: Dp = 4.dp,
    val space2: Dp = 8.dp,
    val space3: Dp = 12.dp,
    val space4: Dp = 16.dp,
    val space6: Dp = 24.dp,
    val space8: Dp = 32.dp,
    val space12: Dp = 48.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
