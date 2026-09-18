# PDF Toolkit — Merge, Split & Compress

A fully offline, on-device native PDF utility app for **Android (Kotlin + Jetpack Compose)** and **iOS (Swift + SwiftUI)** with a shared modern design system, zero learning curve, and lightning-fast performance.

---

## ✨ Features (All 5 Core Tools)

1. **Image(s) → PDF**
   - Pick multiple images from gallery or camera.
   - Drag-and-drop reordering before conversion.
   - Configurable page sizes (A4, Letter, Fit-to-image) and page orientation (Portrait / Landscape).
   - Instant on-device PDF generation.

2. **Merge PDFs**
   - Pick 2 or more PDF documents from device storage.
   - Visual reordering of documents before merging.
   - Smooth progress indicator and instant single merged PDF output.

3. **Split PDF**
   - Pick any PDF and view page thumbnails in a clean responsive grid.
   - Tap individual pages or specify a page range (e.g., 1-5, 8).
   - Extract selected pages into a new standalone PDF.

4. **Compress PDF**
   - Pick a PDF to inspect original file size.
   - 3 optimized compression levels: **Low**, **Medium**, and **High**.
   - Live estimated file size preview before processing.
   - Clear Before vs. After file size comparison.

5. **PDF → Images**
   - Extract every page of a PDF as individual high-resolution JPG or PNG images.
   - Configurable output quality (Standard / High).
   - Save to photo gallery or share all images in one tap.

---

## 🔒 100% On-Device & Privacy-First

- **Zero Cloud Uploads:** All document processing is performed 100% locally on the device CPU/GPU.
- **Offline Reliability:** Core features function with no internet connection.
- **Minimal Permissions:** Only standard scoped storage / photo picker permissions are requested.

---

## 🎨 Unified Design System

Both Android and iOS apps share identical design tokens inspired by Tailwind CSS:
- **Primary Color:** `#4F46E5` (Indigo-600) / `#6366F1` (Dark mode)
- **Background:** `#F9FAFB` (Light) / `#0B0F19` (Dark)
- **Surfaces:** `#FFFFFF` (Light) / `#111827` (Dark)
- **Corner Radii:** 12px for tool cards, 8px for action buttons, 9999px for chips.
- See [DESIGN_TOKENS.md](DESIGN_TOKENS.md) for full specifications.

---

## 📱 Project Structure

```
PDF-toolkit/
├── DESIGN_TOKENS.md          # Shared design tokens specification
├── PRIVACY_POLICY.md         # Store-ready Privacy Policy (Markdown)
├── privacy-policy.html       # Store-ready Privacy Policy (Static HTML)
├── pdf-toolkit-app-spec.md   # Original application specification
│
├── android/                  # Native Android App (Kotlin + Jetpack Compose)
│   ├── build.gradle.kts      # Root build configuration
│   ├── settings.gradle.kts   # Gradle settings
│   └── app/
│       ├── build.gradle.kts  # Dependencies (Compose, PDF Engine, Coroutines, AdMob)
│       └── src/main/
│           ├── AndroidManifest.xml
│           ├── res/          # Strings, colors, styles, FileProvider paths
│           └── java/com/pdftoolkit/app/
│               ├── MainActivity.kt
│               ├── theme/    # Compose theme, colors, typography, shapes
│               ├── engine/   # On-device PDF processing engine
│               ├── data/     # Models, Enums & RecentFilesRepository
│               ├── ads/      # AdMob manager & Banner composable
│               └── ui/       # Screens, ViewModels & Reusable components
│
└── ios/                      # Native iOS App (Swift + SwiftUI)
    ├── PDFToolkit.xcodeproj/ # Xcode project descriptor
    └── PDFToolkit/
        ├── Info.plist        # Permissions & AdMob config
        ├── PDFToolkitApp.swift # App Entry point
        ├── Theme/            # SwiftUI Theme, Colors, Typography, Shapes
        ├── Services/         # PDFKit Engine, RecentFilesStore, AdMobManager
        ├── Models/           # Data models & Enums
        ├── ViewModels/       # ViewModels for all 5 tools & Home
        └── Views/            # SwiftUI Views for Home, Tools, and Result
```

---

## 🚀 Getting Started

### Android
1. Open the `android/` directory in **Android Studio** (Hedgehog / Iguana / Jellyfish or newer).
2. Sync Gradle dependencies.
3. Run on an Android Emulator or physical device (Min SDK 26, Target SDK 34).

### iOS
1. Open the `ios/PDFToolkit.xcodeproj` in **Xcode 15+**.
2. Select target simulator or device (iOS 16.0+).
3. Build and run (⌘ + R).

---

## 📄 License & Privacy
- Zero user tracking or document collection. See [PRIVACY_POLICY.md](PRIVACY_POLICY.md).
