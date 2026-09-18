import SwiftUI

struct AppTypography {
    // 12 / 14 / 16 / 20 / 24 / 32 scale
    static let display = Font.system(size: 32, weight: .bold, design: .default)
    static let title = Font.system(size: 24, weight: .bold, design: .default)
    static let headline = Font.system(size: 20, weight: .semibold, design: .default)
    static let bodyLarge = Font.system(size: 16, weight: .medium, design: .default)
    static let body = Font.system(size: 14, weight: .regular, design: .default)
    static let caption = Font.system(size: 12, weight: .medium, design: .default)
    static let tiny = Font.system(size: 10, weight: .semibold, design: .default)
}

struct AppSpacing {
    // 4 / 8 / 12 / 16 / 24 / 32 / 48 scale
    static let space1: CGFloat = 4
    static let space2: CGFloat = 8
    static let space3: CGFloat = 12
    static let space4: CGFloat = 16
    static let space6: CGFloat = 24
    static let space8: CGFloat = 32
    static let space12: CGFloat = 48
}

struct AppShapes {
    // Corner radius: 12px for cards, 8px for buttons, 999px for pills
    static let buttonRadius: CGFloat = 8
    static let cardRadius: CGFloat = 12
    static let sheetRadius: CGFloat = 16
    static let pillRadius: CGFloat = 999
}
