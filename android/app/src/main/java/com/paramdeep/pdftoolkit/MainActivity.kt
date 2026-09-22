package com.paramdeep.pdftoolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.paramdeep.pdftoolkit.ads.AdMobManager
import com.paramdeep.pdftoolkit.theme.PDFToolkitTheme
import com.paramdeep.pdftoolkit.ui.navigation.PDFToolkitNavGraph

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
