import SwiftUI

@main
struct PDFToolkitApp: App {
    var body: some Scene {
        WindowGroup {
            HomeScreen()
                .preferredColorScheme(nil) // System default light/dark
        }
    }
}
