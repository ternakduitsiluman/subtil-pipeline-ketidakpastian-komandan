package com.takatagit.app.data

import com.takatagit.app.data.model.LogEntry
import com.takatagit.app.data.model.LogEntryDto
import com.takatagit.app.data.model.LogSummary
import com.takatagit.app.data.model.LogSummaryDto
import com.takatagit.app.data.remote.TakataGitApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class TakataGitRepository(
    private val apiProvider: () -> TakataGitApi,
) {
    suspend fun fetchLogs(): List<LogSummary> = apiProvider().getLogs()
        .map { dto -> dto.toDomain() }
        .sortedByDescending { it.epochMillis }

    suspend fun fetchLogDetails(id: String): List<LogEntry> = apiProvider().getLogDetails(id)
        .sortedBy { dto -> parseEpochMillis(dto.timestamp.orEmpty()) }
        .map { dto -> dto.toDomain() }

    private fun LogSummaryDto.toDomain(): LogSummary {
        val rawTimestamp = timestamp.orEmpty()
        return LogSummary(
            id = id,
            timestamp = rawTimestamp.ifBlank { "Unknown timestamp" },
            epochMillis = parseEpochMillis(rawTimestamp),
        )
    }

    private fun LogEntryDto.toDomain(): LogEntry = LogEntry(
        level = level?.trim().orEmpty().ifBlank { "INFO" },
        message = message?.trim().orEmpty().ifBlank { "<empty log line>" },
        timestamp = timestamp?.trim().orEmpty(),
    )

    private fun parseEpochMillis(value: String): Long {
        if (value.isBlank()) return Long.MIN_VALUE
        return runCatching { Instant.parse(value).toEpochMilli() }
            .recoverCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }
            .recoverCatching { LocalDateTime.parse(value).toInstant(ZoneOffset.UTC).toEpochMilli() }
            .getOrElse { Long.MIN_VALUE }
    }
}
