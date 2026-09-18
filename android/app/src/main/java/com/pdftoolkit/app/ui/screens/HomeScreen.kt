package com.pdftoolkit.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.pdftoolkit.app.ads.HomeBannerAd
import com.pdftoolkit.app.data.model.RecentFile
import com.pdftoolkit.app.data.model.ToolType
import com.pdftoolkit.app.theme.LocalSpacing
import com.pdftoolkit.app.ui.components.RecentFilesSection
import com.pdftoolkit.app.ui.components.ToolCard
import com.pdftoolkit.app.ui.viewmodels.HomeViewModel
import java.io.File

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTool: (ToolType) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val recentFiles by viewModel.recentFiles.collectAsState()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            Column {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                HomeBannerAd()
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = spacing.space6)
        ) {
            // App Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space4, vertical = spacing.space4)
                ) {
                    Text(
                        text = "PDF Toolkit",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(spacing.space1))
                    Text(
                        text = "Fast, privacy-friendly on-device PDF utilities",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2-Column Grid of 5 Tools
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space4)
                ) {
                    val tools = viewModel.tools
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                        // Row 1 (Image to PDF, Merge PDFs)
                        androidx.compose.foundation.layout.Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ToolCard(
                                tool = tools[0],
                                onClick = { onNavigateToTool(tools[0]) },
                                modifier = Modifier.weight(1f)
                            )
                            ToolCard(
                                tool = tools[1],
                                onClick = { onNavigateToTool(tools[1]) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2 (Split PDF, Compress PDF)
                        androidx.compose.foundation.layout.Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ToolCard(
                                tool = tools[2],
                                onClick = { onNavigateToTool(tools[2]) },
                                modifier = Modifier.weight(1f)
                            )
                            ToolCard(
                                tool = tools[3],
                                onClick = { onNavigateToTool(tools[3]) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 3 (PDF to Images - Spans wide or single card)
                        androidx.compose.foundation.layout.Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ToolCard(
                                tool = tools[4],
                                onClick = { onNavigateToTool(tools[4]) },
                                modifier = Modifier.weight(1f)
                            )
                            // Spacer to keep 2-column aesthetic balanced or full width
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Recent Files Section
            if (recentFiles.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(spacing.space6))
                    RecentFilesSection(
                        recentFiles = recentFiles,
                        onOpenFile = { file -> openRecentFile(context, file) },
                        onShareFile = { file -> shareRecentFile(context, file) }
                    )
                }
            }
        }
    }
}

private fun openRecentFile(context: Context, recent: RecentFile) {
    try {
        val file = File(recent.filePath)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun shareRecentFile(context: Context, recent: RecentFile) {
    try {
        val file = File(recent.filePath)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
