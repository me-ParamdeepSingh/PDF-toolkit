package com.pdftoolkit.app.engine

import android.net.Uri
import com.pdftoolkit.app.data.model.CompressionLevel
import com.pdftoolkit.app.data.model.ImageQuality
import com.pdftoolkit.app.data.model.PageOrientation
import com.pdftoolkit.app.data.model.PageSizeOption
import com.pdftoolkit.app.data.model.PdfPageInfo
import java.io.File

interface PdfEngine {
    suspend fun imagesToPdf(
        imageUris: List<Uri>,
        pageSize: PageSizeOption,
        orientation: PageOrientation,
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): File

    suspend fun mergePdfs(
        pdfUris: List<Uri>,
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): File

    suspend fun splitPdf(
        pdfUri: Uri,
        selectedPages: Set<Int>,
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): File

    suspend fun compressPdf(
        pdfUri: Uri,
        compressionLevel: CompressionLevel,
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): File

    suspend fun pdfToImages(
        pdfUri: Uri,
        quality: ImageQuality,
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): List<File>

    suspend fun getPdfPageThumbnails(
        pdfUri: Uri
    ): List<PdfPageInfo>

    fun getFileSize(uri: Uri): Long
    fun getFileName(uri: Uri): String
}
