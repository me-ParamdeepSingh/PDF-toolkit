package com.paramdeep.pdftoolkit.data.model

enum class PageSizeOption(
    val label: String,
    val widthPoints: Int,
    val heightPoints: Int
) {
    A4("A4 (210 × 297 mm)", 595, 842),
    LETTER("US Letter (8.5 × 11 in)", 612, 792),
    FIT_TO_IMAGE("Fit to Image Size", 0, 0)
}

enum class PageOrientation(val label: String) {
    AUTO("Auto Detect"),
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}

enum class ImageQuality(
    val label: String,
    val dpi: Int,
    val format: String
) {
    STANDARD("Standard (150 DPI)", 150, "JPEG"),
    HIGH("High Quality (300 DPI)", 300, "PNG")
}
