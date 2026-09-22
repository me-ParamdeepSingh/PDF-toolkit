package com.paramdeep.pdftoolkit.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Corner radius: 12px for cards, 8px for buttons, 999px for pills/chips
val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),      // Buttons & Inputs
    medium = RoundedCornerShape(12.dp),    // Cards & Dialogs
    large = RoundedCornerShape(16.dp),     // Sheets
    extraLarge = RoundedCornerShape(999.dp)// Pills / Chips
)
