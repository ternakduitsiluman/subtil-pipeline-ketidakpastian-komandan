package com.takatagit.app.ui.state

data class SettingsUiState(
    val endpointInput: String = "",
    val apiKeyInput: String = "",
    val resolvedBaseUrl: String = "",
    val statusMessage: String? = null,
)
