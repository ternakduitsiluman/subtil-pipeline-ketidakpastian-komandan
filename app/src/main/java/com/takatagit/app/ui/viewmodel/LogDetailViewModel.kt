package com.takatagit.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import com.takatagit.app.AppContainer
import com.takatagit.app.data.TakataGitRepository
import com.takatagit.app.data.local.EndpointSettingsStore
import com.takatagit.app.ui.state.LogDetailUiState
import com.takatagit.app.ui.state.LoadingMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LogDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TakataGitRepository,
    private val settingsStore: EndpointSettingsStore,
) : ViewModel() {
    private val logId: String = checkNotNull(savedStateHandle["logId"])
    private val _uiState = MutableStateFlow(LogDetailUiState(logId = logId))
    val uiState: StateFlow<LogDetailUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    fun refreshNow() {
        viewModelScope.launch {
            fetchDetails(LoadingMode.MANUAL_REFRESH)
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            fetchDetails(LoadingMode.INITIAL)
            while (true) {
                delay(POLLING_INTERVAL_MS)
                fetchDetails(LoadingMode.AUTO_REFRESH)
            }
        }
    }

    private suspend fun fetchDetails(mode: LoadingMode) {
        val hasData = _uiState.value.entries.isNotEmpty()
        val resolvedMode = if (!hasData) LoadingMode.INITIAL else mode
        _uiState.update {
            it.copy(
                loadingMode = resolvedMode,
                errorMessage = if (resolvedMode == LoadingMode.AUTO_REFRESH) it.errorMessage else null,
            )
        }
        runCatching { repository.fetchLogDetails(logId) }
            .onSuccess { entries ->
                _uiState.update {
                    it.copy(
                        loadingMode = LoadingMode.NONE,
                        entries = entries,
                        errorMessage = null,
                        lastUpdatedLabel = formatRefreshTime(),
                    )
                }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loadingMode = LoadingMode.NONE,
                        errorMessage = buildErrorMessage(throwable.message),
                    )
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
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val savedStateHandle = extras.createSavedStateHandle()
                @Suppress("UNCHECKED_CAST")
                return LogDetailViewModel(
                    savedStateHandle = savedStateHandle,
                    repository = AppContainer.repository,
                    settingsStore = AppContainer.endpointSettingsStore,
                ) as T
            }
        }
    }

    private fun formatRefreshTime(): String = refreshFormatter.format(
        Instant.now().atZone(ZoneId.systemDefault()),
    )

    private fun buildErrorMessage(rawMessage: String?): String {
        val suffix = rawMessage?.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
        return "Unable to load log details from ${settingsStore.currentBaseUrl}$suffix"
    }
}
