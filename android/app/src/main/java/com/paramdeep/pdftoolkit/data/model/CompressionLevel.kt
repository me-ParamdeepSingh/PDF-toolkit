package com.paramdeep.pdftoolkit.data.model

enum class CompressionLevel(
    val label: String,
    val description: String,
    val estimatedReductionPercent: Int,
    val jpegQuality: Int,
    val maxDimension: Int
) {
    LOW(
        label = "Low Compression",
        description = "High quality, slight size reduction (~20-30%)",
        estimatedReductionPercent = 25,
        jpegQuality = 80,
        maxDimension = 1800
    ),
    MEDIUM(
        label = "Medium Compression",
        description = "Balanced quality & size reduction (~50-60%)",
        estimatedReductionPercent = 55,
        jpegQuality = 60,
        maxDimension = 1200
    ),
    HIGH(
        label = "High Compression",
        description = "Smallest size, maximum shrink (~70-80%)",
        estimatedReductionPercent = 75,
        jpegQuality = 40,
        maxDimension = 800
    );

    fun estimateOutputSize(originalSizeBytes: Long): Long {
        val targetRatio = (100 - estimatedReductionPercent) / 100.0
        return (originalSizeBytes * targetRatio).toLong().coerceAtLeast(1024L)
    }
}
