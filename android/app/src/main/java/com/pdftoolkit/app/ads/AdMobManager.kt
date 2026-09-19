package com.pdftoolkit.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdMobManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var lastAdShownTimestamp: Long = 0L

    companion object {
        // Minimum interval between interstitial ads (60 seconds)
        private const val MIN_COOLDOWN_MS = 60_000L
        // Google test interstitial ad unit ID
        const val TEST_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
        // Google test banner ad unit ID
        const val TEST_BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111"
    }

    init {
        loadInterstitialAd()
    }

    fun loadInterstitialAd() {
        if (interstitialAd != null || isLoading) return
        isLoading = true

        val adUnitId = context.getString(com.pdftoolkit.app.R.string.admob_interstitial_id)
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showInterstitialIfReady(activity: Activity, onClosed: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        val elapsed = now - lastAdShownTimestamp

        if (interstitialAd != null && elapsed >= MIN_COOLDOWN_MS) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastAdShownTimestamp = System.currentTimeMillis()
                    loadInterstitialAd()
                    onClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    loadInterstitialAd()
                    onClosed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            onClosed()
        }
    }
}
