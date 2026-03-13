package com.takatagit.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takatagit.app.AppContainer
import com.takatagit.app.data.TakataGitRepository
import com.takatagit.app.data.local.EndpointSettingsStore
import com.takatagit.app.ui.state.DateSortOption
import com.takatagit.app.ui.state.LoadingMode
import com.takatagit.app.ui.state.MainListUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainListViewModel(
    private val repository: TakataGitRepository,
    private val settingsStore: EndpointSettingsStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        MainListUiState(
            endpointLabel = settingsStore.currentBaseUrl,
        ),
    )
    val uiState: StateFlow<MainListUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        observeEndpoint()
        startPolling()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSortOptionSelected(sortOption: DateSortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
    }

    fun refreshNow() {
        viewModelScope.launch {
            fetchLogs(LoadingMode.MANUAL_REFRESH)
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            fetchLogs(LoadingMode.INITIAL)
            while (true) {
                delay(POLLING_INTERVAL_MS)
                fetchLogs(LoadingMode.AUTO_REFRESH)
            }
        }
    }

    private suspend fun fetchLogs(mode: LoadingMode) {
        val hasData = _uiState.value.logs.isNotEmpty()
        val resolvedMode = if (!hasData) LoadingMode.INITIAL else mode
        _uiState.update {
            it.copy(
                loadingMode = resolvedMode,
                errorMessage = if (resolvedMode == LoadingMode.AUTO_REFRESH) it.errorMessage else null,
            )
        }
        runCatching { repository.fetchLogs() }
            .onSuccess { logs ->
                _uiState.update { current ->
                    current.copy(
                        loadingMode = LoadingMode.NONE,
                        logs = logs,
                        errorMessage = null,
                        lastUpdatedLabel = formatRefreshTime(),
                        endpointLabel = settingsStore.currentBaseUrl,
                    )
                }
            }
            .onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        loadingMode = LoadingMode.NONE,
                        endpointLabel = settingsStore.currentBaseUrl,
                        errorMessage = buildErrorMessage(throwable.message, "logs"),
                    )
                }
            }
    }

    private fun observeEndpoint() {
        viewModelScope.launch {
            settingsStore.endpointInput.collect { _ ->
                _uiState.update { it.copy(endpointLabel = settingsStore.currentBaseUrl) }
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val POLLING_INTERVAL_MS = 5_000L
        private val refreshFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm:ss")

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainListViewModel(
                    repository = AppContainer.repository,
                    settingsStore = AppContainer.endpointSettingsStore,
                ) as T
            }
        }
    }

    private fun formatRefreshTime(): String = refreshFormatter.format(
        Instant.now().atZone(ZoneId.systemDefault()),
    )

    private fun buildErrorMessage(rawMessage: String?, resourceName: String): String {
        val suffix = rawMessage?.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
        return "Unable to load $resourceName from ${settingsStore.currentBaseUrl}$suffix"
    }
}
