package com.takatagit.app.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val apiCache = ConcurrentHashMap<String, TakataGitApi>()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    fun api(baseUrl: String, apiKey: String): TakataGitApi {
        val cacheKey = "$baseUrl|$apiKey"
        return apiCache.getOrPut(cacheKey) {
            val headersInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("accept", "application/json")
                    .header("X-API-Key", apiKey)
                    .build()
                chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(headersInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TakataGitApi::class.java)
        }
    }
}
