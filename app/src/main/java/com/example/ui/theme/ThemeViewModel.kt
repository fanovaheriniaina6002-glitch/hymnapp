package com.example.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 60 FPS ViewModel exposing Theme state to avoid full tree reloads.
 */
class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    val themeMode: StateFlow<String> = ThemeManager.themeMode
    val isDarkMode: StateFlow<Boolean> = ThemeManager.isDarkMode

    init {
        ThemeManager.init(application, viewModelScope)
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            ThemeManager.setThemeMode(getApplication(), mode)
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            ThemeManager.toggleTheme(getApplication())
        }
    }
}
