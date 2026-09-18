import Foundation
import Combine

final class RecentFilesStore: ObservableObject {
    static let shared = RecentFilesStore()
    private let userDefaultsKey = "pdf_toolkit_recent_files"

    @Published var recentFiles: [RecentFile] = []

    init() {
        loadRecentFiles()
    }

    func loadRecentFiles() {
        guard let data = UserDefaults.standard.data(forKey: userDefaultsKey),
              let list = try? JSONDecoder().decode([RecentFile].self, from: data) else {
            self.recentFiles = []
            return
        }
        // Filter out files that no longer exist
        self.recentFiles = list.filter { file in
            guard let url = file.fileUrl else { return false }
            return FileManager.default.fileExists(atPath: url.path)
        }.prefix(3).map { $0 }
    }

    func addRecentFile(_ file: RecentFile) {
        var current = recentFiles.filter { $0.fileUrlString != file.fileUrlString }
        current.insert(file, at: 0)
        let trimmed = Array(current.prefix(3))
        self.recentFiles = trimmed
        save(trimmed)
    }

    func removeRecentFile(id: String) {
        let updated = recentFiles.filter { $0.id != id }
        self.recentFiles = updated
        save(updated)
    }

    private func save(_ list: [RecentFile]) {
        if let data = try? JSONEncoder().encode(list) {
            UserDefaults.standard.set(data, forKey: userDefaultsKey)
        }
    }
}
