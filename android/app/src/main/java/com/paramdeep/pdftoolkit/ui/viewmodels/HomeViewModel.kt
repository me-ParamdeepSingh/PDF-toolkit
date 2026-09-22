package com.paramdeep.pdftoolkit.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.paramdeep.pdftoolkit.data.model.RecentFile
import com.paramdeep.pdftoolkit.data.model.ToolType
import com.paramdeep.pdftoolkit.data.repository.RecentFilesRepository
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RecentFilesRepository(application.applicationContext)

    val recentFiles: StateFlow<List<RecentFile>> = repository.recentFiles
    val tools: List<ToolType> = ToolType.values().toList()

    fun removeRecentFile(filePath: String) {
        repository.removeRecentFile(filePath)
    }
}
