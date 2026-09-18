# PDF Toolkit — App Build Spec

## 1. App Overview

**Name (working title):** PDF Toolkit — Merge, Split & Compress
**One-liner:** A fully offline, on-device PDF utility app that lets users convert images to PDF, merge PDFs, split PDFs, compress PDFs, and convert PDF pages to images — with a clean, modern, zero-learning-curve interface.

**Target platforms:** Android (Kotlin + Jetpack Compose) and iOS (Swift + SwiftUI), built as two native codebases sharing the same design system and feature set.

**Core principle:** No login, no internet requirement for core features, no clutter. A first-time user should be able to complete their first task within 10 seconds of opening the app, with zero onboarding screens.

---

## 2. Target User

- Students, job applicants, and office workers who need to quickly convert photos to PDF or shrink a PDF to email/upload size.
- Low average technical literacy — assume the user has never used a "real" PDF tool before.
- Primarily Indian Android users first, iOS as a secondary/stretch build.

---

## 3. Core Features (v1 scope — build all 5)

1. **Image(s) → PDF**
   - Pick multiple images from gallery or camera.
   - Reorder via drag-and-drop before converting.
   - Optional: page size (A4/Letter/Fit-to-image) and orientation toggle.
   - Output: single PDF file.

2. **Merge PDFs**
   - Pick 2+ existing PDFs from device storage.
   - Reorder the PDF list via drag-and-drop.
   - Output: single merged PDF.

3. **Split PDF**
   - Pick one PDF, show page thumbnails in a grid.
   - User selects a page range or taps individual pages to extract.
   - Output: one new PDF with the selected pages.

4. **Compress PDF**
   - Pick a PDF, show original file size.
   - Offer 3 compression levels (Low / Medium / High) with an estimated size shown before confirming.
   - Output: compressed PDF, show before/after size clearly.

5. **PDF → Images**
   - Pick a PDF, convert each page to a JPG/PNG.
   - Let user pick output quality (Standard/High).
   - Output: images saved to a dedicated app folder, with a "Share All" option.

**All processing must happen fully on-device.** No file should ever leave the phone. This is important for the privacy policy, for offline reliability, and for Play Store / App Store review speed.

---

## 4. UI/UX Design Requirements

### 4.1 Design philosophy
- **Simple, modern, minimal** — think Google's own Files app or Apple's Notes app, not a cluttered "toolbox" app.
- **Everything visible on the home screen** — no hidden hamburger menus, no multi-level nested navigation for core tools. The 5 tools should be immediately visible and tappable the instant the app opens.
- **No onboarding/tutorial screens.** The UI itself should be self-explanatory (large icons + short labels + one-line description under each).
- **Consistent flow for every tool:** Pick file(s) → Configure (if needed) → Preview → Confirm → Result screen with Share/Save/Open buttons. Every tool follows this exact same 5-step skeleton so users only have to learn the pattern once.
- **Progress feedback:** show a determinate progress bar (not a spinner) during processing, since PDF operations can take a few seconds.
- **Result screen** always shows: file size (before → after where relevant), a "Share" button, an "Open" button, and a "Done" button that returns home.

### 4.2 Home screen layout
- A short app title/header at top (no logo needed for v1).
- A 2-column grid of 5 large rounded cards, each representing one tool:
  - Icon (simple line icon, consistent stroke width)
  - Tool name (bold, short)
  - One-line description (muted color, small text)
- Recent files section below the grid (last 3 processed files, tappable to re-open/re-share).
- Banner ad placed at the very bottom, visually separated from the tool grid so it never feels like a 6th tool.

### 4.3 Design system (Tailwind-inspired tokens)
Tailwind CSS itself is a web framework and won't run natively in Kotlin/Swift, but replicate its **design tokens** as a shared design-system reference so both platforms look identical:

- **Color palette** (define as named tokens, light + dark mode):
  - `primary`: `#4F46E5` (indigo-600)
  - `primary-light`: `#EEF2FF` (indigo-50)
  - `surface`: `#FFFFFF` / dark: `#111827`
  - `background`: `#F9FAFB` / dark: `#0B0F19`
  - `text-primary`: `#111827` / dark: `#F9FAFB`
  - `text-muted`: `#6B7280` / dark: `#9CA3AF`
  - `success`: `#16A34A`, `warning`: `#D97706`, `danger`: `#DC2626`
- **Typography:** Inter or system font. Scale: 12 / 14 / 16 / 20 / 24 / 32 px, matching Tailwind's `text-xs` → `text-3xl` steps.
- **Spacing scale:** 4 / 8 / 12 / 16 / 24 / 32 / 48 px (Tailwind's 1/2/3/4/6/8/12 units).
- **Corner radius:** 12px for cards, 8px for buttons, 999px for pills/chips.
- **Elevation:** flat design with subtle shadows only on cards (avoid heavy material shadows).
- Implement this as a single `Theme`/`DesignTokens` file per platform (Compose `Theme.kt` / SwiftUI `Theme.swift`) so both apps are visually identical and easy to re-skin later.

### 4.4 Navigation
- Single-activity/single-window app.
- Bottom-safe simple navigation: Home → Tool flow → Result → back to Home. No bottom tab bar needed for v1 since there's only one "area" (Home) plus in-tool flows.

---

## 5. Tech Stack

### Android
- Kotlin, Jetpack Compose, Material 3 (but overridden with the custom design tokens above, not default Material colors).
- PDF engine: PdfBox-Android (or iText, whichever handles merge/split/compress most reliably) for PDF read/write; Android's own `Bitmap`/`Canvas` APIs for image→PDF and PDF→image rendering.
- Local storage only; use Storage Access Framework (SAF) for picking/saving files (required for scoped storage compliance).
- AdMob SDK for banner + interstitial ads.

### iOS
- Swift, SwiftUI.
- PDFKit (native Apple framework) for merge/split/render/compress — no third-party PDF library needed.
- `UIDocumentPickerViewController` (wrapped for SwiftUI) for file picking.
- Google AdMob SDK (Swift) for banner + interstitial ads, or Apple's own ad network if preferred later.

### Shared
- Both apps should implement the exact same 5 tools, same flow skeleton, same design tokens — built independently but visually/behaviorally identical.

---

## 6. Monetization

- **Banner ad:** persistent on Home screen only (not inside active tool flows, to avoid disrupting the task).
- **Interstitial ad:** shown once per successful export, capped at max 1 per 60 seconds so it never feels spammy.
- **No rewarded-ad gating in v1** — keep every tool fully free and fast to build trust and reviews first; monetization tuning can come in v2 once there's usage data.

---

## 7. Permissions

- Android: Photos/Media access (for image picking), no `INTERNET` permission beyond what AdMob requires, no contacts/location/camera-roll-wide access.
- iOS: Photo Library usage description, Files access via document picker (no broad entitlements needed).
- Explicitly **no** analytics SDKs beyond AdMob's own for v1, to keep the privacy policy short and review-friendly.

---

## 8. Non-Functional Requirements

- App must work with **zero internet connection** for all 5 core tools.
- Cold start to first tool-tap should be under 2 seconds on a mid-range device.
- Handle large PDFs (100+ pages) and large images (12MP+) without crashing — process on a background thread/coroutine with a cancel option.
- Graceful error states: corrupted PDF, unsupported file, storage full — always show a clear, non-technical error message with a "Try Again" button.

---

## 9. Deliverables Expected From Build

1. Android app (Kotlin/Compose) implementing all 5 tools + home screen + design system.
2. iOS app (Swift/SwiftUI) implementing all 5 tools + home screen + same design system.
3. A shared `DESIGN_TOKENS` reference (colors, type scale, spacing) documented once and applied consistently on both platforms.
4. AdMob integration on both platforms per the placement rules in Section 6.
5. A basic privacy policy text file (no data collected beyond standard AdMob attribution) suitable for hosting as a static page for Play Store / App Store submission.

---

## 10. Out of Scope for v1 (do not build yet)

- Cloud sync / accounts / login.
- OCR or text extraction from PDFs.
- E-signatures or PDF form filling.
- Watermarking or password protection (candidate for v2 based on user demand).
