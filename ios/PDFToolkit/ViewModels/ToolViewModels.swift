import SwiftUI
import Combine

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var recentFilesStore = RecentFilesStore.shared
    let tools = ToolType.allCases

    func refreshRecentFiles() {
        recentFilesStore.loadRecentFiles()
    }
}

@MainActor
final class ImageToPdfViewModel: ObservableObject {
    @Published var selectedImages: [UIImage] = []
    @Published var pageSize: PageSizeOption = .a4
    @Published var orientation: PageOrientation = .auto
    @Published var processingState: ProcessingState = .idle
    @Published var activeResult: ResultData? = nil

    private let engine = PDFEngine.shared
    private let recentStore = RecentFilesStore.shared

    func addImages(_ images: [UIImage]) {
        self.selectedImages.append(contentsOf: images)
    }

    func removeImage(at index: Int) {
        if index < selectedImages.count {
            selectedImages.remove(at: index)
        }
    }

    func moveImage(from source: IndexSet, to destination: Int) {
        selectedImages.move(fromOffsets: source, toOffset: destination)
    }

    func convertToPdf() {
        guard !selectedImages.isEmpty else { return }
        processingState = .processing(progress: 0.0, step: "Preparing images...")

        Task {
            do {
                let outputUrl = try await engine.imagesToPdf(
                    images: selectedImages,
                    pageSize: pageSize,
                    orientation: orientation
                ) { progress, step in
                    Task { @MainActor in
                        self.processingState = .processing(progress: progress, step: step)
                    }
                }

                let outputSize = engine.getFileSize(url: outputUrl)
                let result = ResultData(
                    fileUrl: outputUrl,
                    fileName: outputUrl.lastPathComponent,
                    toolType: .imageToPdf,
                    originalSizeBytes: 0,
                    outputSizeBytes: outputSize,
                    pageCount: selectedImages.count,
                    outputImageUrls: []
                )

                recentStore.addRecentFile(
                    RecentFile(
                        id: UUID().uuidString,
                        name: outputUrl.lastPathComponent,
                        fileUrlString: outputUrl.absoluteString,
                        sizeBytes: outputSize,
                        timestamp: Date().timeIntervalSince1970,
                        toolType: .imageToPdf,
                        pageCount: selectedImages.count
                    )
                )

                self.activeResult = result
                self.processingState = .success(result)
            } catch {
                self.processingState = .error(error.localizedDescription)
            }
        }
    }

    func reset() {
        selectedImages = []
        processingState = .idle
        activeResult = nil
    }
}

@MainActor
final class MergePdfViewModel: ObservableObject {
    struct SelectedPdfItem: Identifiable {
        let id = UUID()
        let url: URL
        let name: String
        let sizeBytes: Int64
    }

    @Published var selectedPdfs: [SelectedPdfItem] = []
    @Published var processingState: ProcessingState = .idle
    @Published var activeResult: ResultData? = nil

    private let engine = PDFEngine.shared
    private let recentStore = RecentFilesStore.shared

    func addPdfs(urls: [URL]) {
        for url in urls {
            if !selectedPdfs.contains(where: { $0.url == url }) {
                let size = engine.getFileSize(url: url)
                selectedPdfs.append(SelectedPdfItem(url: url, name: url.lastPathComponent, sizeBytes: size))
            }
        }
    }

    func removePdf(at index: Int) {
        if index < selectedPdfs.count {
            selectedPdfs.remove(at: index)
        }
    }

    func movePdf(from source: IndexSet, to destination: Int) {
        selectedPdfs.move(fromOffsets: source, toOffset: destination)
    }

    func mergePdfs() {
        guard selectedPdfs.count >= 2 else { return }
        processingState = .processing(progress: 0.0, step: "Preparing merge...")

        Task {
            do {
                let urls = selectedPdfs.map { $0.url }
                let outputUrl = try await engine.mergePdfs(urls: urls) { progress, step in
                    Task { @MainActor in
                        self.processingState = .processing(progress: progress, step: step)
                    }
                }

                let totalOriginalSize = selectedPdfs.reduce(0) { $0 + $1.sizeBytes }
                let outputSize = engine.getFileSize(url: outputUrl)

                let result = ResultData(
                    fileUrl: outputUrl,
                    fileName: outputUrl.lastPathComponent,
                    toolType: .mergePdf,
                    originalSizeBytes: totalOriginalSize,
                    outputSizeBytes: outputSize,
                    pageCount: selectedPdfs.count,
                    outputImageUrls: []
                )

                recentStore.addRecentFile(
                    RecentFile(
                        id: UUID().uuidString,
                        name: outputUrl.lastPathComponent,
                        fileUrlString: outputUrl.absoluteString,
                        sizeBytes: outputSize,
                        timestamp: Date().timeIntervalSince1970,
                        toolType: .mergePdf,
                        pageCount: selectedPdfs.count
                    )
                )

                self.activeResult = result
                self.processingState = .success(result)
            } catch {
                self.processingState = .error(error.localizedDescription)
            }
        }
    }

    func reset() {
        selectedPdfs = []
        processingState = .idle
        activeResult = nil
    }
}

@MainActor
final class SplitPdfViewModel: ObservableObject {
    @Published var sourceUrl: URL? = nil
    @Published var sourceName: String = ""
    @Published var pages: [PdfPageInfo] = []
    @Published var selectedPages: Set<Int> = []
    @Published var isLoadingThumbnails: Bool = false
    @Published var processingState: ProcessingState = .idle
    @Published var activeResult: ResultData? = nil

    private let engine = PDFEngine.shared
    private let recentStore = RecentFilesStore.shared

    func setSourcePdf(url: URL) {
        self.sourceUrl = url
        self.sourceName = url.lastPathComponent
        loadThumbnails(url: url)
    }

    private func loadThumbnails(url: URL) {
        isLoadingThumbnails = true
        DispatchQueue.global(qos: .userInitiated).async {
            let loadedPages = self.engine.getPageThumbnails(url: url)
            DispatchQueue.main.async {
                self.pages = loadedPages
                self.selectedPages = Set(loadedPages.map { $0.pageNumber })
                self.isLoadingThumbnails = false
            }
        }
    }

    func togglePage(_ pageNum: Int) {
        if selectedPages.contains(pageNum) {
            selectedPages.remove(pageNum)
        } else {
            selectedPages.insert(pageNum)
        }
    }

    func selectAll() {
        selectedPages = Set(pages.map { $0.pageNumber })
    }

    func deselectAll() {
        selectedPages.removeAll()
    }

    func applyRange(_ text: String) {
        var result = Set<Int>()
        let total = pages.count
        let parts = text.split(separator: ",")
        for part in parts {
            let trimmed = part.trimmingCharacters(in: .whitespaces)
            if trimmed.contains("-") {
                let bounds = trimmed.split(separator: "-")
                let start = Int(bounds[0].trimmingCharacters(in: .whitespaces)) ?? 1
                let end = Int(bounds[1].trimmingCharacters(in: .whitespaces)) ?? total
                for p in max(1, start)...min(total, end) {
                    result.insert(p)
                }
            } else if let p = Int(trimmed), p >= 1 && p <= total {
                result.insert(p)
            }
        }
        if !result.isEmpty {
            self.selectedPages = result
        }
    }

    func splitPdf() {
        guard let url = sourceUrl, !selectedPages.isEmpty else { return }
        processingState = .processing(progress: 0.0, step: "Extracting pages...")

        Task {
            do {
                let outputUrl = try await engine.splitPdf(url: url, selectedPages: selectedPages) { progress, step in
                    Task { @MainActor in
                        self.processingState = .processing(progress: progress, step: step)
                    }
                }

                let outputSize = engine.getFileSize(url: outputUrl)
                let originalSize = engine.getFileSize(url: url)

                let result = ResultData(
                    fileUrl: outputUrl,
                    fileName: outputUrl.lastPathComponent,
                    toolType: .splitPdf,
                    originalSizeBytes: originalSize,
                    outputSizeBytes: outputSize,
                    pageCount: selectedPages.count,
                    outputImageUrls: []
                )

                recentStore.addRecentFile(
                    RecentFile(
                        id: UUID().uuidString,
                        name: outputUrl.lastPathComponent,
                        fileUrlString: outputUrl.absoluteString,
                        sizeBytes: outputSize,
                        timestamp: Date().timeIntervalSince1970,
                        toolType: .splitPdf,
                        pageCount: selectedPages.count
                    )
                )

                self.activeResult = result
                self.processingState = .success(result)
            } catch {
                self.processingState = .error(error.localizedDescription)
            }
        }
    }

    func reset() {
        sourceUrl = nil
        pages = []
        selectedPages = []
        processingState = .idle
        activeResult = nil
    }
}

@MainActor
final class CompressPdfViewModel: ObservableObject {
    @Published var sourceUrl: URL? = nil
    @Published var sourceName: String = ""
    @Published var originalSizeBytes: Int64 = 0
    @Published var compressionLevel: CompressionLevel = .medium
    @Published var processingState: ProcessingState = .idle
    @Published var activeResult: ResultData? = nil

    private let engine = PDFEngine.shared
    private let recentStore = RecentFilesStore.shared

    func setSourcePdf(url: URL) {
        self.sourceUrl = url
        self.sourceName = url.lastPathComponent
        self.originalSizeBytes = engine.getFileSize(url: url)
    }

    func compressPdf() {
        guard let url = sourceUrl else { return }
        processingState = .processing(progress: 0.0, step: "Compressing document...")

        Task {
            do {
                let outputUrl = try await engine.compressPdf(url: url, level: compressionLevel) { progress, step in
                    Task { @MainActor in
                        self.processingState = .processing(progress: progress, step: step)
                    }
                }

                let outputSize = engine.getFileSize(url: outputUrl)
                let result = ResultData(
                    fileUrl: outputUrl,
                    fileName: outputUrl.lastPathComponent,
                    toolType: .compressPdf,
                    originalSizeBytes: originalSizeBytes,
                    outputSizeBytes: outputSize,
                    pageCount: 1,
                    outputImageUrls: []
                )

                recentStore.addRecentFile(
                    RecentFile(
                        id: UUID().uuidString,
                        name: outputUrl.lastPathComponent,
                        fileUrlString: outputUrl.absoluteString,
                        sizeBytes: outputSize,
                        timestamp: Date().timeIntervalSince1970,
                        toolType: .compressPdf,
                        pageCount: 1
                    )
                )

                self.activeResult = result
                self.processingState = .success(result)
            } catch {
                self.processingState = .error(error.localizedDescription)
            }
        }
    }

    func reset() {
        sourceUrl = nil
        originalSizeBytes = 0
        processingState = .idle
        activeResult = nil
    }
}

@MainActor
final class PdfToImageViewModel: ObservableObject {
    @Published var sourceUrl: URL? = nil
    @Published var sourceName: String = ""
    @Published var quality: ImageQuality = .standard
    @Published var processingState: ProcessingState = .idle
    @Published var activeResult: ResultData? = nil

    private let engine = PDFEngine.shared
    private let recentStore = RecentFilesStore.shared

    func setSourcePdf(url: URL) {
        self.sourceUrl = url
        self.sourceName = url.lastPathComponent
    }

    func convertPdfToImages() {
        guard let url = sourceUrl else { return }
        processingState = .processing(progress: 0.0, step: "Exporting pages...")

        Task {
            do {
                let outputUrls = try await engine.pdfToImages(url: url, quality: quality) { progress, step in
                    Task { @MainActor in
                        self.processingState = .processing(progress: progress, step: step)
                    }
                }

                guard let firstUrl = outputUrls.first else {
                    self.processingState = .error("No pages found.")
                    return
                }

                let totalOutputSize = outputUrls.reduce(0) { $0 + engine.getFileSize(url: $1) }
                let originalSize = engine.getFileSize(url: url)

                let result = ResultData(
                    fileUrl: firstUrl,
                    fileName: "\(outputUrls.count) images exported",
                    toolType: .pdfToImage,
                    originalSizeBytes: originalSize,
                    outputSizeBytes: totalOutputSize,
                    pageCount: outputUrls.count,
                    outputImageUrls: outputUrls
                )

                recentStore.addRecentFile(
                    RecentFile(
                        id: UUID().uuidString,
                        name: "\(outputUrls.count) images from \(sourceName)",
                        fileUrlString: firstUrl.absoluteString,
                        sizeBytes: totalOutputSize,
                        timestamp: Date().timeIntervalSince1970,
                        toolType: .pdfToImage,
                        pageCount: outputUrls.count
                    )
                )

                self.activeResult = result
                self.processingState = .success(result)
            } catch {
                self.processingState = .error(error.localizedDescription)
            }
        }
    }

    func reset() {
        sourceUrl = nil
        processingState = .idle
        activeResult = nil
    }
}
