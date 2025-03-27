package com.screenbuckets.ui.screens

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenbuckets.ScreenBucketsApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Extension function for Context to create DataStore
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val startOnBoot: Boolean = false,
    val showFloatingButton: Boolean = true,
    val skipSensitiveApps: Boolean = true,
    val autoAnalyzeScreenshots: Boolean = true,
    val analyzeOnlyOnWifi: Boolean = true,
    val apiKey: String = ""
)

class SettingsViewModel : ViewModel() {
    
    private val context = ScreenBucketsApp.instance
    private val dataStore = context.settingsDataStore
    
    // Define preference keys
    private object PreferenceKeys {
        val START_ON_BOOT = booleanPreferencesKey("start_on_boot")
        val SHOW_FLOATING_BUTTON = booleanPreferencesKey("show_floating_button")
        val SKIP_SENSITIVE_APPS = booleanPreferencesKey("skip_sensitive_apps")
        val AUTO_ANALYZE_SCREENSHOTS = booleanPreferencesKey("auto_analyze_screenshots")
        val ANALYZE_ONLY_ON_WIFI = booleanPreferencesKey("analyze_only_on_wifi")
        val API_KEY = stringPreferencesKey("api_key")
    }
    
    // Get settings as a Flow
    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            startOnBoot = preferences[PreferenceKeys.START_ON_BOOT] ?: false,
            showFloatingButton = preferences[PreferenceKeys.SHOW_FLOATING_BUTTON] ?: true,
            skipSensitiveApps = preferences[PreferenceKeys.SKIP_SENSITIVE_APPS] ?: true,
            autoAnalyzeScreenshots = preferences[PreferenceKeys.AUTO_ANALYZE_SCREENSHOTS] ?: true,
            analyzeOnlyOnWifi = preferences[PreferenceKeys.ANALYZE_ONLY_ON_WIFI] ?: true,
            apiKey = preferences[PreferenceKeys.API_KEY] ?: ""
        )
    }
    
    // Update methods
    fun updateStartOnBoot(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.START_ON_BOOT] = enabled
            }
        }
    }
    
    fun updateShowFloatingButton(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.SHOW_FLOATING_BUTTON] = enabled
            }
        }
    }
    
    fun updateSkipSensitiveApps(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.SKIP_SENSITIVE_APPS] = enabled
            }
        }
    }
    
    fun updateAutoAnalyzeScreenshots(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.AUTO_ANALYZE_SCREENSHOTS] = enabled
            }
        }
    }
    
    fun updateAnalyzeOnlyOnWifi(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.ANALYZE_ONLY_ON_WIFI] = enabled
            }
        }
    }
    
    fun updateApiKey(apiKey: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.API_KEY] = apiKey
            }
        }
    }
}