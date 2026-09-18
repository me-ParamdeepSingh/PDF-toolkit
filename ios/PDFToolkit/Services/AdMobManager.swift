import SwiftUI
import UIKit

final class AdMobManager: ObservableObject {
    static let shared = AdMobManager()

    // Test Ad Unit IDs
    static let bannerAdUnitId = "ca-app-pub-3940256099942544/2934735716"
    static let interstitialAdUnitId = "ca-app-pub-3940256099942544/4411468910"

    private var lastAdShownTimestamp: Double = 0
    private let minCooldownSeconds: Double = 60.0

    func showInterstitialIfAllowed(from viewController: UIViewController?, completion: @escaping () -> Void) {
        let now = Date().timeIntervalSince1970
        let elapsed = now - lastAdShownTimestamp

        if elapsed >= minCooldownSeconds {
            lastAdShownTimestamp = now
            // When GoogleMobileAds SDK is linked in production, show GADInterstitialAd here.
            // In standalone/mock mode, trigger callback immediately without delay
            completion()
        } else {
            completion()
        }
    }
}
