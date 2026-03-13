package com.takatagit.app.ui.state

import com.takatagit.app.data.model.LogSummary

enum class DateSortOption(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
}

data class MainListUiState(
    val loadingMode: LoadingMode = LoadingMode.INITIAL,
    val logs: List<LogSummary> = emptyList(),
    val query: String = "",
    val sortOption: DateSortOption = DateSortOption.NEWEST,
    val errorMessage: String? = null,
    val lastUpdatedLabel: String = "",
    val endpointLabel: String = "",
) {
    val isInitialLoading: Boolean
        get() = loadingMode == LoadingMode.INITIAL

    val isManualRefreshing: Boolean
        get() = loadingMode == LoadingMode.MANUAL_REFRESH

    val isAutoRefreshing: Boolean
        get() = loadingMode == LoadingMode.AUTO_REFRESH

    val filteredLogs: List<LogSummary>
        get() {
            val searched = logs.filter { it.id.contains(query.trim(), ignoreCase = true) }
            return when (sortOption) {
                DateSortOption.NEWEST -> searched.sortedByDescending { it.epochMillis }
                DateSortOption.OLDEST -> searched.sortedBy { it.epochMillis }
            }
        }
}
