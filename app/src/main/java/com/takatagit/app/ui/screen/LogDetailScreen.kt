package com.takatagit.app.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takatagit.app.data.model.LogEntry
import com.takatagit.app.ui.viewmodel.LogDetailViewModel

@Composable
fun LogDetailScreen(
    viewModel: LogDetailViewModel,
    onNavigateUp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LogDetailContent(
        logId = uiState.logId,
        entries = uiState.entries,
        isInitialLoading = uiState.isInitialLoading,
        isManualRefreshing = uiState.isManualRefreshing,
        isAutoRefreshing = uiState.isAutoRefreshing,
        errorMessage = uiState.errorMessage,
        lastUpdatedLabel = uiState.lastUpdatedLabel,
        onRefresh = viewModel::refreshNow,
        onNavigateUp = onNavigateUp,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LogDetailContent(
    logId: String,
    entries: List<LogEntry>,
    isInitialLoading: Boolean,
    isManualRefreshing: Boolean,
    isAutoRefreshing: Boolean,
    errorMessage: String?,
    lastUpdatedLabel: String,
    onRefresh: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val scrollAnchor = entries.lastOrNull()?.let { "${it.timestamp}:${it.level}:${it.message}" }
    var zoom by remember { mutableFloatStateOf(DefaultTerminalZoom) }
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        zoom = (zoom * zoomChange).coerceIn(MinTerminalZoom, MaxTerminalZoom)
    }

    LaunchedEffect(scrollAnchor) {
        if (entries.isNotEmpty()) {
            repeat(2) { withFrameNanos { } }
            verticalScrollState.scrollTo(verticalScrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terminal Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh details",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(20.dp),
                color = Color.Black,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D)),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D1117))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "TERMINAL SESSION",
                            color = Color(0xFF58A6FF),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = logId,
                            color = Color(0xFFC9D1D9),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = when {
                                isInitialLoading -> "Loading terminal session..."
                                isManualRefreshing -> "Refreshing terminal..."
                                isAutoRefreshing -> "Auto-refreshing terminal..."
                                lastUpdatedLabel.isBlank() -> "Polling every 5 seconds"
                                else -> "Last sync $lastUpdatedLabel"
                            },
                            color = Color(0xFF8B949E),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (lastUpdatedLabel.isNotBlank() && !isInitialLoading && !isManualRefreshing && !isAutoRefreshing) {
                            Text(
                                text = "Polling every 5 seconds",
                                color = Color(0xFF8B949E),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    if (isAutoRefreshing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF58A6FF),
                            trackColor = Color(0xFF0D1117),
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2D1117))
                                .padding(12.dp),
                            color = Color(0xFFFF7B72),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .transformable(
                                state = transformableState,
                                canPan = { false },
                            ),
                    ) {
                        when {
                            isInitialLoading -> TerminalSkeleton()
                            entries.isEmpty() -> {
                                Text(
                                    text = "No terminal output available yet.",
                                    color = Color(0xFF8B949E),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    modifier = Modifier.padding(16.dp),
                                )
                            }

                            else -> {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(verticalScrollState)
                                        .horizontalScroll(horizontalScrollState)
                                        .wrapContentWidth(unbounded = true)
                                        .graphicsLayer {
                                            scaleX = zoom
                                            scaleY = zoom
                                            transformOrigin = TransformOrigin(0f, 0f)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    entries.forEach { entry ->
                                        TerminalLine(entry = entry)
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isManualRefreshing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun TerminalSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(9) { index ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(184.dp)
                        .height(14.dp)
                        .background(Color(0x2230363D), RoundedCornerShape(999.dp)),
                )
                Box(
                    modifier = Modifier
                        .width(74.dp)
                        .height(14.dp)
                        .background(Color(0x223A6EA5), RoundedCornerShape(999.dp)),
                )
                Box(
                    modifier = Modifier
                        .width(if (index % 2 == 0) 220.dp else 280.dp)
                        .height(14.dp)
                        .background(Color(0x2230363D), RoundedCornerShape(999.dp)),
                )
            }
        }
    }
}

@Composable
private fun TerminalLine(entry: LogEntry) {
    val levelColor = when (entry.level.uppercase()) {
        "ERROR" -> Color(0xFFFF7B72)
        "WARNING", "WARN" -> Color(0xFFD29922)
        else -> Color(0xFF3FB950)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = entry.timestamp.ifBlank { "--" },
            modifier = Modifier.width(184.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color(0xFF8B949E),
            ),
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
        Text(
            text = "[${entry.level.uppercase()}]",
            modifier = Modifier.width(74.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = levelColor,
            ),
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xFFC9D1D9),
            ),
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}

private const val DefaultTerminalZoom: Float = 1f
private const val MinTerminalZoom: Float = 0.85f
private const val MaxTerminalZoom: Float = 2.4f
