package com.paramdeep.pdftoolkit.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paramdeep.pdftoolkit.data.model.PdfPageInfo
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

class SplitPdfViewModel(application: Application) : AndroidViewModel(application) {
    private val pdfEngine = AndroidPdfEngine(application.applicationContext)
    private val repository = RecentFilesRepository(application.applicationContext)

    private val _sourcePdfUri = MutableStateFlow<Uri?>(null)
    val sourcePdfUri: StateFlow<Uri?> = _sourcePdfUri.asStateFlow()

    private val _sourcePdfName = MutableStateFlow("")
    val sourcePdfName: StateFlow<String> = _sourcePdfName.asStateFlow()

    private val _pages = MutableStateFlow<List<PdfPageInfo>>(emptyList())
    val pages: StateFlow<List<PdfPageInfo>> = _pages.asStateFlow()

    private val _selectedPages = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPages: StateFlow<Set<Int>> = _selectedPages.asStateFlow()

    private val _isLoadingThumbnails = MutableStateFlow(false)
    val isLoadingThumbnails: StateFlow<Boolean> = _isLoadingThumbnails.asStateFlow()

    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    private var processJob: Job? = null

    fun setSourcePdf(uri: Uri) {
        _sourcePdfUri.value = uri
        _sourcePdfName.value = pdfEngine.getFileName(uri)
        loadThumbnails(uri)
    }

    private fun loadThumbnails(uri: Uri) {
        viewModelScope.launch {
            _isLoadingThumbnails.value = true
            try {
                val pageList = pdfEngine.getPdfPageThumbnails(uri)
                _pages.value = pageList
                // By default select all pages
                _selectedPages.value = pageList.map { it.pageNumber }.toSet()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingThumbnails.value = false
            }
        }
    }

    fun togglePage(pageNumber: Int) {
        val current = _selectedPages.value.toMutableSet()
        if (current.contains(pageNumber)) {
            current.remove(pageNumber)
        } else {
            current.add(pageNumber)
        }
        _selectedPages.value = current
    }

    fun selectAll() {
        _selectedPages.value = _pages.value.map { it.pageNumber }.toSet()
    }

    fun deselectAll() {
        _selectedPages.value = emptySet()
    }

    fun applyPageRange(rangeText: String) {
        val pageNumbers = mutableSetOf<Int>()
        val total = _pages.value.size
        try {
            val parts = rangeText.split(",")
            for (part in parts) {
                val trimmed = part.trim()
                if (trimmed.contains("-")) {
                    val bounds = trimmed.split("-")
                    val start = bounds[0].trim().toIntOrNull() ?: 1
                    val end = bounds[1].trim().toIntOrNull() ?: total
                    for (p in start.coerceIn(1, total)..end.coerceIn(1, total)) {
                        pageNumbers.add(p)
                    }
                } else {
                    val p = trimmed.toIntOrNull()
                    if (p != null && p in 1..total) {
                        pageNumbers.add(p)
                    }
                }
            }
            if (pageNumbers.isNotEmpty()) {
                _selectedPages.value = pageNumbers
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun splitPdf(onSuccess: (ResultData) -> Unit) {
        val uri = _sourcePdfUri.value ?: return
        val selected = _selectedPages.value
        if (selected.isEmpty()) return

        processJob?.cancel()
        processJob = viewModelScope.launch {
            _processingState.value = ProcessingState.Processing(0f, "Extracting pages...")
            try {
                val outputFile = pdfEngine.splitPdf(
                    pdfUri = uri,
                    selectedPages = selected,
                    onProgress = { progress, step ->
                        _processingState.value = ProcessingState.Processing(progress, step)
                    }
                )

                val originalSize = pdfEngine.getFileSize(uri)
                val resultData = ResultData(
                    uri = Uri.fromFile(outputFile),
                    filePath = outputFile.absolutePath,
                    fileName = outputFile.name,
                    toolType = ToolType.SPLIT_PDF,
                    originalSizeBytes = originalSize,
                    outputSizeBytes = outputFile.length(),
                    pageCount = selected.size
                )

                repository.addRecentFile(
                    RecentFile(
                        id = UUID.randomUUID().toString(),
                        name = outputFile.name,
                        filePath = outputFile.absolutePath,
                        sizeBytes = outputFile.length(),
                        timestamp = System.currentTimeMillis(),
                        toolType = ToolType.SPLIT_PDF,
                        pageCount = selected.size
                    )
                )

                _processingState.value = ProcessingState.Success(resultData)
                onSuccess(resultData)
            } catch (e: Exception) {
                _processingState.value = ProcessingState.Error(e.localizedMessage ?: "Failed to split PDF")
            }
        }
    }

    fun cancelProcessing() {
        processJob?.cancel()
        _processingState.value = ProcessingState.Idle
    }

    fun resetState() {
        _sourcePdfUri.value = null
        _pages.value = emptyList()
        _selectedPages.value = emptySet()
        _processingState.value = ProcessingState.Idle
    }
}
