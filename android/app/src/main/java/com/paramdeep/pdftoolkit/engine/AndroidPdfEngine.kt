package com.paramdeep.pdftoolkit.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.paramdeep.pdftoolkit.data.model.CompressionLevel
import com.paramdeep.pdftoolkit.data.model.ImageQuality
import com.paramdeep.pdftoolkit.data.model.PageOrientation
import com.paramdeep.pdftoolkit.data.model.PageSizeOption
import com.paramdeep.pdftoolkit.data.model.PdfPageInfo
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidPdfEngine(private val context: Context) : PdfEngine {

    private val outputDir: File
        get() {
            val dir = File(context.filesDir, "processed_pdfs")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    private val imageOutputDir: File
        get() {
            val dir = File(context.filesDir, "exported_images")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    override suspend fun imagesToPdf(
        imageUris: List<Uri>,
        pageSize: PageSizeOption,
        orientation: PageOrientation,
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val total = imageUris.size
        val pdfDocument = PdfDocument()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "PDFToolkit_Images_$timestamp.pdf")

        try {
            for (i in imageUris.indices) {
                val uri = imageUris[i]
                val currentStep = "Processing image ${i + 1} of $total..."
                val progress = (i.toFloat() / total) * 0.9f
                onProgress(progress, currentStep)

                val bitmap = loadOptimizedBitmap(uri, maxDimension = 2400)
                    ?: continue

                // Calculate target page dimensions in points (1/72 inch)
                val isLandscape = when (orientation) {
                    PageOrientation.PORTRAIT -> false
                    PageOrientation.LANDSCAPE -> true
                    PageOrientation.AUTO -> bitmap.width > bitmap.height
                }

                val pageWidth: Int
                val pageHeight: Int

                when (pageSize) {
                    PageSizeOption.A4 -> {
                        pageWidth = if (isLandscape) PageSizeOption.A4.heightPoints else PageSizeOption.A4.widthPoints
                        pageHeight = if (isLandscape) PageSizeOption.A4.widthPoints else PageSizeOption.A4.heightPoints
                    }
                    PageSizeOption.LETTER -> {
                        pageWidth = if (isLandscape) PageSizeOption.LETTER.heightPoints else PageSizeOption.LETTER.widthPoints
                        pageHeight = if (isLandscape) PageSizeOption.LETTER.widthPoints else PageSizeOption.LETTER.heightPoints
                    }
                    PageSizeOption.FIT_TO_IMAGE -> {
                        // Fit to exact aspect ratio capped to standard bounds
                        pageWidth = bitmap.width.coerceAtMost(1200)
                        val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()
                        pageHeight = (pageWidth * ratio).toInt()
                    }
                }

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                // Draw background white
                canvas.drawColor(Color.WHITE)

                // Scale bitmap to fit page
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val srcRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                val dstRect = calculateFittedRect(srcRect, pageWidth.toFloat(), pageHeight.toFloat(), margin = 16f)
                canvas.drawBitmap(bitmap, null, dstRect, paint)

                pdfDocument.finishPage(page)
                bitmap.recycle()
            }

            onProgress(0.95f, "Finalizing PDF file...")
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            onProgress(1.0f, "Completed!")
            outputFile
        } finally {
            pdfDocument.close()
        }
    }

    override suspend fun mergePdfs(
        pdfUris: List<Uri>,
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val total = pdfUris.size
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "PDFToolkit_Merged_$timestamp.pdf")

        val merger = PDFMergerUtility().apply {
            destinationFileName = outputFile.absolutePath
        }

        val tempFiles = mutableListOf<File>()
        try {
            for (i in pdfUris.indices) {
                val uri = pdfUris[i]
                val step = "Adding PDF document ${i + 1} of $total..."
                val progress = (i.toFloat() / total) * 0.7f
                onProgress(progress, step)

                val tempFile = copyUriToTempFile(uri, "merge_part_$i")
                tempFiles.add(tempFile)
                merger.addSource(tempFile)
            }

            onProgress(0.8f, "Merging documents...")
            merger.mergeDocuments(null)
            onProgress(1.0f, "Completed!")
            outputFile
        } finally {
            tempFiles.forEach { it.delete() }
        }
    }

    override suspend fun splitPdf(
        pdfUri: Uri,
        selectedPages: Set<Int>, // 1-indexed
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "PDFToolkit_Split_$timestamp.pdf")

        val tempInput = copyUriToTempFile(pdfUri, "split_source")
        try {
            onProgress(0.1f, "Opening source document...")
            val sourceDoc = PDDocument.load(tempInput)
            val newDoc = PDDocument()

            val sortedPages = selectedPages.sorted()
            val totalSelected = sortedPages.size

            for (i in sortedPages.indices) {
                val pageNum = sortedPages[i]
                val pageIndex = pageNum - 1
                if (pageIndex in 0 until sourceDoc.numberOfPages) {
                    val page: PDPage = sourceDoc.getPage(pageIndex)
                    newDoc.addPage(page)
                }
                val progress = 0.1f + (i.toFloat() / totalSelected) * 0.75f
                onProgress(progress, "Extracting page $pageNum of $totalSelected...")
            }

            onProgress(0.9f, "Saving extracted PDF...")
            newDoc.save(outputFile)
            newDoc.close()
            sourceDoc.close()
            onProgress(1.0f, "Completed!")
            outputFile
        } finally {
            tempInput.delete()
        }
    }

    override suspend fun compressPdf(
        pdfUri: Uri,
        compressionLevel: CompressionLevel,
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "PDFToolkit_Compressed_$timestamp.pdf")
        val tempInput = copyUriToTempFile(pdfUri, "compress_source")

        val pfd = ParcelFileDescriptor.open(tempInput, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val newPdf = PdfDocument()

        try {
            val pageCount = renderer.pageCount
            for (i in 0 until pageCount) {
                val step = "Compressing page ${i + 1} of $pageCount..."
                val progress = (i.toFloat() / pageCount) * 0.85f
                onProgress(progress, step)

                val page = renderer.openPage(i)
                val originalWidth = page.width
                val originalHeight = page.height

                // Calculate scaled dimension based on compression level
                val maxDim = compressionLevel.maxDimension
                val scale = if (originalWidth > originalHeight) {
                    if (originalWidth > maxDim) maxDim.toFloat() / originalWidth else 1.0f
                } else {
                    if (originalHeight > maxDim) maxDim.toFloat() / originalHeight else 1.0f
                }

                val renderWidth = (originalWidth * scale).toInt().coerceAtLeast(100)
                val renderHeight = (originalHeight * scale).toInt().coerceAtLeast(100)

                val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                page.close()

                // Re-compress via JPEG byte stream at specified quality
                val compressedBitmap = compressBitmapToJpeg(bitmap, compressionLevel.jpegQuality)
                bitmap.recycle()

                // Write to new PDF document with standard point size
                val pageInfo = PdfDocument.PageInfo.Builder(originalWidth, originalHeight, i + 1).create()
                val docPage = newPdf.startPage(pageInfo)
                val canvas = docPage.canvas
                canvas.drawColor(Color.WHITE)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val dstRect = RectF(0f, 0f, originalWidth.toFloat(), originalHeight.toFloat())
                canvas.drawBitmap(compressedBitmap, null, dstRect, paint)
                newPdf.finishPage(docPage)
                compressedBitmap.recycle()
            }

            onProgress(0.92f, "Building compressed document...")
            FileOutputStream(outputFile).use { out ->
                newPdf.writeTo(out)
            }
            onProgress(1.0f, "Completed!")
            outputFile
        } finally {
            newPdf.close()
            renderer.close()
            pfd.close()
            tempInput.delete()
        }
    }

    override suspend fun pdfToImages(
        pdfUri: Uri,
        quality: ImageQuality,
        onProgress: suspend (progress: Float, step: String) -> Unit
    ): List<File> = withContext(Dispatchers.IO) {
        val tempInput = copyUriToTempFile(pdfUri, "pdf_to_images_src")
        val pfd = ParcelFileDescriptor.open(tempInput, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val resultFiles = mutableListOf<File>()

        try {
            val total = renderer.pageCount
            val scaleFactor = if (quality == ImageQuality.HIGH) 2.5f else 1.5f
            val ext = if (quality == ImageQuality.HIGH) "png" else "jpg"
            val format = if (quality == ImageQuality.HIGH) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            val compressQuality = if (quality == ImageQuality.HIGH) 100 else 90

            for (i in 0 until total) {
                val step = "Rendering page ${i + 1} of $total to image..."
                val progress = (i.toFloat() / total) * 0.9f
                onProgress(progress, step)

                val page = renderer.openPage(i)
                val width = (page.width * scaleFactor).toInt()
                val height = (page.height * scaleFactor).toInt()

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val file = File(imageOutputDir, "page_${i + 1}_${System.currentTimeMillis()}.$ext")
                FileOutputStream(file).use { out ->
                    bitmap.compress(format, compressQuality, out)
                }
                bitmap.recycle()
                resultFiles.add(file)
            }
            onProgress(1.0f, "Completed!")
            resultFiles
        } finally {
            renderer.close()
            pfd.close()
            tempInput.delete()
        }
    }

    override suspend fun getPdfPageThumbnails(
        pdfUri: Uri
    ): List<PdfPageInfo> = withContext(Dispatchers.IO) {
        val tempInput = copyUriToTempFile(pdfUri, "thumb_source")
        val pfd = ParcelFileDescriptor.open(tempInput, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val pages = mutableListOf<PdfPageInfo>()

        try {
            val total = renderer.pageCount
            val maxPages = total.coerceAtMost(100)
            for (i in 0 until maxPages) {
                val page = renderer.openPage(i)
                val targetWidth = 140
                val ratio = page.height.toFloat() / page.width.toFloat()
                val targetHeight = (targetWidth * ratio).toInt()

                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                pages.add(
                    PdfPageInfo(
                        pageIndex = i,
                        pageNumber = i + 1,
                        thumbnail = bitmap,
                        isSelected = true
                    )
                )
            }
            pages
        } finally {
            renderer.close()
            pfd.close()
            tempInput.delete()
        }
    }

    override fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst() && sizeIndex != -1) {
                    cursor.getLong(sizeIndex)
                } else 0L
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    override fun getFileName(uri: Uri): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    cursor.getString(nameIndex)
                } else "document.pdf"
            } ?: "document.pdf"
        } catch (e: Exception) {
            "document.pdf"
        }
    }

    private fun loadOptimizedBitmap(uri: Uri, maxDimension: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        var sampleSize = 1
        while (options.outWidth / (sampleSize * 2) >= maxDimension ||
            options.outHeight / (sampleSize * 2) >= maxDimension
        ) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    }

    private fun compressBitmapToJpeg(source: Bitmap, quality: Int): Bitmap {
        val stream = java.io.ByteArrayOutputStream()
        source.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun calculateFittedRect(src: RectF, destWidth: Float, destHeight: Float, margin: Float): RectF {
        val availableWidth = destWidth - (margin * 2)
        val availableHeight = destHeight - (margin * 2)

        val srcRatio = src.width() / src.height()
        val destRatio = availableWidth / availableHeight

        val finalWidth: Float
        val finalHeight: Float

        if (srcRatio > destRatio) {
            finalWidth = availableWidth
            finalHeight = availableWidth / srcRatio
        } else {
            finalHeight = availableHeight
            finalWidth = availableHeight * srcRatio
        }

        val left = margin + (availableWidth - finalWidth) / 2f
        val top = margin + (availableHeight - finalHeight) / 2f

        return RectF(left, top, left + finalWidth, top + finalHeight)
    }

    private fun copyUriToTempFile(uri: Uri, prefix: String): File {
        val temp = File.createTempFile(prefix, ".pdf", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(temp).use { output ->
                input.copyTo(output)
            }
        }
        return temp
    }
}
