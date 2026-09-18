# PDF Toolkit — Shared Design Tokens Specification

This document defines the unified design tokens implemented across both **Android (Jetpack Compose)** and **iOS (SwiftUI)** native codebases.

---

## 1. Color Palette

Tailwind-inspired Indigo design tokens with dedicated Light and Dark mode mappings.

| Token Name | Light Mode Hex | Dark Mode Hex | Usage |
| :--- | :--- | :--- | :--- |
| `primary` | `#4F46E5` (indigo-600) | `#6366F1` (indigo-500) | Primary brand color, CTA buttons, active indicators |
| `primary-light` | `#EEF2FF` (indigo-50) | `#1E1B4B` (indigo-950) | Card icon backgrounds, active chip backgrounds |
| `primary-hover` | `#4338CA` (indigo-700) | `#4F46E5` (indigo-600) | Button hover & press states |
| `surface` | `#FFFFFF` | `#111827` (gray-900) | Card backgrounds, sheet surfaces, dialogs |
| `surface-subtle` | `#F3F4F6` (gray-100) | `#1F2937` (gray-800) | Input fields, thumbnail backgrounds, dividers |
| `background` | `#F9FAFB` (gray-50) | `#0B0F19` (gray-950) | App scaffold background |
| `text-primary` | `#111827` (gray-900) | `#F9FAFB` (gray-50) | Headlines, primary text, active labels |
| `text-muted` | `#6B7280` (gray-500) | `#9CA3AF` (gray-400) | Subtitles, helper text, file sizes, placeholders |
| `border` | `#E5E7EB` (gray-200) | `#374151` (gray-700) | Card borders, subtle separators |
| `success` | `#16A34A` (green-600) | `#22C55E` (green-500) | Success checkmarks, size reduction indicators |
| `warning` | `#D97706` (amber-600) | `#F59E0B` (amber-500) | Caution alerts, processing warnings |
| `danger` | `#DC2626` (red-600) | `#EF4444` (red-500) | Error banners, delete actions, cancel buttons |

---

## 2. Typography Scale

Clean sans-serif typography hierarchy (Inter / Native System Font).

| Token | Size | Line Height | Weight | Usage |
| :--- | :--- | :--- | :--- | :--- |
| `display` | 32px | 38px | Bold (700) | App header, feature hero titles |
| `title` | 24px | 30px | Bold (700) | Screen titles, result headers |
| `headline` | 20px | 26px | SemiBold (600) | Section titles, modal titles |
| `body-large` | 16px | 24px | Medium / Regular | Tool card titles, button text |
| `body` | 14px | 20px | Regular (400) | General body copy, list items |
| `caption` | 12px | 16px | Medium (500) | Tool descriptions, timestamps, badges |
| `tiny` | 10px | 14px | SemiBold (600) | Chip labels, page number badges |

---

## 3. Spacing Scale

Based on 4px grid increments:

| Token | Pixels | Unit (Tailwind) | Usage |
| :--- | :--- | :--- | :--- |
| `space-1` | 4px | 1 | Micro spacing, icon gaps |
| `space-2` | 8px | 2 | Element padding, chip gaps |
| `space-3` | 12px | 3 | Inner card padding, compact lists |
| `space-4` | 16px | 4 | Standard screen margins, standard card padding |
| `space-6` | 24px | 6 | Section spacing, grid gaps |
| `space-8` | 32px | 8 | Top/Bottom screen padding |
| `space-12` | 48px | 12 | Hero section top padding |

---

## 4. Corner Radii

| Token | Value | Usage |
| :--- | :--- | :--- |
| `radius-sm` | 6px | Small badges, thumbnails |
| `radius-md` | 8px | Primary & secondary buttons, text inputs |
| `radius-lg` | 12px | Tool cards, modal dialogs, result cards |
| `radius-xl` | 16px | Bottom sheet dialogs |
| `radius-full` | 9999px | Pills, chips, circular action buttons |

---

## 5. Elevation & Shadows

Flat, modern aesthetic with subtle soft shadows:
- **Card Shadow**: `0px 2px 8px rgba(0, 0, 0, 0.04)` (Light) / `0px 2px 8px rgba(0, 0, 0, 0.25)` (Dark)
- **Active Card / Float**: `0px 8px 24px rgba(79, 70, 229, 0.12)`

---

## 6. Implementation References

- **Android (Kotlin + Jetpack Compose)**:
  - Theme file: `android/app/src/main/java/com/pdftoolkit/app/theme/Theme.kt`
  - Color tokens: `Color.kt`
  - Typography: `Type.kt`
  - Shapes: `Shape.kt`
  - Spacing: `Spacing.kt`

- **iOS (Swift + SwiftUI)**:
  - Theme file: `ios/PDFToolkit/Theme/Theme.swift`
  - Color tokens: `Colors.swift`
  - Typography: `Typography.swift`
  - Shapes & Spacing: `Shapes.swift`, `Spacing.swift`
