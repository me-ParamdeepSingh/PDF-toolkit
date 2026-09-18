package com.pdftoolkit.app.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdftoolkit.app.data.model.CompressionLevel
import com.pdftoolkit.app.data.model.ProcessingState
import com.pdftoolkit.app.data.model.RecentFile
import com.pdftoolkit.app.data.model.ResultData
import com.pdftoolkit.app.data.model.ToolType
import com.pdftoolkit.app.data.repository.RecentFilesRepository
import com.pdftoolkit.app.engine.AndroidPdfEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CompressPdfViewModel(application: Application) : AndroidViewModel(application) {
    private val pdfEngine = AndroidPdfEngine(application.applicationContext)
    private val repository = RecentFilesRepository(application.applicationContext)

    private val _sourcePdfUri = MutableStateFlow<Uri?>(null)
    val sourcePdfUri: StateFlow<Uri?> = _sourcePdfUri.asStateFlow()

    private val _sourcePdfName = MutableStateFlow("")
    val sourcePdfName: StateFlow<String> = _sourcePdfName.asStateFlow()

    private val _originalSizeBytes = MutableStateFlow(0L)
    val originalSizeBytes: StateFlow<Long> = _originalSizeBytes.asStateFlow()

    private val _compressionLevel = MutableStateFlow(CompressionLevel.MEDIUM)
    val compressionLevel: StateFlow<CompressionLevel> = _compressionLevel.asStateFlow()

    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    private var processJob: Job? = null

    fun setSourcePdf(uri: Uri) {
        _sourcePdfUri.value = uri
        _sourcePdfName.value = pdfEngine.getFileName(uri)
        _originalSizeBytes.value = pdfEngine.getFileSize(uri)
    }

    fun setCompressionLevel(level: CompressionLevel) {
        _compressionLevel.value = level
    }

    fun compressPdf(onSuccess: (ResultData) -> Unit) {
        val uri = _sourcePdfUri.value ?: return
        val level = _compressionLevel.value
        val originalSize = _originalSizeBytes.value

        processJob?.cancel()
        processJob = viewModelScope.launch {
            _processingState.value = ProcessingState.Processing(0f, "Starting compression...")
            try {
                val outputFile = pdfEngine.compressPdf(
                    pdfUri = uri,
                    compressionLevel = level,
                    onProgress = { progress, step ->
                        _processingState.value = ProcessingState.Processing(progress, step)
                    }
                )

                val resultData = ResultData(
                    uri = Uri.fromFile(outputFile),
                    filePath = outputFile.absolutePath,
                    fileName = outputFile.name,
                    toolType = ToolType.COMPRESS_PDF,
                    originalSizeBytes = originalSize,
                    outputSizeBytes = outputFile.length(),
                    pageCount = 1
                )

                repository.addRecentFile(
                    RecentFile(
                        id = UUID.randomUUID().toString(),
                        name = outputFile.name,
                        filePath = outputFile.absolutePath,
                        sizeBytes = outputFile.length(),
                        timestamp = System.currentTimeMillis(),
                        toolType = ToolType.COMPRESS_PDF,
                        pageCount = 1
                    )
                )

                _processingState.value = ProcessingState.Success(resultData)
                onSuccess(resultData)
            } catch (e: Exception) {
                _processingState.value = ProcessingState.Error(e.localizedMessage ?: "Failed to compress PDF")
            }
        }
    }

    fun cancelProcessing() {
        processJob?.cancel()
        _processingState.value = ProcessingState.Idle
    }

    fun resetState() {
        _sourcePdfUri.value = null
        _originalSizeBytes.value = 0L
        _processingState.value = ProcessingState.Idle
    }
}
