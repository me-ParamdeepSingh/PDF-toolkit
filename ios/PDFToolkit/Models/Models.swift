import Foundation
import UIKit

enum CompressionLevel: String, CaseIterable, Identifiable {
    case low
    case medium
    case high

    var id: String { rawValue }

    var label: String {
        switch self {
        case .low: return "Low Compression"
        case .medium: return "Medium Compression"
        case .high: return "High Compression"
        }
    }

    var description: String {
        switch self {
        case .low: return "High quality, slight size reduction (~20-30%)"
        case .medium: return "Balanced quality & size reduction (~50-60%)"
        case .high: return "Smallest size, maximum shrink (~70-80%)"
        }
    }

    var estimatedReductionPercent: Int {
        switch self {
        case .low: return 25
        case .medium: return 55
        case .high: return 75
        }
    }

    var jpegCompressionQuality: CGFloat {
        switch self {
        case .low: return 0.80
        case .medium: return 0.60
        case .high: return 0.40
        }
    }

    var maxDimension: CGFloat {
        switch self {
        case .low: return 1800
        case .medium: return 1200
        case .high: return 800
        }
    }

    func estimateOutputSize(originalSizeBytes: Int64) -> Int64 {
        let ratio = Double(100 - estimatedReductionPercent) / 100.0
        return max(1024, Int64(Double(originalSizeBytes) * ratio))
    }
}

enum PageSizeOption: String, CaseIterable, Identifiable {
    case a4
    case letter
    case fitToImage

    var id: String { rawValue }

    var label: String {
        switch self {
        case .a4: return "A4 (210 × 297 mm)"
        case .letter: return "US Letter (8.5 × 11 in)"
        case .fitToImage: return "Fit to Image Size"
        }
    }

    var points: CGSize {
        switch self {
        case .a4: return CGSize(width: 595, height: 842)
        case .letter: return CGSize(width: 612, height: 792)
        case .fitToImage: return .zero
        }
    }
}

enum PageOrientation: String, CaseIterable, Identifiable {
    case auto
    case portrait
    case landscape

    var id: String { rawValue }

    var label: String {
        switch self {
        case .auto: return "Auto Detect"
        case .portrait: return "Portrait"
        case .landscape: return "Landscape"
        }
    }
}

enum ImageQuality: String, CaseIterable, Identifiable {
    case standard
    case high

    var id: String { rawValue }

    var label: String {
        switch self {
        case .standard: return "Standard (150 DPI JPG)"
        case .high: return "High Quality (300 DPI PNG)"
        }
    }

    var scaleFactor: CGFloat {
        switch self {
        case .standard: return 1.5
        case .high: return 2.5
        }
    }

    var fileExtension: String {
        switch self {
        case .standard: return "jpg"
        case .high: return "png"
        }
    }
}

struct PdfPageInfo: Identifiable {
    let id = UUID()
    let pageIndex: Int
    let pageNumber: Int
    let thumbnail: UIImage?
    var isSelected: Bool = true
}

enum ProcessingState: Equatable {
    case idle
    case processing(progress: Double, step: String)
    case success(ResultData)
    case error(String)
}

struct ResultData: Identifiable, Equatable {
    let id = UUID()
    let fileUrl: URL
    let fileName: String
    let toolType: ToolType
    let originalSizeBytes: Int64
    let outputSizeBytes: Int64
    let pageCount: Int
    let outputImageUrls: [URL]

    var isMultiImage: Bool {
        !outputImageUrls.isEmpty
    }

    var formattedOriginalSize: String {
        RecentFile.formatBytes(originalSizeBytes)
    }

    var formattedOutputSize: String {
        RecentFile.formatBytes(outputSizeBytes)
    }

    var compressionSavingsText: String? {
        if originalSizeBytes > outputSizeBytes && originalSizeBytes > 0 {
            let saved = originalSizeBytes - outputSizeBytes
            let percent = Int((Double(saved) / Double(originalSizeBytes)) * 100)
            return "Saved \(RecentFile.formatBytes(saved)) (\(percent)% smaller)"
        }
        return nil
    }

    static func == (lhs: ResultData, rhs: ResultData) -> Bool {
        lhs.id == rhs.id
    }
}
