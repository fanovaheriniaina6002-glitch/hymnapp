package com.example.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.data.local.AppDatabase
import com.example.data.local.onboardingDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 60 FPS Solid Bento Theme Manager.
 * Manages theme state (Light / Dark / Auto) via StateFlow & DataStore without heavy UI reload.
 */
object ThemeManager {

    val KEY_THEME_MODE = stringPreferencesKey("theme_mode_preference")

    private val _themeMode = MutableStateFlow("jour") // "jour", "nuit", "auto"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private var isInitialized = false

    fun init(context: Context, scope: CoroutineScope) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext
        scope.launch(Dispatchers.IO) {
            // Read initial preference from DataStore or Room
            val savedMode = appContext.onboardingDataStore.data.map { prefs ->
                prefs[KEY_THEME_MODE]
            }.firstOrNull() ?: try {
                AppDatabase.getInstance(appContext).songDao().getUserProfileOnce()?.themeMode ?: "jour"
            } catch (_: Exception) {
                "jour"
            }

            _themeMode.value = savedMode
            applyNightMode(appContext, savedMode)

            // Listen for continuous updates from DataStore
            appContext.onboardingDataStore.data.map { prefs ->
                prefs[KEY_THEME_MODE] ?: "jour"
            }.collect { mode ->
                if (_themeMode.value != mode) {
                    _themeMode.value = mode
                    applyNightMode(appContext, mode)
                }
            }
        }
    }

    private fun applyNightMode(context: Context, mode: String) {
        val nightMode = when (mode) {
            "nuit" -> AppCompatDelegate.MODE_NIGHT_YES
            "auto" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }

        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        val isDark = when (mode) {
            "nuit" -> true
            "jour" -> false
            else -> {
                val currentNightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                currentNightMask == Configuration.UI_MODE_NIGHT_YES
            }
        }
        _isDarkMode.value = isDark
    }

    suspend fun setThemeMode(context: Context, mode: String) {
        val appContext = context.applicationContext
        _themeMode.value = mode
        applyNightMode(appContext, mode)

        // Save to DataStore
        appContext.onboardingDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }

        // Sync with Room UserProfile
        try {
            val dao = AppDatabase.getInstance(appContext).songDao()
            val existing = dao.getUserProfileOnce()
            if (existing != null) {
                dao.saveUserProfile(existing.copy(themeMode = mode))
            }
        } catch (_: Exception) {
        }
    }

    suspend fun toggleTheme(context: Context) {
        val next = if (_isDarkMode.value) "jour" else "nuit"
        setThemeMode(context, next)
    }

    fun handleConfigurationChange(context: Context) {
        if (_themeMode.value == "auto") {
            val currentNightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            _isDarkMode.value = (currentNightMask == Configuration.UI_MODE_NIGHT_YES)
        }
    }
}
