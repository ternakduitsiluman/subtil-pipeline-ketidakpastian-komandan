package com.takatagit.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takatagit.app.data.model.LogSummary
import com.takatagit.app.ui.state.DateSortOption
import com.takatagit.app.ui.viewmodel.MainListViewModel

@Composable
fun MainListScreen(
    viewModel: MainListViewModel,
    onOpenSettings: () -> Unit,
    onLogClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainListContent(
        query = uiState.query,
        sortOption = uiState.sortOption,
        logs = uiState.filteredLogs,
        isInitialLoading = uiState.isInitialLoading,
        isManualRefreshing = uiState.isManualRefreshing,
        isAutoRefreshing = uiState.isAutoRefreshing,
        errorMessage = uiState.errorMessage,
        lastUpdatedLabel = uiState.lastUpdatedLabel,
        endpointLabel = uiState.endpointLabel,
        onQueryChange = viewModel::onSearchQueryChange,
        onSortChange = viewModel::onSortOptionSelected,
        onRefresh = viewModel::refreshNow,
        onOpenSettings = onOpenSettings,
        onLogClick = onLogClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainListContent(
    query: String,
    sortOption: DateSortOption,
    logs: List<LogSummary>,
    isInitialLoading: Boolean,
    isManualRefreshing: Boolean,
    isAutoRefreshing: Boolean,
    errorMessage: String?,
    lastUpdatedLabel: String,
    endpointLabel: String,
    onQueryChange: (String) -> Unit,
    onSortChange: (DateSortOption) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TakataGit Logs") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Open API settings",
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh logs",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        ),
                    ),
                )
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search IDs") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                DateSortOption.entries.forEach { option ->
                    AssistChip(
                        onClick = { onSortChange(option) },
                        label = { Text(option.label) },
                        leadingIcon = if (option == sortOption) {
                            {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when {
                    isInitialLoading -> "Loading logs..."
                    isManualRefreshing -> "Refreshing logs..."
                    isAutoRefreshing -> "Auto-refreshing..."
                    lastUpdatedLabel.isBlank() -> "Polling every 5 seconds"
                    else -> "Last sync $lastUpdatedLabel"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (lastUpdatedLabel.isNotBlank() && !isInitialLoading && !isManualRefreshing && !isAutoRefreshing) {
                Text(
                    text = "Polling every 5 seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (endpointLabel.isNotBlank()) {
                Text(
                    text = endpointLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (isAutoRefreshing) {
                Text(
                    text = "Background sync keeps the list updated.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (errorMessage != null) {
                ErrorBanner(message = errorMessage)
                Spacer(modifier = Modifier.height(12.dp))
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (isInitialLoading) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        repeat(5) {
                            LogSummarySkeleton()
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (logs.isEmpty()) {
                            item {
                                EmptyLogsCard(query = query)
                            }
                        }
                        items(
                            items = logs,
                            key = { it.id },
                        ) { log ->
                            LogSummaryCard(
                                log = log,
                                onClick = { onLogClick(log.id) },
                            )
                        }
                    }
                }
                if (isManualRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LogSummarySkeleton() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SkeletonBar(widthFraction = 0.35f, height = 12.dp)
            SkeletonBar(widthFraction = 1f, height = 20.dp)
            SkeletonBar(widthFraction = 0.6f, height = 12.dp)
        }
    }
}

@Composable
private fun SkeletonBar(
    widthFraction: Float,
    height: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0x2630363D)),
    )
}

@Composable
private fun EmptyLogsCard(query: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (query.isBlank()) "No logs available." else "No logs match \"$query\".",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Adjust the search or wait for the next polling cycle.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LogSummaryCard(
    log: LogSummary,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "LOG ID",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                InputChip(
                    selected = true,
                    onClick = onClick,
                    label = { Text("Open") },
                )
            }
            Text(
                text = log.id,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Text(
                text = log.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Tap card to open terminal detail",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
