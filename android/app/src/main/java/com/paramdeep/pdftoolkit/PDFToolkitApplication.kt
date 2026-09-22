package com.paramdeep.pdftoolkit

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class PDFToolkitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize PDFBox for Android
        PDFBoxResourceLoader.init(applicationContext)

        // Initialize Google Mobile Ads SDK on a background thread
        Thread {
            MobileAds.initialize(this) {}
        }.start()
    }
}
