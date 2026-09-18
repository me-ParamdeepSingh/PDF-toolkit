import SwiftUI

// MARK: - 1. Image to PDF View
struct ImageToPdfView: View {
    @StateObject private var viewModel = ImageToPdfViewModel()
    @State private var isShowingPicker = false
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                if case .processing(let progress, let step) = viewModel.processingState {
                    VStack {
                        Spacer()
                        DeterminateProgressView(progress: progress, statusText: step)
                            .padding(AppSpacing.space4)
                        Spacer()
                    }
                } else if viewModel.selectedImages.isEmpty {
                    // Empty State
                    VStack(spacing: AppSpacing.space4) {
                        Spacer()
                        ZStack {
                            Circle()
                                .fill(Color.appPrimaryLight)
                                .frame(width: 80, height: 80)
                            Image(systemName: "photo.on.rectangle.angled")
                                .font(.system(size: 36))
                                .foregroundColor(Color.appPrimary)
                        }

                        Text("Select Images to Convert")
                            .font(AppTypography.title)
                            .foregroundColor(Color.appTextPrimary)

                        Text("Choose one or more photos from your device.\nYou can reorder them before converting.")
                            .font(AppTypography.body)
                            .foregroundColor(Color.appTextMuted)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, AppSpacing.space6)

                        Button(action: { isShowingPicker = true }) {
                            HStack {
                                Image(systemName: "plus")
                                Text("Pick Images")
                            }
                            .font(AppTypography.bodyLarge)
                            .bold()
                            .foregroundColor(.white)
                            .padding(.horizontal, AppSpacing.space6)
                            .padding(.vertical, AppSpacing.space3)
                            .background(Color.appPrimary)
                            .cornerRadius(AppShapes.buttonRadius)
                        }
                        .padding(.top, AppSpacing.space4)

                        Spacer()
                    }
                } else {
                    // Controls + Grid
                    ScrollView {
                        VStack(alignment: .leading, spacing: AppSpacing.space4) {
                            // Options
                            VStack(alignment: .leading, spacing: AppSpacing.space2) {
                                Text("Page Size")
                                    .font(AppTypography.caption)
                                    .bold()
                                    .foregroundColor(Color.appTextMuted)

                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: AppSpacing.space2) {
                                        ForEach(PageSizeOption.allCases) { opt in
                                            Button(action: { viewModel.pageSize = opt }) {
                                                Text(opt.label)
                                                    .font(AppTypography.caption)
                                                    .padding(.horizontal, 12)
                                                    .padding(.vertical, 6)
                                                    .background(viewModel.pageSize == opt ? Color.appPrimaryLight : Color.appSurface)
                                                    .foregroundColor(viewModel.pageSize == opt ? Color.appPrimary : Color.appTextPrimary)
                                                    .cornerRadius(AppShapes.pillRadius)
                                                    .overlay(
                                                        RoundedRectangle(cornerRadius: AppShapes.pillRadius)
                                                            .stroke(viewModel.pageSize == opt ? Color.appPrimary : Color.appBorder, lineWidth: 1)
                                                    )
                                            }
                                        }
                                    }
                                }

                                Text("Orientation")
                                    .font(AppTypography.caption)
                                    .bold()
                                    .foregroundColor(Color.appTextMuted)
                                    .padding(.top, AppSpacing.space1)

                                HStack(spacing: AppSpacing.space2) {
                                    ForEach(PageOrientation.allCases) { opt in
                                        Button(action: { viewModel.orientation = opt }) {
                                            Text(opt.label)
                                                .font(AppTypography.caption)
                                                .padding(.horizontal, 12)
                                                .padding(.vertical, 6)
                                                .background(viewModel.orientation == opt ? Color.appPrimaryLight : Color.appSurface)
                                                .foregroundColor(viewModel.orientation == opt ? Color.appPrimary : Color.appTextPrimary)
                                                .cornerRadius(AppShapes.pillRadius)
                                                .overlay(
                                                    RoundedRectangle(cornerRadius: AppShapes.pillRadius)
                                                        .stroke(viewModel.orientation == opt ? Color.appPrimary : Color.appBorder, lineWidth: 1)
                                                )
                                        }
                                    }
                                }
                            }
                            .padding(.horizontal, AppSpacing.space4)

                            HStack {
                                Text("Pages (\(viewModel.selectedImages.count))")
                                    .font(AppTypography.headline)
                                    .foregroundColor(Color.appTextPrimary)
                                Spacer()
                                Button(action: { isShowingPicker = true }) {
                                    HStack(spacing: 4) {
                                        Image(systemName: "plus")
                                        Text("Add More")
                                    }
                                    .font(AppTypography.caption)
                                    .foregroundColor(Color.appPrimary)
                                }
                            }
                            .padding(.horizontal, AppSpacing.space4)

                            // Image Grid
                            LazyVGrid(columns: [GridItem(.adaptive(minimum: 100), spacing: 12)], spacing: 12) {
                                ForEach(Array(viewModel.selectedImages.enumerated()), id: \.offset) { index, img in
                                    ZStack(alignment: .topTrailing) {
                                        Image(uiImage: img)
                                            .resizable()
                                            .scaledToFill()
                                            .frame(width: 100, height: 130)
                                            .clipped()
                                            .cornerRadius(AppShapes.buttonRadius)

                                        Button(action: { viewModel.removeImage(at: index) }) {
                                            Image(systemName: "xmark.circle.fill")
                                                .foregroundColor(.white)
                                                .background(Circle().fill(Color.black.opacity(0.6)))
                                        }
                                        .padding(4)

                                        VStack {
                                            Spacer()
                                            HStack {
                                                Text("\(index + 1)")
                                                    .font(AppTypography.tiny)
                                                    .bold()
                                                    .foregroundColor(.white)
                                                    .padding(.horizontal, 6)
                                                    .padding(.vertical, 2)
                                                    .background(Color.black.opacity(0.6))
                                                    .cornerRadius(4)
                                                Spacer()
                                            }
                                            .padding(4)
                                        }
                                    }
                                }
                            }
                            .padding(.horizontal, AppSpacing.space4)
                        }
                        .padding(.vertical, AppSpacing.space4)
                    }

                    // Convert Button
                    VStack {
                        Divider()
                        Button(action: { viewModel.convertToPdf() }) {
                            HStack {
                                Image(systemName: "doc.fill")
                                Text("Create PDF (\(viewModel.selectedImages.count) pages)")
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
        }
        .navigationTitle("Images to PDF")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $isShowingPicker) {
            MultiImagePicker(maxSelectionCount: 50) { images in
                viewModel.addImages(images)
            }
        }
        .sheet(item: $viewModel.activeResult) { result in
            ResultView(result: result) {
                viewModel.reset()
                presentationMode.wrappedValue.dismiss()
            }
        }
    }
}

// MARK: - 2. Merge PDFs View
struct MergePdfView: View {
    @StateObject private var viewModel = MergePdfViewModel()
    @State private var isShowingPicker = false
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                if case .processing(let progress, let step) = viewModel.processingState {
                    VStack {
                        Spacer()
                        DeterminateProgressView(progress: progress, statusText: step)
                            .padding(AppSpacing.space4)
                        Spacer()
                    }
                } else if viewModel.selectedPdfs.isEmpty {
                    VStack(spacing: AppSpacing.space4) {
                        Spacer()
                        ZStack {
                            Circle()
                                .fill(Color.appPrimaryLight)
                                .frame(width: 80, height: 80)
                            Image(systemName: "doc.on.doc")
                                .font(.system(size: 36))
                                .foregroundColor(Color.appPrimary)
                        }

                        Text("Select 2 or More PDFs")
                            .font(AppTypography.title)
                            .foregroundColor(Color.appTextPrimary)

                        Text("Choose existing PDF files from your device.\nYou can change document order before merging.")
                            .font(AppTypography.body)
                            .foregroundColor(Color.appTextMuted)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, AppSpacing.space6)

                        Button(action: { isShowingPicker = true }) {
                            HStack {
                                Image(systemName: "plus")
                                Text("Pick PDF Documents")
                            }
                            .font(AppTypography.bodyLarge)
                            .bold()
                            .foregroundColor(.white)
                            .padding(.horizontal, AppSpacing.space6)
                            .padding(.vertical, AppSpacing.space3)
                            .background(Color.appPrimary)
                            .cornerRadius(AppShapes.buttonRadius)
                        }
                        .padding(.top, AppSpacing.space4)

                        Spacer()
                    }
                } else {
                    List {
                        Section(header: HStack {
                            Text("Documents (\(viewModel.selectedPdfs.count))")
                            Spacer()
                            Button("Add More") { isShowingPicker = true }
                                .foregroundColor(Color.appPrimary)
                        }) {
                            ForEach(Array(viewModel.selectedPdfs.enumerated()), id: \.element.id) { index, doc in
                                HStack(spacing: AppSpacing.space3) {
                                    Text("\(index + 1)")
                                        .font(AppTypography.body)
                                        .bold()
                                        .foregroundColor(Color.appPrimary)
                                        .frame(width: 24)

                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(doc.name)
                                            .font(AppTypography.body)
                                            .bold()
                                            .lineLimit(1)
                                        Text(RecentFile.formatBytes(doc.sizeBytes))
                                            .font(AppTypography.caption)
                                            .foregroundColor(Color.appTextMuted)
                                    }
                                }
                            }
                            .onDelete { indexSet in
                                for index in indexSet { viewModel.removePdf(at: index) }
                            }
                            .onMove { source, destination in
                                viewModel.movePdf(from: source, to: destination)
                            }
                        }
                    }
                    .listStyle(InsetGroupedListStyle())

                    VStack {
                        Divider()
                        Button(action: { viewModel.mergePdfs() }) {
                            HStack {
                                Image(systemName: "doc.on.doc.fill")
                                Text("Merge \(viewModel.selectedPdfs.count) PDFs")
                            }
                            .font(AppTypography.bodyLarge)
                            .bold()
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(viewModel.selectedPdfs.count >= 2 ? Color.appPrimary : Color.appTextMuted)
                            .cornerRadius(AppShapes.buttonRadius)
                            .padding(AppSpacing.space4)
                        }
                        .disabled(viewModel.selectedPdfs.count < 2)
                    }
                    .background(Color.appSurface)
                }
            }
        }
        .navigationTitle("Merge PDFs")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if !viewModel.selectedPdfs.isEmpty {
                EditButton()
            }
        }
        .sheet(isPresented: $isShowingPicker) {
            DocumentPicker(allowsMultipleSelection: true) { urls in
                viewModel.addPdfs(urls: urls)
            }
        }
        .sheet(item: $viewModel.activeResult) { result in
            ResultView(result: result) {
                viewModel.reset()
                presentationMode.wrappedValue.dismiss()
            }
        }
    }
}

// MARK: - 3. Split PDF View
struct SplitPdfView: View {
    @StateObject private var viewModel = SplitPdfViewModel()
    @State private var isShowingPicker = false
    @State private var rangeInput = ""
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                if case .processing(let progress, let step) = viewModel.processingState {
                    VStack {
                        Spacer()
                        DeterminateProgressView(progress: progress, statusText: step)
                            .padding(AppSpacing.space4)
                        Spacer()
                    }
                } else if viewModel.sourceUrl == nil {
                    VStack(spacing: AppSpacing.space4) {
                        Spacer()
                        ZStack {
                            Circle()
                                .fill(Color.appPrimaryLight)
                                .frame(width: 80, height: 80)
                            Image(systemName: "arrow.triangle.branch")
                                .font(.system(size: 36))
                                .foregroundColor(Color.appPrimary)
                        }

                        Text("Select a PDF to Split")
                            .font(AppTypography.title)
                            .foregroundColor(Color.appTextPrimary)

                        Text("Choose a PDF file to view thumbnails.\nSelect individual pages or enter a range to extract.")
                            .font(AppTypography.body)
                            .foregroundColor(Color.appTextMuted)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, AppSpacing.space6)

                        Button(action: { isShowingPicker = true }) {
                            HStack {
                                Image(systemName: "plus")
                                Text("Pick PDF File")
                            }
                            .font(AppTypography.bodyLarge)
                            .bold()
                            .foregroundColor(.white)
                            .padding(.horizontal, AppSpacing.space6)
                            .padding(.vertical, AppSpacing.space3)
                            .background(Color.appPrimary)
                            .cornerRadius(AppShapes.buttonRadius)
                        }
                        .padding(.top, AppSpacing.space4)

                        Spacer()
                    }
                } else if viewModel.isLoadingThumbnails {
                    VStack(spacing: AppSpacing.space3) {
                        Spacer()
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color.appPrimary))
                        Text("Rendering page previews...")
                            .font(AppTypography.body)
                            .foregroundColor(Color.appTextMuted)
                        Spacer()
                    }
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: AppSpacing.space3) {
                            // Header info
                            VStack(alignment: .leading, spacing: 4) {
                                Text(viewModel.sourceName)
                                    .font(AppTypography.bodyLarge)
                                    .bold()
                                    .lineLimit(1)
                                Text("\(viewModel.pages.count) Total Pages • \(viewModel.selectedPages.count) Selected")
                                    .font(AppTypography.caption)
                                    .foregroundColor(Color.appTextMuted)
                            }
                            .padding(.horizontal, AppSpacing.space4)

                            // Actions
                            HStack(spacing: AppSpacing.space2) {
                                Button("Select All") { viewModel.selectAll() }
                                    .font(AppTypography.caption)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.appSurface)
                                    .cornerRadius(AppShapes.buttonRadius)

                                Button("Clear") { viewModel.deselectAll() }
                                    .font(AppTypography.caption)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.appSurface)
                                    .cornerRadius(AppShapes.buttonRadius)

                                Spacer()
                            }
                            .padding(.horizontal, AppSpacing.space4)

                            // Page Grid
                            LazyVGrid(columns: [GridItem(.adaptive(minimum: 95), spacing: 12)], spacing: 12) {
                                ForEach(viewModel.pages) { page in
                                    let isSelected = viewModel.selectedPages.contains(page.pageNumber)
                                    Button(action: { viewModel.togglePage(page.pageNumber) }) {
                                        ZStack(alignment: .topTrailing) {
                                            if let thumb = page.thumbnail {
                                                Image(uiImage: thumb)
                                                    .resizable()
                                                    .scaledToFit()
                                                    .frame(height: 125)
                                                    .background(Color.appSurface)
                                                    .cornerRadius(6)
                                                    .overlay(
                                                        RoundedRectangle(cornerRadius: 6)
                                                            .stroke(isSelected ? Color.appPrimary : Color.appBorder, lineWidth: isSelected ? 2 : 1)
                                                    )
                                            }

                                            // Badge
                                            ZStack {
                                                Circle()
                                                    .fill(isSelected ? Color.appPrimary : Color.appSurfaceSubtle)
                                                    .frame(width: 20, height: 20)
                                                if isSelected {
                                                    Image(systemName: "checkmark")
                                                        .font(.system(size: 10, weight: .bold))
                                                        .foregroundColor(.white)
                                                }
                                            }
                                            .padding(4)
                                        }
                                    }
                                }
                            }
                            .padding(.horizontal, AppSpacing.space4)
                        }
                        .padding(.vertical, AppSpacing.space4)
                    }

                    VStack {
                        Divider()
                        Button(action: { viewModel.splitPdf() }) {
                            HStack {
                                Image(systemName: "arrow.triangle.branch")
                                Text("Extract \(viewModel.selectedPages.count) Pages")
                            }
                            .font(AppTypography.bodyLarge)
                            .bold()
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(viewModel.selectedPages.isEmpty ? Color.appTextMuted : Color.appPrimary)
                            .cornerRadius(AppShapes.buttonRadius)
                            .padding(AppSpacing.space4)
                        }
                        .disabled(viewModel.selectedPages.isEmpty)
                    }
                    .background(Color.appSurface)
                }
            }
        }
        .navigationTitle("Split PDF")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $isShowingPicker) {
            DocumentPicker(allowsMultipleSelection: false) { urls in
                if let url = urls.first { viewModel.setSourcePdf(url: url) }
            }
        }
        .sheet(item: $viewModel.activeResult) { result in
            ResultView(result: result) {
                viewModel.reset()
                presentationMode.wrappedValue.dismiss()
            }
        }
    }
}

// MARK: - 4. Compress PDF View
struct CompressPdfView: View {
    @StateObject private var viewModel = CompressPdfViewModel()
    @State private var isShowingPicker = false
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                if case .processing(let progress, let step) = viewModel.processingState {
                    VStack {
                        Spacer()
                        DeterminateProgressView(progress: progress, statusText: step)
                            .padding(AppSpacing.space4)
                        Spacer()
                    }
                } else if viewModel.sourceUrl == nil {
                    VStack(spacing: AppSpacing.space4) {
                        Spacer()
                        ZStack {
                            Circle()
                                .fill(Color.appPrimaryLight)
                                .frame(width: 80, height: 80)
                            Image(systemName: "arrow.down.right.and.arrow.up.left")
                                .font(.system(size: 36))
                                .foregroundColor(Color.appPrimary)
                        }

                        Text("Select a PDF to Compress")
                            .font(AppTypography.title)
                            .foregroundColor(Color.appTextPrimary)

                        Text("Reduce file size for emails and web uploads.\nChoose from 3 tailored compression levels.")
                            .font(AppTypography.body)
                            .foregroundColor(Color.appTextMuted)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, AppSpacing.space6)

                        Button(action: { isShowingPicker = true }) {
                            HStack {
                                Image(systemName: "plus")
                                Text("Pick PDF File")
                            }
                            .font(AppTypography.bodyLarge)
                            .bold()
                            .foregroundColor(.white)
                            .padding(.horizontal, AppSpacing.space6)
                            .padding(.vertical, AppSpacing.space3)
                            .background(Color.appPrimary)
                            .cornerRadius(AppShapes.buttonRadius)
                        }
                        .padding(.top, AppSpacing.space4)

                        Spacer()
                    }
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: AppSpacing.space4) {
                            // File card
                            HStack {
                                Image(systemName: "doc.text.fill")
                                    .font(.system(size: 28))
                                    .foregroundColor(Color.appPrimary)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(viewModel.sourceName)
                                        .font(AppTypography.bodyLarge)
                                        .bold()
                                        .lineLimit(1)
                                    Text("Current Size: \(RecentFile.formatBytes(viewModel.originalSizeBytes))")
                                        .font(AppTypography.caption)
                                        .foregroundColor(Color.appTextMuted)
                                }
                                Spacer()
                                Button("Change") { isShowingPicker = true }
                                    .font(AppTypography.caption)
                                    .foregroundColor(Color.appPrimary)
                            }
                            .padding(AppSpacing.space4)
                            .background(Color.appSurface)
                            .cornerRadius(AppShapes.cardRadius)

                            Text("Choose Compression Level")
                                .font(AppTypography.headline)
                                .foregroundColor(Color.appTextPrimary)

                            // 3 Levels
                            VStack(spacing: AppSpacing.space2) {
                                ForEach(CompressionLevel.allCases) { level in
                                    let isSelected = viewModel.compressionLevel == level
                                    let estSize = level.estimateOutputSize(originalSizeBytes: viewModel.originalSizeBytes)

                                    Button(action: { viewModel.compressionLevel = level }) {
                                        HStack {
                                            Image(systemName: isSelected ? "largecircle.fill.circle" : "circle")
                                                .foregroundColor(isSelected ? Color.appPrimary : Color.appTextMuted)

                                            VStack(alignment: .leading, spacing: 2) {
                                                Text(level.label)
                                                    .font(AppTypography.body)
                                                    .bold()
                                                    .foregroundColor(Color.appTextPrimary)
                                                Text(level.description)
                                                    .font(AppTypography.caption)
                                                    .foregroundColor(Color.appTextMuted)
                                            }

                                            Spacer()

                                            VStack(alignment: .trailing) {
                                                Text("~\(RecentFile.formatBytes(estSize))")
                                                    .font(AppTypography.body)
                                                    .bold()
                                                    .foregroundColor(Color.appPrimary)
                                                Text("-\(level.estimatedReductionPercent)%")
                                                    .font(AppTypography.tiny)
                                                    .foregroundColor(Color.appTextMuted)
                                            }
                                        }
                                        .padding(AppSpacing.space4)
                                        .background(isSelected ? Color.appPrimaryLight.opacity(0.4) : Color.appSurface)
                                        .cornerRadius(AppShapes.cardRadius)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: AppShapes.cardRadius)
                                                .stroke(isSelected ? Color.appPrimary : Color.appBorder, lineWidth: isSelected ? 2 : 1)
                                        )
                                    }
                                }
                            }
                        }
                        .padding(AppSpacing.space4)
                    }

                    VStack {
                        Divider()
                        Button(action: { viewModel.compressPdf() }) {
                            HStack {
                                Image(systemName: "arrow.down.right.and.arrow.up.left")
                                Text("Compress Now (~\(RecentFile.formatBytes(viewModel.compressionLevel.estimateOutputSize(originalSizeBytes: viewModel.originalSizeBytes))))")
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
        }
        .navigationTitle("Compress PDF")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $isShowingPicker) {
            DocumentPicker(allowsMultipleSelection: false) { urls in
                if let url = urls.first { viewModel.setSourcePdf(url: url) }
            }
        }
        .sheet(item: $viewModel.activeResult) { result in
            ResultView(result: result) {
                viewModel.reset()
                presentationMode.wrappedValue.dismiss()
            }
        }
    }
}

// MARK: - 5. PDF to Images View
struct PdfToImageView: View {
    @StateObject private var viewModel = PdfToImageViewModel()
    @State private var isShowingPicker = false
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                if case .processing(let progress, let step) = viewModel.processingState {
                    VStack {
                        Spacer()
                        DeterminateProgressView(progress: progress, statusText: step)
                            .padding(AppSpacing.space4)
                        Spacer()
                    }
                } else if viewModel.sourceUrl == nil {
                    VStack(spacing: AppSpacing.space4) {
                        Spacer()
                        ZStack {
                            Circle()
                                .fill(Color.appPrimaryLight)
                                .frame(width: 80, height: 80)
                            Image(systemName: "photo.stack")
                                .font(.system(size: 36))
                                .foregroundColor(Color.appPrimary)
                        }

                        Text("Select a PDF to Export Images")
                            .font(AppTypography.title)
                            .foregroundColor(Color.appTextPrimary)

                        Text("Extract every page as a standalone photo.\nChoose standard JPG or high resolution PNG.")
                            .font(AppTypography.body)
                            .foregroundColor(Color.appTextMuted)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, AppSpacing.space6)

                        Button(action: { isShowingPicker = true }) {
                            HStack {
                                Image(systemName: "plus")
                                Text("Pick PDF File")
                            }
                            .font(AppTypography.bodyLarge)
                            .bold()
                            .foregroundColor(.white)
                            .padding(.horizontal, AppSpacing.space6)
                            .padding(.vertical, AppSpacing.space3)
                            .background(Color.appPrimary)
                            .cornerRadius(AppShapes.buttonRadius)
                        }
                        .padding(.top, AppSpacing.space4)

                        Spacer()
                    }
                } else {
                    VStack(alignment: .leading, spacing: AppSpacing.space4) {
                        HStack {
                            Image(systemName: "doc.text.fill")
                                .font(.system(size: 28))
                                .foregroundColor(Color.appPrimary)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(viewModel.sourceName)
                                    .font(AppTypography.bodyLarge)
                                    .bold()
                                    .lineLimit(1)
                                Text("Ready to export pages")
                                    .font(AppTypography.caption)
                                    .foregroundColor(Color.appTextMuted)
                            }
                            Spacer()
                            Button("Change") { isShowingPicker = true }
                                .font(AppTypography.caption)
                                .foregroundColor(Color.appPrimary)
                        }
                        .padding(AppSpacing.space4)
                        .background(Color.appSurface)
                        .cornerRadius(AppShapes.cardRadius)

                        Text("Output Image Quality")
                            .font(AppTypography.headline)
                            .foregroundColor(Color.appTextPrimary)

                        ForEach(ImageQuality.allCases) { q in
                            let isSelected = viewModel.quality == q
                            Button(action: { viewModel.quality = q }) {
                                HStack {
                                    Image(systemName: isSelected ? "largecircle.fill.circle" : "circle")
                                        .foregroundColor(isSelected ? Color.appPrimary : Color.appTextMuted)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(q.label)
                                            .font(AppTypography.body)
                                            .bold()
                                            .foregroundColor(Color.appTextPrimary)
                                        Text("Outputs .\(q.fileExtension) files")
                                            .font(AppTypography.caption)
                                            .foregroundColor(Color.appTextMuted)
                                    }
                                    Spacer()
                                }
                                .padding(AppSpacing.space4)
                                .background(isSelected ? Color.appPrimaryLight.opacity(0.4) : Color.appSurface)
                                .cornerRadius(AppShapes.cardRadius)
                                .overlay(
                                    RoundedRectangle(cornerRadius: AppShapes.cardRadius)
                                        .stroke(isSelected ? Color.appPrimary : Color.appBorder, lineWidth: isSelected ? 2 : 1)
                                )
                            }
                        }

                        Spacer()
                    }
                    .padding(AppSpacing.space4)

                    VStack {
                        Divider()
                        Button(action: { viewModel.convertPdfToImages() }) {
                            HStack {
                                Image(systemName: "photo.stack")
                                Text("Export Images")
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
        }
        .navigationTitle("PDF to Images")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $isShowingPicker) {
            DocumentPicker(allowsMultipleSelection: false) { urls in
                if let url = urls.first { viewModel.setSourcePdf(url: url) }
            }
        }
        .sheet(item: $viewModel.activeResult) { result in
            ResultView(result: result) {
                viewModel.reset()
                presentationMode.wrappedValue.dismiss()
            }
        }
    }
}
