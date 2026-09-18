package com.pdftoolkit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pdftoolkit.app.ads.AdMobManager
import com.pdftoolkit.app.theme.PDFToolkitTheme
import com.pdftoolkit.app.ui.navigation.PDFToolkitNavGraph

class MainActivity : ComponentActivity() {
    private lateinit var adMobManager: AdMobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adMobManager = AdMobManager(this)

        setContent {
            PDFToolkitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PDFToolkitNavGraph(adMobManager = adMobManager)
                }
            }
        }
    }
}
