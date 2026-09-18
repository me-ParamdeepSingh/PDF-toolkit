import SwiftUI

enum ToolType: String, CaseIterable, Identifiable, Codable {
    case imageToPdf = "image_to_pdf"
    case mergePdf = "merge_pdf"
    case splitPdf = "split_pdf"
    case compressPdf = "compress_pdf"
    case pdfToImage = "pdf_to_image"

    var id: String { rawValue }

    var title: String {
        switch self {
        case .imageToPdf: return "Image to PDF"
        case .mergePdf: return "Merge PDFs"
        case .splitPdf: return "Split PDF"
        case .compressPdf: return "Compress PDF"
        case .pdfToImage: return "PDF to Images"
        }
    }

    var description: String {
        switch self {
        case .imageToPdf: return "Convert photos & gallery images into a clean PDF"
        case .mergePdf: return "Combine 2 or more PDF documents into a single file"
        case .splitPdf: return "Extract selected pages or custom ranges into a new PDF"
        case .compressPdf: return "Shrink PDF file size while keeping visual clarity crisp"
        case .pdfToImage: return "Export individual PDF pages as high quality JPG or PNG"
        }
    }

    var iconName: String {
        switch self {
        case .imageToPdf: return "photo.on.rectangle.angled"
        case .mergePdf: return "doc.on.doc"
        case .splitPdf: return "arrow.triangle.branch"
        case .compressPdf: return "arrow.down.right.and.arrow.up.left"
        case .pdfToImage: return "photo.stack"
        }
    }
}
