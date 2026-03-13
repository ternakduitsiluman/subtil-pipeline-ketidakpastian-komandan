package com.takatagit.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.takatagit.app.AppContainer
import com.takatagit.app.data.local.EndpointSettingsStore
import com.takatagit.app.ui.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    private val settingsStore: EndpointSettingsStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            endpointInput = settingsStore.currentInput,
            apiKeyInput = settingsStore.currentApiKey,
            resolvedBaseUrl = settingsStore.currentBaseUrl,
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onEndpointInputChange(value: String) {
        _uiState.update {
            it.copy(
                endpointInput = value,
                resolvedBaseUrl = EndpointSettingsStore.normalizeToBaseUrl(value),
                statusMessage = null,
            )
        }
    }

    fun onApiKeyInputChange(value: String) {
        _uiState.update {
            it.copy(
                apiKeyInput = value,
                statusMessage = null,
            )
        }
    }

    fun save() {
        val currentInput = _uiState.value.endpointInput
        val currentApiKey = _uiState.value.apiKeyInput
        settingsStore.updateEndpointInput(currentInput)
        settingsStore.updateApiKeyInput(currentApiKey)
        _uiState.update {
            it.copy(
                endpointInput = settingsStore.currentInput,
                apiKeyInput = settingsStore.currentApiKey,
                resolvedBaseUrl = settingsStore.currentBaseUrl,
                statusMessage = "Endpoint saved.",
            )
        }
    }

    fun resetToDefault() {
        settingsStore.updateEndpointInput(EndpointSettingsStore.DEFAULT_ENDPOINT_INPUT)
        settingsStore.updateApiKeyInput(EndpointSettingsStore.DEFAULT_API_KEY)
        _uiState.update {
            it.copy(
                endpointInput = settingsStore.currentInput,
                apiKeyInput = settingsStore.currentApiKey,
                resolvedBaseUrl = settingsStore.currentBaseUrl,
                statusMessage = "Endpoint and API key reset to default.",
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(AppContainer.endpointSettingsStore) as T
            }
        }
    }
}
