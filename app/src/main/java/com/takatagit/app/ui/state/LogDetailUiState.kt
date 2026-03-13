package com.takatagit.app.ui.state

import com.takatagit.app.data.model.LogEntry

data class LogDetailUiState(
    val logId: String = "",
    val loadingMode: LoadingMode = LoadingMode.INITIAL,
    val entries: List<LogEntry> = emptyList(),
    val errorMessage: String? = null,
    val lastUpdatedLabel: String = "",
) {
    val isInitialLoading: Boolean
        get() = loadingMode == LoadingMode.INITIAL

    val isManualRefreshing: Boolean
        get() = loadingMode == LoadingMode.MANUAL_REFRESH

    val isAutoRefreshing: Boolean
        get() = loadingMode == LoadingMode.AUTO_REFRESH
}
