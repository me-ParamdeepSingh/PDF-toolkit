package com.pdftoolkit.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ImageToPdf : Screen("image_to_pdf")
    data object MergePdf : Screen("merge_pdf")
    data object SplitPdf : Screen("split_pdf")
    data object CompressPdf : Screen("compress_pdf")
    data object PdfToImage : Screen("pdf_to_image")
    data object Result : Screen("result/{resultKey}") {
        fun createRoute(resultKey: String): String = "result/$resultKey"
    }
}
