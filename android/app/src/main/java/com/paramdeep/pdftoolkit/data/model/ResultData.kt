package com.paramdeep.pdftoolkit.data.model

import android.graphics.Bitmap
import android.net.Uri

data class PdfPageInfo(
    val pageIndex: Int,
    val pageNumber: Int,
    val thumbnail: Bitmap? = null,
    val isSelected: Boolean = true
)

sealed interface ProcessingState {
    data object Idle : ProcessingState
    data class Processing(
        val progress: Float, // 0.0f to 1.0f
        val currentStep: String,
        val totalSteps: Int = 100,
        val currentStepIndex: Int = 0
    ) : ProcessingState
    data class Success(val result: ResultData) : ProcessingState
    data class Error(val message: String) : ProcessingState
}

data class ResultData(
    val uri: Uri,
    val filePath: String,
    val fileName: String,
    val toolType: ToolType,
    val originalSizeBytes: Long = 0L,
    val outputSizeBytes: Long = 0L,
    val pageCount: Int = 1,
    val outputImages: List<Uri> = emptyList()
) {
    val isMultiImage: Boolean
        get() = outputImages.isNotEmpty()

    val formattedOriginalSize: String
        get() = RecentFile.formatBytes(originalSizeBytes)

    val formattedOutputSize: String
        get() = RecentFile.formatBytes(outputSizeBytes)

    val compressionSavingsText: String?
        get() = if (originalSizeBytes > outputSizeBytes && originalSizeBytes > 0) {
            val saved = originalSizeBytes - outputSizeBytes
            val percent = (saved.toDouble() / originalSizeBytes * 100).toInt()
            "Saved ${RecentFile.formatBytes(saved)} ($percent% smaller)"
        } else null
}
