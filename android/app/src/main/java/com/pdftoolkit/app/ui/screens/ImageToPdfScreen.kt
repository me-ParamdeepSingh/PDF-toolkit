package com.pdftoolkit.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pdftoolkit.app.data.model.PageOrientation
import com.pdftoolkit.app.data.model.PageSizeOption
import com.pdftoolkit.app.data.model.ProcessingState
import com.pdftoolkit.app.data.model.ResultData
import com.pdftoolkit.app.theme.LocalSpacing
import com.pdftoolkit.app.ui.components.AppTopBar
import com.pdftoolkit.app.ui.components.DeterminateProgressBar
import com.pdftoolkit.app.ui.viewmodels.ImageToPdfViewModel

@Composable
fun ImageToPdfScreen(
    viewModel: ImageToPdfViewModel,
    onBackClick: () -> Unit,
    onResult: (ResultData) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val selectedImages by viewModel.selectedImages.collectAsState()
    val pageSize by viewModel.pageSize.collectAsState()
    val orientation by viewModel.orientation.collectAsState()
    val processingState by viewModel.processingState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(50)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Images to PDF",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (selectedImages.isNotEmpty() && processingState is ProcessingState.Idle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(spacing.space4)
                ) {
                    Button(
                        onClick = { viewModel.convertToPdf(onResult) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Outlined.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(spacing.space2))
                        Text(
                            text = "Create PDF (${selectedImages.size} pages)",
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
            // Processing State Overlay
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

            if (selectedImages.isEmpty()) {
                // Empty State / Picker Prompt
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
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.space4))

                        Text(
                            text = "Select Images to Convert",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(spacing.space2))

                        Text(
                            text = "Choose one or more photos from your device.\nYou can reorder them before converting.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(spacing.space6))

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(spacing.space2))
                            Text("Pick Images")
                        }
                    }
                }
            } else {
                // Options and Reorderable Image Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space4, vertical = spacing.space2)
                ) {
                    // Page Size Selector
                    Text(
                        text = "Page Size",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                        modifier = Modifier.padding(vertical = spacing.space1)
                    ) {
                        items(PageSizeOption.values().toList()) { opt ->
                            FilterChip(
                                selected = pageSize == opt,
                                onClick = { viewModel.setPageSize(opt) },
                                label = { Text(opt.label, style = MaterialTheme.typography.bodySmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Orientation Selector
                    Text(
                        text = "Orientation",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.space1)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                        modifier = Modifier.padding(vertical = spacing.space1)
                    ) {
                        items(PageOrientation.values().toList()) { opt ->
                            FilterChip(
                                selected = orientation == opt,
                                onClick = { viewModel.setOrientation(opt) },
                                label = { Text(opt.label, style = MaterialTheme.typography.bodySmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                // Header for Image List & Add More
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space4, vertical = spacing.space2),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pages (${selectedImages.size})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add More", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Grid of selected images with reorder buttons
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(spacing.space4),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                    verticalArrangement = Arrangement.spacedBy(spacing.space3),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(selectedImages, key = { _, uri -> uri.toString() }) { index, uri ->
                        ImageCardItem(
                            uri = uri,
                            index = index,
                            total = selectedImages.size,
                            onMoveLeft = { if (index > 0) viewModel.moveImage(index, index - 1) },
                            onMoveRight = { if (index < selectedImages.size - 1) viewModel.moveImage(index, index + 1) },
                            onRemove = { viewModel.removeImage(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageCardItem(
    uri: Uri,
    index: Int,
    total: Int,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRemove: () -> Unit
) {
    val spacing = LocalSpacing.current

    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = uri,
                contentDescription = "Image $index",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Page Number Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(spacing.space1)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Remove Button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Reorder actions bar at bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (index > 0) {
                    IconButton(onClick = onMoveLeft, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "Move earlier", modifier = Modifier.size(14.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }

                if (index < total - 1) {
                    IconButton(onClick = onMoveRight, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Outlined.ArrowForward, contentDescription = "Move later", modifier = Modifier.size(14.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
