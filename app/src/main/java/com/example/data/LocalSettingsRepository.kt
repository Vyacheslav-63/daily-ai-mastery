package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "daily_ai_mastery_local_settings"
)

class LocalSettingsRepository(private val context: Context) {

    private object Keys {
        val CUSTOM_API_KEY = stringPreferencesKey("custom_gemini_api_key")
        val CUSTOM_PROXY_BASE_URL = stringPreferencesKey("custom_proxy_base_url")
        val CUSTOM_MODEL_NAME = stringPreferencesKey("custom_model_name")
        val SELF_PACED_MODE = booleanPreferencesKey("self_paced_mode")
    }

    val settingsFlow: Flow<ProxySettings> = context.settingsDataStore.data.map { prefs ->
        ProxySettings(
            customApiKey = prefs[Keys.CUSTOM_API_KEY].orEmpty(),
            customProxyBaseUrl = prefs[Keys.CUSTOM_PROXY_BASE_URL].orEmpty(),
            customModelName = prefs[Keys.CUSTOM_MODEL_NAME]?.takeIf { it.isNotBlank() }
                ?: "gemini-2.5-flash",
            selfPacedMode = prefs[Keys.SELF_PACED_MODE] ?: false
        )
    }

    suspend fun saveProxySettings(
        apiKey: String,
        proxyBaseUrl: String,
        modelName: String
    ) {
        val normalizedUrl = normalizeProxyBaseUrl(proxyBaseUrl)
        val normalizedModel = modelName.trim().ifBlank { "gemini-2.5-flash" }
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.CUSTOM_API_KEY] = apiKey.trim()
            prefs[Keys.CUSTOM_PROXY_BASE_URL] = normalizedUrl
            prefs[Keys.CUSTOM_MODEL_NAME] = normalizedModel
        }
    }

    companion object {
        fun normalizeProxyBaseUrl(rawUrl: String): String {
            val trimmed = rawUrl.trim().trimEnd('/')
            if (trimmed.isBlank()) return ""
            val withScheme = if (
                trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("http://", ignoreCase = true)
            ) {
                trimmed
            } else {
                "https://$trimmed"
            }
            return withScheme
                .removeSuffix("/v1beta/models")
                .removeSuffix("/v1beta")
                .trimEnd('/')
        }
    }

    suspend fun resetProxySettings() {
        context.settingsDataStore.edit { prefs ->
            prefs.remove(Keys.CUSTOM_API_KEY)
            prefs.remove(Keys.CUSTOM_PROXY_BASE_URL)
            prefs[Keys.CUSTOM_MODEL_NAME] = "gemini-2.5-flash"
        }
    }

    suspend fun setSelfPacedMode(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.SELF_PACED_MODE] = enabled
        }
    }
}
