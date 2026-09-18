import SwiftUI
import QuickLook

struct ResultView: View {
    let result: ResultData
    let onDone: () -> Void

    @State private var isShowingShareSheet = false
    @State private var isShowingQuickLook = false

    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                ScrollView {
                    VStack(spacing: AppSpacing.space4) {
                        Spacer(minLength: 20)

                        // Success Badge
                        ZStack {
                            Circle()
                                .fill(Color.appSuccess.opacity(0.12))
                                .frame(width: 72, height: 72)
                            Image(systemName: "checkmark")
                                .font(.system(size: 32, weight: .bold))
                                .foregroundColor(Color.appSuccess)
                        }

                        Text("Processing Complete!")
                            .font(AppTypography.title)
                            .foregroundColor(Color.appTextPrimary)

                        Text("Your document has been generated and saved locally.")
                            .font(AppTypography.body)
                            .foregroundColor(Color.appTextMuted)
                            .multilineTextAlignment(.center)

                        // Details Card
                        VStack(spacing: AppSpacing.space3) {
                            HStack {
                                Text("Operation")
                                    .font(AppTypography.caption)
                                    .foregroundColor(Color.appTextMuted)
                                Spacer()
                                Text(result.toolType.title)
                                    .font(AppTypography.caption)
                                    .bold()
                                    .foregroundColor(Color.appTextPrimary)
                            }

                            Divider()

                            HStack {
                                Text("File Name")
                                    .font(AppTypography.caption)
                                    .foregroundColor(Color.appTextMuted)
                                Spacer()
                                Text(result.fileName)
                                    .font(AppTypography.caption)
                                    .bold()
                                    .foregroundColor(Color.appTextPrimary)
                                    .lineLimit(1)
                            }

                            Divider()

                            if result.originalSizeBytes > 0 && result.toolType == .compressPdf {
                                HStack {
                                    Text("Original Size")
                                        .font(AppTypography.caption)
                                        .foregroundColor(Color.appTextMuted)
                                    Spacer()
                                    Text(result.formattedOriginalSize)
                                        .font(AppTypography.caption)
                                        .foregroundColor(Color.appTextMuted)
                                }

                                Divider()

                                HStack {
                                    Text("Compressed Size")
                                        .font(AppTypography.caption)
                                        .foregroundColor(Color.appTextMuted)
                                    Spacer()
                                    Text(result.formattedOutputSize)
                                        .font(AppTypography.body)
                                        .bold()
                                        .foregroundColor(Color.appSuccess)
                                }

                                if let savings = result.compressionSavingsText {
                                    HStack {
                                        Spacer()
                                        Text(savings)
                                            .font(AppTypography.caption)
                                            .bold()
                                            .foregroundColor(Color.appSuccess)
                                    }
                                }
                            } else {
                                HStack {
                                    Text("Output Size")
                                        .font(AppTypography.caption)
                                        .foregroundColor(Color.appTextMuted)
                                    Spacer()
                                    Text(result.formattedOutputSize)
                                        .font(AppTypography.body)
                                        .bold()
                                        .foregroundColor(Color.appTextPrimary)
                                }
                            }
                        }
                        .padding(AppSpacing.space4)
                        .background(Color.appSurface)
                        .cornerRadius(AppShapes.cardRadius)
                        .overlay(
                            RoundedRectangle(cornerRadius: AppShapes.cardRadius)
                                .stroke(Color.appBorder.opacity(0.6), lineWidth: 1)
                        )
                        .padding(.horizontal, AppSpacing.space4)

                        // Action Buttons: Share & Open
                        HStack(spacing: AppSpacing.space3) {
                            Button(action: { isShowingShareSheet = true }) {
                                HStack {
                                    Image(systemName: "square.and.arrow.up")
                                    Text(result.isMultiImage ? "Share All" : "Share")
                                }
                                .font(AppTypography.bodyLarge)
                                .bold()
                                .foregroundColor(Color.appPrimary)
                                .frame(maxWidth: .infinity)
                                .frame(height: 48)
                                .background(Color.appSurface)
                                .cornerRadius(AppShapes.buttonRadius)
                                .overlay(
                                    RoundedRectangle(cornerRadius: AppShapes.buttonRadius)
                                        .stroke(Color.appBorder, lineWidth: 1)
                                )
                            }

                            Button(action: { isShowingQuickLook = true }) {
                                HStack {
                                    Image(systemName: "arrow.up.right.square")
                                    Text("Open")
                                }
                                .font(AppTypography.bodyLarge)
                                .bold()
                                .foregroundColor(Color.appPrimary)
                                .frame(maxWidth: .infinity)
                                .frame(height: 48)
                                .background(Color.appPrimaryLight)
                                .cornerRadius(AppShapes.buttonRadius)
                            }
                        }
                        .padding(.horizontal, AppSpacing.space4)
                        .padding(.top, AppSpacing.space2)
                    }
                    .padding(.bottom, AppSpacing.space6)
                }

                // Bottom Done Button
                VStack {
                    Divider()
                    Button(action: onDone) {
                        HStack {
                            Image(systemName: "house.fill")
                            Text("Done (Return Home)")
                        }
                        .font(AppTypography.bodyLarge)
                        .bold()
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(Color.appPrimary)
                        .cornerRadius(AppShapes.buttonRadius)
                        .padding(AppSpacing.space4)
                    }
                }
                .background(Color.appSurface)
            }
        }
        .sheet(isPresented: $isShowingShareSheet) {
            if result.isMultiImage {
                ActivityView(activityItems: result.outputImageUrls)
            } else {
                ActivityView(activityItems: [result.fileUrl])
            }
        }
        .quickLookPreview($isShowingQuickLook, url: result.fileUrl)
    }
}

// Helper QuickLook modifier
extension View {
    func quickLookPreview(_ isPresented: Binding<Bool>, url: URL) -> some View {
        self.sheet(isPresented: isPresented) {
            QuickLookControllerWrapper(url: url)
        }
    }
}

struct QuickLookControllerWrapper: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> UINavigationController {
        let ql = QLPreviewController()
        ql.dataSource = context.coordinator
        let nav = UINavigationController(rootViewController: ql)
        return nav
    }

    func updateUIViewController(_ uiViewController: UINavigationController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, QLPreviewControllerDataSource {
        let parent: QuickLookControllerWrapper
        init(_ parent: QuickLookControllerWrapper) { self.parent = parent }

        func numberOfPreviewItems(in controller: QLPreviewController) -> Int { 1 }

        func previewController(_ controller: QLPreviewController, previewItemAt index: Int) -> QLPreviewItem {
            parent.url as QLPreviewItem
        }
    }
}
