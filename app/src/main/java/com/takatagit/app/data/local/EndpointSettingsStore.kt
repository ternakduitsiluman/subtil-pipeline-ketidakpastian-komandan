package com.takatagit.app.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EndpointSettingsStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _endpointInput = MutableStateFlow(
        preferences.getString(KEY_ENDPOINT_INPUT, DEFAULT_ENDPOINT_INPUT) ?: DEFAULT_ENDPOINT_INPUT,
    )
    private val _apiKeyInput = MutableStateFlow(
        preferences.getString(KEY_API_KEY_INPUT, DEFAULT_API_KEY) ?: DEFAULT_API_KEY,
    )

    val endpointInput: StateFlow<String> = _endpointInput.asStateFlow()
    val apiKeyInput: StateFlow<String> = _apiKeyInput.asStateFlow()

    val currentInput: String
        get() = _endpointInput.value

    val currentApiKey: String
        get() = sanitizeApiKey(_apiKeyInput.value)

    val currentBaseUrl: String
        get() = normalizeToBaseUrl(currentInput)

    fun updateEndpointInput(value: String) {
        val sanitized = sanitizeInput(value)
        preferences.edit().putString(KEY_ENDPOINT_INPUT, sanitized).apply()
        _endpointInput.value = sanitized
    }

    fun updateApiKeyInput(value: String) {
        val sanitized = sanitizeApiKey(value)
        preferences.edit().putString(KEY_API_KEY_INPUT, sanitized).apply()
        _apiKeyInput.value = sanitized
    }

    companion object {
        const val DEFAULT_ENDPOINT_INPUT = "takatagit.dawg.web.id"
        const val DEFAULT_API_KEY = "akucintasagiri"

        private const val PREFS_NAME = "takata_git_settings"
        private const val KEY_ENDPOINT_INPUT = "endpoint_input"
        private const val KEY_API_KEY_INPUT = "api_key_input"

        fun sanitizeInput(value: String): String {
            val trimmed = value.trim()
            if (trimmed.isBlank()) return DEFAULT_ENDPOINT_INPUT
            return trimmed.removeSuffix("/")
        }

        fun sanitizeApiKey(value: String): String {
            val trimmed = value.trim()
            if (trimmed.isBlank()) return DEFAULT_API_KEY
            return trimmed
        }

        fun normalizeToBaseUrl(value: String): String {
            val sanitized = sanitizeInput(value)
            val withScheme = if (
                sanitized.startsWith("http://", ignoreCase = true) ||
                sanitized.startsWith("https://", ignoreCase = true)
            ) {
                sanitized
            } else {
                "https://$sanitized"
            }

            val withoutTrailingSlash = withScheme.removeSuffix("/")
            val hasCustomPath = withoutTrailingSlash.substringAfter("://").contains("/")

            return if (hasCustomPath) {
                "$withoutTrailingSlash/"
            } else {
                "$withoutTrailingSlash/api/v1/"
            }
        }
    }
}
