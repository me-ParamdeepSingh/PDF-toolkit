package com.paramdeep.pdftoolkit.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.paramdeep.pdftoolkit.data.model.RecentFile
import com.paramdeep.pdftoolkit.data.model.ToolType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class RecentFilesRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pdf_toolkit_recent", Context.MODE_PRIVATE)

    private val _recentFiles = MutableStateFlow<List<RecentFile>>(emptyList())
    val recentFiles: StateFlow<List<RecentFile>> = _recentFiles.asStateFlow()

    init {
        loadRecentFiles()
    }

    private fun loadRecentFiles() {
        val jsonString = prefs.getString(KEY_RECENT_FILES, null) ?: return
        try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<RecentFile>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val filePath = obj.optString("filePath")
                val file = File(filePath)
                // Only keep existing files
                if (file.exists()) {
                    list.add(
                        RecentFile(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            filePath = filePath,
                            sizeBytes = file.length(),
                            timestamp = obj.getLong("timestamp"),
                            toolType = ToolType.valueOf(obj.getString("toolType")),
                            pageCount = obj.optInt("pageCount", 1)
                        )
                    )
                }
            }
            _recentFiles.value = list.take(3)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addRecentFile(file: RecentFile) {
        val currentList = _recentFiles.value.filter { it.filePath != file.filePath }.toMutableList()
        currentList.add(0, file)
        val trimmed = currentList.take(3)
        _recentFiles.value = trimmed
        persistRecentFiles(trimmed)
    }

    fun removeRecentFile(filePath: String) {
        val updated = _recentFiles.value.filter { it.filePath != filePath }
        _recentFiles.value = updated
        persistRecentFiles(updated)
    }

    private fun persistRecentFiles(list: List<RecentFile>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("filePath", item.filePath)
                put("sizeBytes", item.sizeBytes)
                put("timestamp", item.timestamp)
                put("toolType", item.toolType.name)
                put("pageCount", item.pageCount)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_RECENT_FILES, jsonArray.toString()).apply()
    }

    companion object {
        private const val KEY_RECENT_FILES = "recent_files_json"
    }
}
