package com.takatagit.app

import android.content.Context
import com.takatagit.app.data.TakataGitRepository
import com.takatagit.app.data.local.EndpointSettingsStore
import com.takatagit.app.data.remote.NetworkModule

object AppContainer {
    lateinit var endpointSettingsStore: EndpointSettingsStore
        private set

    lateinit var repository: TakataGitRepository
        private set

    fun initialize(context: Context) {
        if (::endpointSettingsStore.isInitialized && ::repository.isInitialized) return

        endpointSettingsStore = EndpointSettingsStore(context.applicationContext)
        repository = TakataGitRepository(
            apiProvider = {
                NetworkModule.api(
                    baseUrl = endpointSettingsStore.currentBaseUrl,
                    apiKey = endpointSettingsStore.currentApiKey,
                )
            },
        )
    }
}
