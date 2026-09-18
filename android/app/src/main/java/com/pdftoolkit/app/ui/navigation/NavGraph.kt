package com.pdftoolkit.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pdftoolkit.app.ads.AdMobManager
import com.pdftoolkit.app.data.model.ResultData
import com.pdftoolkit.app.data.model.ToolType
import com.pdftoolkit.app.ui.screens.CompressPdfScreen
import com.pdftoolkit.app.ui.screens.HomeScreen
import com.pdftoolkit.app.ui.screens.ImageToPdfScreen
import com.pdftoolkit.app.ui.screens.MergePdfScreen
import com.pdftoolkit.app.ui.screens.PdfToImageScreen
import com.pdftoolkit.app.ui.screens.ResultScreen
import com.pdftoolkit.app.ui.screens.SplitPdfScreen
import com.pdftoolkit.app.ui.viewmodels.CompressPdfViewModel
import com.pdftoolkit.app.ui.viewmodels.HomeViewModel
import com.pdftoolkit.app.ui.viewmodels.ImageToPdfViewModel
import com.pdftoolkit.app.ui.viewmodels.MergePdfViewModel
import com.pdftoolkit.app.ui.viewmodels.PdfToImageViewModel
import com.pdftoolkit.app.ui.viewmodels.SplitPdfViewModel

@Composable
fun PDFToolkitNavGraph(
    adMobManager: AdMobManager,
    navController: NavHostController = rememberNavController()
) {
    var currentResultData by remember { mutableStateOf<ResultData?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToTool = { tool ->
                    when (tool) {
                        ToolType.IMAGE_TO_PDF -> navController.navigate(Screen.ImageToPdf.route)
                        ToolType.MERGE_PDF -> navController.navigate(Screen.MergePdf.route)
                        ToolType.SPLIT_PDF -> navController.navigate(Screen.SplitPdf.route)
                        ToolType.COMPRESS_PDF -> navController.navigate(Screen.CompressPdf.route)
                        ToolType.PDF_TO_IMAGE -> navController.navigate(Screen.PdfToImage.route)
                    }
                }
            )
        }

        composable(Screen.ImageToPdf.route) {
            val imageToPdfViewModel: ImageToPdfViewModel = viewModel()
            ImageToPdfScreen(
                viewModel = imageToPdfViewModel,
                onBackClick = { navController.popBackStack() },
                onResult = { result ->
                    currentResultData = result
                    navController.navigate("result_view")
                }
            )
        }

        composable(Screen.MergePdf.route) {
            val mergePdfViewModel: MergePdfViewModel = viewModel()
            MergePdfScreen(
                viewModel = mergePdfViewModel,
                onBackClick = { navController.popBackStack() },
                onResult = { result ->
                    currentResultData = result
                    navController.navigate("result_view")
                }
            )
        }

        composable(Screen.SplitPdf.route) {
            val splitPdfViewModel: SplitPdfViewModel = viewModel()
            SplitPdfScreen(
                viewModel = splitPdfViewModel,
                onBackClick = { navController.popBackStack() },
                onResult = { result ->
                    currentResultData = result
                    navController.navigate("result_view")
                }
            )
        }

        composable(Screen.CompressPdf.route) {
            val compressPdfViewModel: CompressPdfViewModel = viewModel()
            CompressPdfScreen(
                viewModel = compressPdfViewModel,
                onBackClick = { navController.popBackStack() },
                onResult = { result ->
                    currentResultData = result
                    navController.navigate("result_view")
                }
            )
        }

        composable(Screen.PdfToImage.route) {
            val pdfToImageViewModel: PdfToImageViewModel = viewModel()
            PdfToImageScreen(
                viewModel = pdfToImageViewModel,
                onBackClick = { navController.popBackStack() },
                onResult = { result ->
                    currentResultData = result
                    navController.navigate("result_view")
                }
            )
        }

        composable("result_view") {
            val result = currentResultData
            if (result != null) {
                ResultScreen(
                    result = result,
                    onDone = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    adMobManager = adMobManager
                )
            }
        }
    }
}
