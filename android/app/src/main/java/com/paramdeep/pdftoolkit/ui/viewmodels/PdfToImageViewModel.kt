package com.paramdeep.pdftoolkit.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paramdeep.pdftoolkit.data.model.ImageQuality
import com.paramdeep.pdftoolkit.data.model.ProcessingState
import com.paramdeep.pdftoolkit.data.model.RecentFile
import com.paramdeep.pdftoolkit.data.model.ResultData
import com.paramdeep.pdftoolkit.data.model.ToolType
import com.paramdeep.pdftoolkit.data.repository.RecentFilesRepository
import com.paramdeep.pdftoolkit.engine.AndroidPdfEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PdfToImageViewModel(application: Application) : AndroidViewModel(application) {
    private val pdfEngine = AndroidPdfEngine(application.applicationContext)
    private val repository = RecentFilesRepository(application.applicationContext)

    private val _sourcePdfUri = MutableStateFlow<Uri?>(null)
    val sourcePdfUri: StateFlow<Uri?> = _sourcePdfUri.asStateFlow()

    private val _sourcePdfName = MutableStateFlow("")
    val sourcePdfName: StateFlow<String> = _sourcePdfName.asStateFlow()

    private val _quality = MutableStateFlow(ImageQuality.STANDARD)
    val quality: StateFlow<ImageQuality> = _quality.asStateFlow()

    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    private var processJob: Job? = null

    fun setSourcePdf(uri: Uri) {
        _sourcePdfUri.value = uri
        _sourcePdfName.value = pdfEngine.getFileName(uri)
    }

    fun setQuality(q: ImageQuality) {
        _quality.value = q
    }

    fun convertPdfToImages(onSuccess: (ResultData) -> Unit) {
        val uri = _sourcePdfUri.value ?: return

        processJob?.cancel()
        processJob = viewModelScope.launch {
            _processingState.value = ProcessingState.Processing(0f, "Starting page export...")
            try {
                val outputFiles = pdfEngine.pdfToImages(
                    pdfUri = uri,
                    quality = _quality.value,
                    onProgress = { progress, step ->
                        _processingState.value = ProcessingState.Processing(progress, step)
                    }
                )

                if (outputFiles.isEmpty()) {
                    _processingState.value = ProcessingState.Error("No pages found in PDF")
                    return@launch
                }

                val imageUris = outputFiles.map { Uri.fromFile(it) }
                val firstImage = outputFiles.first()
                val totalOutputSize = outputFiles.sumOf { it.length() }
                val originalSize = pdfEngine.getFileSize(uri)

                val resultData = ResultData(
                    uri = Uri.fromFile(firstImage),
                    filePath = firstImage.absolutePath,
                    fileName = "${outputFiles.size} images exported",
                    toolType = ToolType.PDF_TO_IMAGE,
                    originalSizeBytes = originalSize,
                    outputSizeBytes = totalOutputSize,
                    pageCount = outputFiles.size,
                    outputImages = imageUris
                )

                repository.addRecentFile(
                    RecentFile(
                        id = UUID.randomUUID().toString(),
                        name = "${outputFiles.size} images from ${_sourcePdfName.value}",
                        filePath = firstImage.absolutePath,
                        sizeBytes = totalOutputSize,
                        timestamp = System.currentTimeMillis(),
                        toolType = ToolType.PDF_TO_IMAGE,
                        pageCount = outputFiles.size
                    )
                )

                _processingState.value = ProcessingState.Success(resultData)
                onSuccess(resultData)
            } catch (e: Exception) {
                _processingState.value = ProcessingState.Error(e.localizedMessage ?: "Failed to export images")
            }
        }
    }

    fun cancelProcessing() {
        processJob?.cancel()
        _processingState.value = ProcessingState.Idle
    }

    fun resetState() {
        _sourcePdfUri.value = null
        _processingState.value = ProcessingState.Idle
    }
}
