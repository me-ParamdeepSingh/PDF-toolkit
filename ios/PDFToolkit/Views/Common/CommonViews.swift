import SwiftUI
import PhotosUI
import QuickLook

// MARK: - Determinate Progress View
struct DeterminateProgressView: View {
    let progress: Double
    let statusText: String
    var onCancel: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: AppSpacing.space4) {
            HStack {
                Text(statusText)
                    .font(AppTypography.bodyLarge)
                    .foregroundColor(Color.appTextPrimary)
                    .lineLimit(1)
                Spacer()
                Text("\(Int(progress * 100))%")
                    .font(AppTypography.bodyLarge)
                    .bold()
                    .foregroundColor(Color.appPrimary)
            }

            ProgressView(value: min(max(progress, 0.0), 1.0))
                .progressViewStyle(LinearProgressViewStyle(tint: Color.appPrimary))
                .scaleEffect(x: 1, y: 2, anchor: .center)
                .clipShape(RoundedRectangle(cornerRadius: 4))

            if let onCancel = onCancel {
                Button(action: onCancel) {
                    Text("Cancel")
                        .font(AppTypography.body)
                        .bold()
                        .foregroundColor(Color.appDanger)
                        .padding(.top, AppSpacing.space2)
                }
            }
        }
        .padding(AppSpacing.space4)
        .background(Color.appSurface)
        .cornerRadius(AppShapes.cardRadius)
        .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 2)
    }
}

// MARK: - Document Picker (PDF)
struct DocumentPicker: UIViewControllerRepresentable {
    let allowsMultipleSelection: Bool
    let onDocumentsPicked: ([URL]) -> Void

    func makeUIViewController(context: Context) -> UIDocumentPickerViewController {
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.pdf], asCopy: true)
        picker.allowsMultipleSelection = allowsMultipleSelection
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIDocumentPickerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, UIDocumentPickerDelegate {
        let parent: DocumentPicker
        init(_ parent: DocumentPicker) { self.parent = parent }

        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            parent.onDocumentsPicked(urls)
        }
    }
}

// MARK: - Image Picker (PHPicker)
struct MultiImagePicker: UIViewControllerRepresentable {
    let maxSelectionCount: Int
    let onImagesPicked: ([UIImage]) -> Void

    func makeUIViewController(context: Context) -> PHPickerViewController {
        var config = PHPickerConfiguration()
        config.filter = .images
        config.selectionLimit = maxSelectionCount
        let picker = PHPickerViewController(configuration: config)
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, PHPickerViewControllerDelegate {
        let parent: MultiImagePicker
        init(_ parent: MultiImagePicker) { self.parent = parent }

        func picker(_ controller: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            controller.dismiss(animated: true)
            guard !results.isEmpty else { return }

            var images: [UIImage] = []
            let group = DispatchGroup()

            for result in results {
                if result.itemProvider.canLoadObject(ofClass: UIImage.self) {
                    group.enter()
                    result.itemProvider.loadObject(ofClass: UIImage.self) { image, _ in
                        if let uiImage = image as? UIImage {
                            DispatchQueue.main.async {
                                images.append(uiImage)
                            }
                        }
                        group.leave()
                    }
                }
            }

            group.notify(queue: .main) {
                self.parent.onImagesPicked(images)
            }
        }
    }
}

// MARK: - Activity Share View
struct ActivityView: UIViewControllerRepresentable {
    let activityItems: [Any]
    let applicationActivities: [UIActivity]? = nil

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: applicationActivities)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

// MARK: - Home Banner Ad Placeholder / AdMob View
struct BannerAdView: View {
    var body: some View {
        HStack {
            Spacer()
            VStack(spacing: 2) {
                Text("ADVERTISEMENT")
                    .font(AppTypography.tiny)
                    .foregroundColor(Color.appTextMuted)
                Text("Google AdMob Banner")
                    .font(AppTypography.caption)
                    .foregroundColor(Color.appTextMuted)
            }
            Spacer()
        }
        .frame(height: 50)
        .background(Color.appSurfaceSubtle.opacity(0.6))
        .cornerRadius(AppShapes.buttonRadius)
        .padding(.horizontal, AppSpacing.space4)
        .padding(.vertical, AppSpacing.space2)
    }
}
