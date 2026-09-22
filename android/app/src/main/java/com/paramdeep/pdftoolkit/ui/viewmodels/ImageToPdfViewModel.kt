package com.paramdeep.pdftoolkit.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paramdeep.pdftoolkit.data.model.PageOrientation
import com.paramdeep.pdftoolkit.data.model.PageSizeOption
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
import java.io.File
import java.util.UUID

class ImageToPdfViewModel(application: Application) : AndroidViewModel(application) {
    private val pdfEngine = AndroidPdfEngine(application.applicationContext)
    private val repository = RecentFilesRepository(application.applicationContext)

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    private val _pageSize = MutableStateFlow(PageSizeOption.A4)
    val pageSize: StateFlow<PageSizeOption> = _pageSize.asStateFlow()

    private val _orientation = MutableStateFlow(PageOrientation.AUTO)
    val orientation: StateFlow<PageOrientation> = _orientation.asStateFlow()

    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    private var processJob: Job? = null

    fun addImages(uris: List<Uri>) {
        val current = _selectedImages.value.toMutableList()
        uris.forEach { if (!current.contains(it)) current.add(it) }
        _selectedImages.value = current
    }

    fun removeImage(index: Int) {
        val current = _selectedImages.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _selectedImages.value = current
        }
    }

    fun moveImage(fromIndex: Int, toIndex: Int) {
        val current = _selectedImages.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _selectedImages.value = current
        }
    }

    fun setPageSize(option: PageSizeOption) {
        _pageSize.value = option
    }

    fun setOrientation(option: PageOrientation) {
        _orientation.value = option
    }

    fun convertToPdf(onSuccess: (ResultData) -> Unit) {
        val images = _selectedImages.value
        if (images.isEmpty()) return

        processJob?.cancel()
        processJob = viewModelScope.launch {
            _processingState.value = ProcessingState.Processing(0f, "Starting conversion...")
            try {
                val outputFile = pdfEngine.imagesToPdf(
                    imageUris = images,
                    pageSize = _pageSize.value,
                    orientation = _orientation.value,
                    onProgress = { progress, step ->
                        _processingState.value = ProcessingState.Processing(progress, step)
                    }
                )

                val resultData = ResultData(
                    uri = Uri.fromFile(outputFile),
                    filePath = outputFile.absolutePath,
                    fileName = outputFile.name,
                    toolType = ToolType.IMAGE_TO_PDF,
                    originalSizeBytes = images.sumOf { pdfEngine.getFileSize(it) },
                    outputSizeBytes = outputFile.length(),
                    pageCount = images.size
                )

                // Save to recent files
                repository.addRecentFile(
                    RecentFile(
                        id = UUID.randomUUID().toString(),
                        name = outputFile.name,
                        filePath = outputFile.absolutePath,
                        sizeBytes = outputFile.length(),
                        timestamp = System.currentTimeMillis(),
                        toolType = ToolType.IMAGE_TO_PDF,
                        pageCount = images.size
                    )
                )

                _processingState.value = ProcessingState.Success(resultData)
                onSuccess(resultData)
            } catch (e: Exception) {
                _processingState.value = ProcessingState.Error(e.localizedMessage ?: "Failed to generate PDF")
            }
        }
    }

    fun cancelProcessing() {
        processJob?.cancel()
        _processingState.value = ProcessingState.Idle
    }

    fun resetState() {
        _selectedImages.value = emptyList()
        _processingState.value = ProcessingState.Idle
    }
}
