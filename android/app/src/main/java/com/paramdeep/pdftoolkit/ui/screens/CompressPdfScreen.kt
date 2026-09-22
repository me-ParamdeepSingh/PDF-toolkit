package com.paramdeep.pdftoolkit.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.paramdeep.pdftoolkit.data.model.CompressionLevel
import com.paramdeep.pdftoolkit.data.model.ProcessingState
import com.paramdeep.pdftoolkit.data.model.RecentFile
import com.paramdeep.pdftoolkit.data.model.ResultData
import com.paramdeep.pdftoolkit.theme.LocalSpacing
import com.paramdeep.pdftoolkit.ui.components.AppTopBar
import com.paramdeep.pdftoolkit.ui.components.DeterminateProgressBar
import com.paramdeep.pdftoolkit.ui.viewmodels.CompressPdfViewModel

@Composable
fun CompressPdfScreen(
    viewModel: CompressPdfViewModel,
    onBackClick: () -> Unit,
    onResult: (ResultData) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sourceUri by viewModel.sourcePdfUri.collectAsState()
    val sourceName by viewModel.sourcePdfName.collectAsState()
    val originalSize by viewModel.originalSizeBytes.collectAsState()
    val compressionLevel by viewModel.compressionLevel.collectAsState()
    val processingState by viewModel.processingState.collectAsState()

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
                title = "Compress PDF",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (sourceUri != null && processingState is ProcessingState.Idle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(spacing.space4)
                ) {
                    Button(
                        onClick = { viewModel.compressPdf(onResult) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Outlined.Compress, contentDescription = null)
                        Spacer(modifier = Modifier.width(spacing.space2))
                        Text(
                            text = "Compress Now (~${RecentFile.formatBytes(compressionLevel.estimateOutputSize(originalSize))})",
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
                                imageVector = Icons.Outlined.Compress,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.space4))

                        Text(
                            text = "Select a PDF to Compress",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(spacing.space2))

                        Text(
                            text = "Reduce file size for emails and web uploads.\nChoose from 3 tailored compression levels.",
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(spacing.space4)
                ) {
                    // Selected file summary card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.space4),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(spacing.space3))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sourceName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Current Size: ${RecentFile.formatBytes(originalSize)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { filePickerLauncher.launch(arrayOf("application/pdf")) },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text("Change", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.space6))

                    Text(
                        text = "Choose Compression Level",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(spacing.space3))

                    // 3 Compression Level Cards
                    CompressionLevel.values().forEach { level ->
                        val isSelected = compressionLevel == level
                        val estimatedSize = level.estimateOutputSize(originalSize)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = spacing.space1)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { viewModel.setCompressionLevel(level) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.space4),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setCompressionLevel(level) },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(spacing.space2))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = level.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = level.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(spacing.space2))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "~${RecentFile.formatBytes(estimatedSize)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "-${level.estimatedReductionPercent}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
