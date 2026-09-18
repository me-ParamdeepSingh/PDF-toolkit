import SwiftUI

struct HomeScreen: View {
    @StateObject private var viewModel = HomeViewModel()
    @State private var selectedTool: ToolType? = nil
    @State private var previewFileUrl: URL? = nil
    @State private var isShowingPreview = false
    @State private var shareFileUrl: URL? = nil
    @State private var isShowingShare = false

    var body: some View {
        NavigationView {
            ZStack {
                Color.appBackground.ignoresSafeArea()

                VStack(spacing: 0) {
                    ScrollView {
                        VStack(alignment: .leading, spacing: AppSpacing.space6) {
                            // Header
                            VStack(alignment: .leading, spacing: AppSpacing.space1) {
                                Text("PDF Toolkit")
                                    .font(AppTypography.display)
                                    .foregroundColor(Color.appTextPrimary)

                                Text("Fast, privacy-friendly on-device PDF utilities")
                                    .font(AppTypography.body)
                                    .foregroundColor(Color.appTextMuted)
                            }
                            .padding(.horizontal, AppSpacing.space4)
                            .padding(.top, AppSpacing.space4)

                            // 2-Column Grid of 5 Tools
                            LazyVGrid(columns: [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)], spacing: 12) {
                                ForEach(viewModel.tools) { tool in
                                    ToolCardView(tool: tool) {
                                        selectedTool = tool
                                    }
                                }
                            }
                            .padding(.horizontal, AppSpacing.space4)

                            // Recent Files Section
                            if !viewModel.recentFilesStore.recentFiles.isEmpty {
                                RecentFilesView(
                                    recentFiles: viewModel.recentFilesStore.recentFiles,
                                    onOpen: { file in
                                        if let url = file.fileUrl {
                                            self.previewFileUrl = url
                                            self.isShowingPreview = true
                                        }
                                    },
                                    onShare: { file in
                                        if let url = file.fileUrl {
                                            self.shareFileUrl = url
                                            self.isShowingShare = true
                                        }
                                    }
                                )
                            }
                        }
                        .padding(.bottom, AppSpacing.space8)
                    }

                    // Bottom Persistent Banner Ad
                    VStack(spacing: 0) {
                        Divider()
                        BannerAdView()
                    }
                    .background(Color.appSurface)
                }

                // Hidden Navigation Links for each tool
                NavigationLink(
                    destination: ImageToPdfView(),
                    tag: ToolType.imageToPdf,
                    selection: $selectedTool
                ) { EmptyView() }

                NavigationLink(
                    destination: MergePdfView(),
                    tag: ToolType.mergePdf,
                    selection: $selectedTool
                ) { EmptyView() }

                NavigationLink(
                    destination: SplitPdfView(),
                    tag: ToolType.splitPdf,
                    selection: $selectedTool
                ) { EmptyView() }

                NavigationLink(
                    destination: CompressPdfView(),
                    tag: ToolType.compressPdf,
                    selection: $selectedTool
                ) { EmptyView() }

                NavigationLink(
                    destination: PdfToImageView(),
                    tag: ToolType.pdfToImage,
                    selection: $selectedTool
                ) { EmptyView() }
            }
            .navigationBarHidden(true)
            .onAppear {
                viewModel.refreshRecentFiles()
            }
            .sheet(isPresented: $isShowingShare) {
                if let url = shareFileUrl {
                    ActivityView(activityItems: [url])
                }
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
}
