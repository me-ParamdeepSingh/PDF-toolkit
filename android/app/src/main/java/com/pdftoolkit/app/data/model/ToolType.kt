package com.pdftoolkit.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Difference
import androidx.compose.material.icons.outlined.Image
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolType(
    val title: String,
    val description: String,
    val route: String
) {
    IMAGE_TO_PDF(
        title = "Image to PDF",
        description = "Convert photos & gallery images into a clean PDF document",
        route = "image_to_pdf"
    ),
    MERGE_PDF(
        title = "Merge PDFs",
        description = "Combine 2 or more PDF documents into a single file",
        route = "merge_pdf"
    ),
    SPLIT_PDF(
        title = "Split PDF",
        description = "Extract selected pages or custom page ranges into a new PDF",
        route = "split_pdf"
    ),
    COMPRESS_PDF(
        title = "Compress PDF",
        description = "Shrink PDF file size while keeping visual clarity crisp",
        route = "compress_pdf"
    ),
    PDF_TO_IMAGE(
        title = "PDF to Images",
        description = "Export individual PDF pages as high quality JPG or PNG",
        route = "pdf_to_image"
    );

    val icon: ImageVector
        get() = when (this) {
            IMAGE_TO_PDF -> Icons.Outlined.Image
            MERGE_PDF -> Icons.Outlined.Difference
            SPLIT_PDF -> Icons.Outlined.CallSplit
            COMPRESS_PDF -> Icons.Outlined.Compress
            PDF_TO_IMAGE -> Icons.Outlined.Collections
        }
}
