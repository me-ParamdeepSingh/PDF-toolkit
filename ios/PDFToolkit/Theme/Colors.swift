import SwiftUI

extension Color {
    // Shared Design Tokens — Light & Dark Palette
    static let appPrimary = Color("Primary", bundle: nil).fallback(
        light: Color(hex: 0x4F46E5), // indigo-600
        dark: Color(hex: 0x6366F1)   // indigo-500
    )
    
    static let appPrimaryLight = Color("PrimaryLight", bundle: nil).fallback(
        light: Color(hex: 0xEEF2FF), // indigo-50
        dark: Color(hex: 0x1E1B4B)   // indigo-950
    )
    
    static let appSurface = Color("Surface", bundle: nil).fallback(
        light: Color(hex: 0xFFFFFF),
        dark: Color(hex: 0x111827)
    )
    
    static let appSurfaceSubtle = Color("SurfaceSubtle", bundle: nil).fallback(
        light: Color(hex: 0xF3F4F6),
        dark: Color(hex: 0x1F2937)
    )
    
    static let appBackground = Color("Background", bundle: nil).fallback(
        light: Color(hex: 0xF9FAFB),
        dark: Color(hex: 0x0B0F19)
    )
    
    static let appTextPrimary = Color("TextPrimary", bundle: nil).fallback(
        light: Color(hex: 0x111827),
        dark: Color(hex: 0xF9FAFB)
    )
    
    static let appTextMuted = Color("TextMuted", bundle: nil).fallback(
        light: Color(hex: 0x6B7280),
        dark: Color(hex: 0x9CA3AF)
    )
    
    static let appBorder = Color("Border", bundle: nil).fallback(
        light: Color(hex: 0xE5E7EB),
        dark: Color(hex: 0x374151)
    )
    
    static let appSuccess = Color(hex: 0x16A34A)
    static let appWarning = Color(hex: 0xD97706)
    static let appDanger = Color(hex: 0xDC2626)

    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 255,
            green: Double((hex >> 08) & 0xff) / 255,
            blue: Double((hex >> 00) & 0xff) / 255,
            opacity: alpha
        )
    }

    fileprivate func fallback(light: Color, dark: Color) -> Color {
        #if os(iOS)
        return Color(UIColor { traitCollection in
            switch traitCollection.userInterfaceStyle {
            case .dark:
                return UIColor(dark)
            default:
                return UIColor(light)
            }
        })
        #else
        return light
        #endif
    }
}
