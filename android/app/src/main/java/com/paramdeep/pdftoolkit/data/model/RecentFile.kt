package com.paramdeep.pdftoolkit.data.model

data class RecentFile(
    val id: String,
    val name: String,
    val filePath: String,
    val sizeBytes: Long,
    val timestamp: Long,
    val toolType: ToolType,
    val pageCount: Int = 1
) {
    val formattedSize: String
        get() = formatBytes(sizeBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            val unitIndex = digitGroups.coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, unitIndex.toDouble())
            return String.format("%.1f %s", value, units[unitIndex])
        }
    }
}
