package com.takatagit.app.data.remote

import com.takatagit.app.data.model.LogEntryDto
import com.takatagit.app.data.model.LogSummaryDto
import retrofit2.http.GET
import retrofit2.http.Path

interface TakataGitApi {
    @GET("logs")
    suspend fun getLogs(): List<LogSummaryDto>

    @GET("logs/{id}")
    suspend fun getLogDetails(@Path("id") id: String): List<LogEntryDto>
}
