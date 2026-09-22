package com.paramdeep.pdftoolkit.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paramdeep.pdftoolkit.data.model.ProcessingState
import com.paramdeep.pdftoolkit.data.model.ResultData
import com.paramdeep.pdftoolkit.theme.LocalSpacing
import com.paramdeep.pdftoolkit.ui.components.AppTopBar
import com.paramdeep.pdftoolkit.ui.components.DeterminateProgressBar
import com.paramdeep.pdftoolkit.ui.components.ThumbnailGrid
import com.paramdeep.pdftoolkit.ui.viewmodels.SplitPdfViewModel

@Composable
fun SplitPdfScreen(
    viewModel: SplitPdfViewModel,
    onBackClick: () -> Unit,
    onResult: (ResultData) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sourceUri by viewModel.sourcePdfUri.collectAsState()
    val sourceName by viewModel.sourcePdfName.collectAsState()
    val pages by viewModel.pages.collectAsState()
    val selectedPages by viewModel.selectedPages.collectAsState()
    val isLoadingThumbnails by viewModel.isLoadingThumbnails.collectAsState()
    val processingState by viewModel.processingState.collectAsState()

    var rangeInput by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.setSourcePdf(uri)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Split PDF",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (sourceUri != null && selectedPages.isNotEmpty() && processingState is ProcessingState.Idle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(spacing.space4)
                ) {
                    Button(
                        onClick = { viewModel.splitPdf(onResult) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Outlined.CallSplit, contentDescription = null)
                        Spacer(modifier = Modifier.width(spacing.space2))
                        Text(
                            text = "Extract ${selectedPages.size} ${if (selectedPages.size == 1) "Page" else "Pages"}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (processingState is ProcessingState.Processing) {
                val state = processingState as ProcessingState.Processing
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spacing.space4),
                    contentAlignment = Alignment.Center
                ) {
                    DeterminateProgressBar(
                        progress = state.progress,
                        statusText = state.currentStep,
                        onCancel = { viewModel.cancelProcessing() }
                    )
                }
                return@Scaffold
            }

            if (sourceUri == null) {
                // Empty state prompt
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spacing.space6),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CallSplit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.space4))

                        Text(
                            text = "Select a PDF to Split",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(spacing.space2))

                        Text(
                            text = "Choose a PDF file to view its pages.\nYou can select exact pages or ranges to extract.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(spacing.space6))

                        Button(
                            onClick = { filePickerLauncher.launch(arrayOf("application/pdf")) },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(spacing.space2))
                            Text("Pick PDF File")
                        }
                    }
                }
            } else if (isLoadingThumbnails) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(spacing.space3))
                        Text(
                            text = "Generating page previews...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Document details and Page selection actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space4, vertical = spacing.space2)
                ) {
                    Text(
                        text = sourceName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${pages.size} Total Pages • ${selectedPages.size} Selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(spacing.space2))

                    // Selection Actions (Select All, Deselect All, Range Input)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.selectAll() },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Select All", style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedButton(
                            onClick = { viewModel.deselectAll() },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.space2))

                    // Quick Range Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = rangeInput,
                            onValueChange = { rangeInput = it },
                            placeholder = { Text("Range (e.g. 1-3, 5)", style = MaterialTheme.typography.bodySmall) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(spacing.space2))
                        Button(
                            onClick = {
                                viewModel.applyPageRange(rangeInput)
                                rangeInput = ""
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text("Apply", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Grid of page thumbnails
                ThumbnailGrid(
                    pages = pages,
                    selectedPages = selectedPages,
                    onTogglePage = { pageNum -> viewModel.togglePage(pageNum) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
