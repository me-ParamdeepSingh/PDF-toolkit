import Foundation
import PDFKit
import UIKit

final class PDFEngine {
    static let shared = PDFEngine()

    private var outputDirectory: URL {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        let dir = paths[0].appendingPathComponent("ProcessedPDFs", isDirectory: true)
        if !FileManager.default.fileExists(atPath: dir.path) {
            try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        }
        return dir
    }

    private var imagesOutputDirectory: URL {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        let dir = paths[0].appendingPathComponent("ExportedImages", isDirectory: true)
        if !FileManager.default.fileExists(atPath: dir.path) {
            try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        }
        return dir
    }

    // MARK: - 1. Images to PDF
    func imagesToPdf(
        images: [UIImage],
        pageSize: PageSizeOption,
        orientation: PageOrientation,
        progress: @escaping (Double, String) -> Void
    ) async throws -> URL {
        let timestamp = ISO8601DateFormatter().string(from: Date()).replacingOccurrences(of: ":", with: "-")
        let outputUrl = outputDirectory.appendingPathComponent("PDFToolkit_Images_\(timestamp).pdf")

        let pdfRenderer = UIGraphicsPDFRenderer(bounds: CGRect.zero)
        let total = images.count

        let data = pdfRenderer.pdfData { context in
            for (index, image) in images.enumerated() {
                let step = "Processing image \(index + 1) of \(total)..."
                let currentProgress = (Double(index) / Double(total)) * 0.9
                progress(currentProgress, step)

                let isLandscape: Bool
                switch orientation {
                case .portrait: isLandscape = false
                case .landscape: isLandscape = true
                case .auto: isLandscape = image.size.width > image.size.height
                }

                var pageWidth: CGFloat
                var pageHeight: CGFloat

                switch pageSize {
                case .a4:
                    pageWidth = isLandscape ? PageSizeOption.a4.points.height : PageSizeOption.a4.points.width
                    pageHeight = isLandscape ? PageSizeOption.a4.points.width : PageSizeOption.a4.points.height
                case .letter:
                    pageWidth = isLandscape ? PageSizeOption.letter.points.height : PageSizeOption.letter.points.width
                    pageHeight = isLandscape ? PageSizeOption.letter.points.width : PageSizeOption.letter.points.height
                case .fitToImage:
                    pageWidth = min(image.size.width, 1200)
                    let ratio = image.size.height / image.size.width
                    pageHeight = pageWidth * ratio
                }

                let pageRect = CGRect(x: 0, y: 0, width: pageWidth, height: pageHeight)
                context.beginPage(withBounds: pageRect, pageInfo: [:])

                let margin: CGFloat = 16
                let destRect = self.calculateFittedRect(
                    srcSize: image.size,
                    destWidth: pageWidth - (margin * 2),
                    destHeight: pageHeight - (margin * 2),
                    margin: margin
                )

                image.draw(in: destRect)
            }
        }

        progress(0.95, "Writing PDF file...")
        try data.write(to: outputUrl)
        progress(1.0, "Completed!")
        return outputUrl
    }

    // MARK: - 2. Merge PDFs
    func mergePdfs(
        urls: [URL],
        progress: @escaping (Double, String) -> Void
    ) async throws -> URL {
        let timestamp = ISO8601DateFormatter().string(from: Date()).replacingOccurrences(of: ":", with: "-")
        let outputUrl = outputDirectory.appendingPathComponent("PDFToolkit_Merged_\(timestamp).pdf")

        let mergedDoc = PDFDocument()
        let total = urls.count
        var pageIndexCounter = 0

        for (i, url) in urls.enumerated() {
            let step = "Adding PDF document \(i + 1) of \(total)..."
            let currentProgress = (Double(i) / Double(total)) * 0.8
            progress(currentProgress, step)

            guard let sourceDoc = PDFDocument(url: url) else { continue }
            for p in 0..<sourceDoc.pageCount {
                if let page = sourceDoc.page(at: p) {
                    mergedDoc.insert(page, at: pageIndexCounter)
                    pageIndexCounter += 1
                }
            }
        }

        progress(0.9, "Saving merged document...")
        mergedDoc.write(to: outputUrl)
        progress(1.0, "Completed!")
        return outputUrl
    }

    // MARK: - 3. Split PDF
    func splitPdf(
        url: URL,
        selectedPages: Set<Int>, // 1-indexed
        progress: @escaping (Double, String) -> Void
    ) async throws -> URL {
        guard let sourceDoc = PDFDocument(url: url) else {
            throw NSError(domain: "PDFEngine", code: 404, userInfo: [NSLocalizedDescriptionKey: "Cannot open PDF document."])
        }

        let timestamp = ISO8601DateFormatter().string(from: Date()).replacingOccurrences(of: ":", with: "-")
        let outputUrl = outputDirectory.appendingPathComponent("PDFToolkit_Split_\(timestamp).pdf")
        let newDoc = PDFDocument()

        let sortedPages = selectedPages.sorted()
        let total = sortedPages.count

        for (i, pageNum) in sortedPages.enumerated() {
            let zeroIndex = pageNum - 1
            if zeroIndex >= 0 && zeroIndex < sourceDoc.pageCount, let page = sourceDoc.page(at: zeroIndex) {
                newDoc.insert(page, at: i)
            }
            let currentProgress = 0.1 + (Double(i) / Double(total)) * 0.8
            progress(currentProgress, "Extracting page \(pageNum) of \(total)...")
        }

        progress(0.95, "Writing PDF file...")
        newDoc.write(to: outputUrl)
        progress(1.0, "Completed!")
        return outputUrl
    }

    // MARK: - 4. Compress PDF
    func compressPdf(
        url: URL,
        level: CompressionLevel,
        progress: @escaping (Double, String) -> Void
    ) async throws -> URL {
        guard let sourceDoc = PDFDocument(url: url) else {
            throw NSError(domain: "PDFEngine", code: 404, userInfo: [NSLocalizedDescriptionKey: "Cannot open PDF document."])
        }

        let timestamp = ISO8601DateFormatter().string(from: Date()).replacingOccurrences(of: ":", with: "-")
        let outputUrl = outputDirectory.appendingPathComponent("PDFToolkit_Compressed_\(timestamp).pdf")
        let compressedDoc = PDFDocument()

        let pageCount = sourceDoc.pageCount

        for i in 0..<pageCount {
            let step = "Compressing page \(i + 1) of \(pageCount)..."
            let currentProgress = (Double(i) / Double(pageCount)) * 0.85
            progress(currentProgress, step)

            guard let page = sourceDoc.page(at: i) else { continue }
            let bounds = page.bounds(for: .mediaBox)

            // Render page to image at scaled quality
            let maxDim = level.maxDimension
            let scale = bounds.width > bounds.height
                ? (bounds.width > maxDim ? maxDim / bounds.width : 1.0)
                : (bounds.height > maxDim ? maxDim / bounds.height : 1.0)

            let renderSize = CGSize(width: max(100, bounds.width * scale), height: max(100, bounds.height * scale))
            let pageImage = page.thumbnail(of: renderSize, for: .mediaBox)

            // Compress as JPEG
            if let jpegData = pageImage.jpegData(compressionQuality: level.jpegCompressionQuality),
               let compressedImage = UIImage(data: jpegData),
               let newPage = PDFPage(image: compressedImage) {
                newPage.setBounds(bounds, for: .mediaBox)
                compressedDoc.insert(newPage, at: i)
            } else {
                compressedDoc.insert(page, at: i)
            }
        }

        progress(0.92, "Building compressed document...")
        compressedDoc.write(to: outputUrl)
        progress(1.0, "Completed!")
        return outputUrl
    }

    // MARK: - 5. PDF to Images
    func pdfToImages(
        url: URL,
        quality: ImageQuality,
        progress: @escaping (Double, String) -> Void
    ) async throws -> [URL] {
        guard let sourceDoc = PDFDocument(url: url) else {
            throw NSError(domain: "PDFEngine", code: 404, userInfo: [NSLocalizedDescriptionKey: "Cannot open PDF document."])
        }

        var outputFiles = [URL]()
        let total = sourceDoc.pageCount

        for i in 0..<total {
            let step = "Exporting page \(i + 1) of \(total) to image..."
            let currentProgress = (Double(i) / Double(total)) * 0.9
            progress(currentProgress, step)

            guard let page = sourceDoc.page(at: i) else { continue }
            let bounds = page.bounds(for: .mediaBox)
            let renderSize = CGSize(width: bounds.width * quality.scaleFactor, height: bounds.height * quality.scaleFactor)
            let image = page.thumbnail(of: renderSize, for: .mediaBox)

            let fileUrl = imagesOutputDirectory.appendingPathComponent("page_\(i + 1)_\(Int(Date().timeIntervalSince1970)).\(quality.fileExtension)")

            if quality == .high {
                if let pngData = image.pngData() {
                    try pngData.write(to: fileUrl)
                    outputFiles.append(fileUrl)
                }
            } else {
                if let jpgData = image.jpegData(compressionQuality: 0.9) {
                    try jpgData.write(to: fileUrl)
                    outputFiles.append(fileUrl)
                }
            }
        }

        progress(1.0, "Completed!")
        return outputFiles
    }

    // MARK: - Page Thumbnails for Split
    func getPageThumbnails(url: URL) -> [PdfPageInfo] {
        guard let doc = PDFDocument(url: url) else { return [] }
        var result = [PdfPageInfo]()
        let maxPages = min(doc.pageCount, 100)

        for i in 0..<maxPages {
            if let page = doc.page(at: i) {
                let thumb = page.thumbnail(of: CGSize(width: 140, height: 180), for: .mediaBox)
                result.append(PdfPageInfo(pageIndex: i, pageNumber: i + 1, thumbnail: thumb, isSelected: true))
            }
        }
        return result
    }

    func getFileSize(url: URL) -> Int64 {
        let values = try? url.resourceValues(forKeys: [.fileSizeKey])
        return Int64(values?.fileSize ?? 0)
    }

    private func calculateFittedRect(srcSize: CGSize, destWidth: CGFloat, destHeight: CGFloat, margin: CGFloat) -> CGRect {
        let srcRatio = srcSize.width / srcSize.height
        let destRatio = destWidth / destHeight

        var finalWidth: CGFloat
        var finalHeight: CGFloat

        if srcRatio > destRatio {
            finalWidth = destWidth
            finalHeight = destWidth / srcRatio
        } else {
            finalHeight = destHeight
            finalWidth = destHeight * srcRatio
        }

        let left = margin + (destWidth - finalWidth) / 2
        let top = margin + (destHeight - finalHeight) / 2

        return CGRect(x: left, y: top, width: finalWidth, height: finalHeight)
    }
}
