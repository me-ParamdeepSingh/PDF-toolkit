import Foundation

struct RecentFile: Identifiable, Codable {
    let id: String
    let name: String
    let fileUrlString: String
    let sizeBytes: Int64
    let timestamp: Double
    let toolType: ToolType
    let pageCount: Int

    var fileUrl: URL? {
        URL(string: fileUrlString)
    }

    var formattedSize: String {
        Self.formatBytes(sizeBytes)
    }

    static func formatBytes(_ bytes: Int64) -> String {
        if bytes <= 0 { return "0 B" }
        let units = ["B", "KB", "MB", "GB"]
        let digitGroups = Int(log10(Double(bytes)) / log10(1024.0))
        let unitIndex = max(0, min(digitGroups, units.count - 1))
        let value = Double(bytes) / pow(1024.0, Double(unitIndex))
        return String(format: "%.1f %@", value, units[unitIndex])
    }
}
