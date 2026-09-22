package com.paramdeep.pdftoolkit.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Difference
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.paramdeep.pdftoolkit.data.model.ProcessingState
import com.paramdeep.pdftoolkit.data.model.RecentFile
import com.paramdeep.pdftoolkit.data.model.ResultData
import com.paramdeep.pdftoolkit.theme.LocalSpacing
import com.paramdeep.pdftoolkit.ui.components.AppTopBar
import com.paramdeep.pdftoolkit.ui.components.DeterminateProgressBar
import com.paramdeep.pdftoolkit.ui.viewmodels.MergePdfViewModel
import com.paramdeep.pdftoolkit.ui.viewmodels.SelectedPdfDoc

@Composable
fun MergePdfScreen(
    viewModel: MergePdfViewModel,
    onBackClick: () -> Unit,
    onResult: (ResultData) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val selectedPdfs by viewModel.selectedPdfs.collectAsState()
    val processingState by viewModel.processingState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addPdfs(uris)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Merge PDFs",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (selectedPdfs.size >= 2 && processingState is ProcessingState.Idle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(spacing.space4)
                ) {
                    Button(
                        onClick = { viewModel.mergePdfs(onResult) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Outlined.Difference, contentDescription = null)
                        Spacer(modifier = Modifier.width(spacing.space2))
                        Text(
                            text = "Merge ${selectedPdfs.size} PDFs",
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

            if (selectedPdfs.isEmpty()) {
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
                                imageVector = Icons.Outlined.Difference,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.space4))

                        Text(
                            text = "Select 2 or More PDFs",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(spacing.space2))

                        Text(
                            text = "Choose existing PDF files from your storage.\nYou can change the document order before merging.",
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
                            Text("Pick PDF Documents")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space4, vertical = spacing.space3),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Documents (${selectedPdfs.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch(arrayOf("application/pdf")) },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add PDF", style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (selectedPdfs.size < 2) {
                    Text(
                        text = "⚠️ Please add at least 1 more PDF to merge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = spacing.space4, vertical = spacing.space1)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = spacing.space4),
                    verticalArrangement = Arrangement.spacedBy(spacing.space2)
                ) {
                    itemsIndexed(selectedPdfs, key = { _, item -> item.uri.toString() }) { index, doc ->
                        MergePdfListItem(
                            doc = doc,
                            index = index,
                            total = selectedPdfs.size,
                            onMoveUp = { if (index > 0) viewModel.movePdf(index, index - 1) },
                            onMoveDown = { if (index < selectedPdfs.size - 1) viewModel.movePdf(index, index + 1) },
                            onRemove = { viewModel.removePdf(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MergePdfListItem(
    doc: SelectedPdfDoc,
    index: Int,
    total: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(spacing.space3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = RecentFile.formatBytes(doc.sizeBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Up / Down controls
            IconButton(onClick = onMoveUp, enabled = index > 0) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Move up",
                    tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onMoveDown, enabled = index < total - 1) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Move down",
                    tint = if (index < total - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
