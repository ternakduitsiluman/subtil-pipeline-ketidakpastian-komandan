package com.takatagit.app.data.model

import com.google.gson.annotations.SerializedName

data class LogSummaryDto(
    @SerializedName(value = "id", alternate = ["uuid", "log_id"])
    val id: String,
    @SerializedName(value = "timestamp", alternate = ["created_at", "updated_at", "date"])
    val timestamp: String? = null,
)

data class LogEntryDto(
    @SerializedName(value = "level", alternate = ["severity", "type"])
    val level: String? = null,
    @SerializedName(value = "message", alternate = ["log", "content", "text"])
    val message: String? = null,
    @SerializedName(value = "timestamp", alternate = ["created_at", "updated_at", "date"])
    val timestamp: String? = null,
)

data class LogSummary(
    val id: String,
    val timestamp: String,
    val epochMillis: Long,
)

data class LogEntry(
    val level: String,
    val message: String,
    val timestamp: String,
)
