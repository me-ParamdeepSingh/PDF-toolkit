package com.pdftoolkit.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pdftoolkit.app.data.model.RecentFile
import com.pdftoolkit.app.data.model.ToolType
import com.pdftoolkit.app.data.repository.RecentFilesRepository
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RecentFilesRepository(application.applicationContext)

    val recentFiles: StateFlow<List<RecentFile>> = repository.recentFiles
    val tools: List<ToolType> = ToolType.values().toList()

    fun removeRecentFile(filePath: String) {
        repository.removeRecentFile(filePath)
    }
}
