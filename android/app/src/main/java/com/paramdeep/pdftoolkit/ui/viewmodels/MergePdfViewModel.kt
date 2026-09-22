package com.paramdeep.pdftoolkit.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

data class SelectedPdfDoc(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long
)

class MergePdfViewModel(application: Application) : AndroidViewModel(application) {
    private val pdfEngine = AndroidPdfEngine(application.applicationContext)
    private val repository = RecentFilesRepository(application.applicationContext)

    private val _selectedPdfs = MutableStateFlow<List<SelectedPdfDoc>>(emptyList())
    val selectedPdfs: StateFlow<List<SelectedPdfDoc>> = _selectedPdfs.asStateFlow()

    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    private var processJob: Job? = null

    fun addPdfs(uris: List<Uri>) {
        val current = _selectedPdfs.value.toMutableList()
        uris.forEach { uri ->
            if (current.none { it.uri == uri }) {
                val name = pdfEngine.getFileName(uri)
                val size = pdfEngine.getFileSize(uri)
                current.add(SelectedPdfDoc(uri = uri, name = name, sizeBytes = size))
            }
        }
        _selectedPdfs.value = current
    }

    fun removePdf(index: Int) {
        val current = _selectedPdfs.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _selectedPdfs.value = current
        }
    }

    fun movePdf(fromIndex: Int, toIndex: Int) {
        val current = _selectedPdfs.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _selectedPdfs.value = current
        }
    }

    fun mergePdfs(onSuccess: (ResultData) -> Unit) {
        val pdfs = _selectedPdfs.value
        if (pdfs.size < 2) return

        processJob?.cancel()
        processJob = viewModelScope.launch {
            _processingState.value = ProcessingState.Processing(0f, "Preparing documents...")
            try {
                val outputFile = pdfEngine.mergePdfs(
                    pdfUris = pdfs.map { it.uri },
                    onProgress = { progress, step ->
                        _processingState.value = ProcessingState.Processing(progress, step)
                    }
                )

                val originalTotal = pdfs.sumOf { it.sizeBytes }
                val resultData = ResultData(
                    uri = Uri.fromFile(outputFile),
                    filePath = outputFile.absolutePath,
                    fileName = outputFile.name,
                    toolType = ToolType.MERGE_PDF,
                    originalSizeBytes = originalTotal,
                    outputSizeBytes = outputFile.length(),
                    pageCount = pdfs.size
                )

                repository.addRecentFile(
                    RecentFile(
                        id = UUID.randomUUID().toString(),
                        name = outputFile.name,
                        filePath = outputFile.absolutePath,
                        sizeBytes = outputFile.length(),
                        timestamp = System.currentTimeMillis(),
                        toolType = ToolType.MERGE_PDF,
                        pageCount = pdfs.size
                    )
                )

                _processingState.value = ProcessingState.Success(resultData)
                onSuccess(resultData)
            } catch (e: Exception) {
                _processingState.value = ProcessingState.Error(e.localizedMessage ?: "Failed to merge PDF files")
            }
        }
    }

    fun cancelProcessing() {
        processJob?.cancel()
        _processingState.value = ProcessingState.Idle
    }

    fun resetState() {
        _selectedPdfs.value = emptyList()
        _processingState.value = ProcessingState.Idle
    }
}
