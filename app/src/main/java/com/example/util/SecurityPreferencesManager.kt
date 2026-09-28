package com.example.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages advanced security preferences:
 * - Scrambled PIN Keypad
 * - Auto-Lock Vault Inactivity & Background Timeout (10s, 30s, 60s)
 */
object SecurityPreferencesManager {

    private const val PREFS_NAME = "krypton_security_suite_prefs"
    private const val KEY_SCRAMBLE_KEYPAD = "scramble_keypad_enabled"
    private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout_seconds"
    private const val KEY_LAST_BACKGROUND_TIME = "last_background_timestamp"
    private const val KEY_THEME_ACCENT = "krypton_theme_accent"

    private val _themeAccentFlow = kotlinx.coroutines.flow.MutableStateFlow(com.example.ui.theme.KryptonThemeAccent.MONOCHROME)
    val themeAccentFlow: kotlinx.coroutines.flow.StateFlow<com.example.ui.theme.KryptonThemeAccent> = _themeAccentFlow

    fun init(context: Context) {
        val id = getPrefs(context).getString(KEY_THEME_ACCENT, com.example.ui.theme.KryptonThemeAccent.MONOCHROME.id)
        _themeAccentFlow.value = com.example.ui.theme.KryptonThemeAccent.fromId(id)
    }

    fun getSelectedTheme(context: Context): com.example.ui.theme.KryptonThemeAccent {
        val id = getPrefs(context).getString(KEY_THEME_ACCENT, com.example.ui.theme.KryptonThemeAccent.MONOCHROME.id)
        val theme = com.example.ui.theme.KryptonThemeAccent.fromId(id)
        if (_themeAccentFlow.value != theme) {
            _themeAccentFlow.value = theme
        }
        return theme
    }

    fun setSelectedTheme(context: Context, theme: com.example.ui.theme.KryptonThemeAccent) {
        getPrefs(context).edit().putString(KEY_THEME_ACCENT, theme.id).commit()
        _themeAccentFlow.value = theme
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isScrambleKeypadEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SCRAMBLE_KEYPAD, false)
    }

    fun setScrambleKeypadEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SCRAMBLE_KEYPAD, enabled).apply()
    }

    /**
     * Returns auto-lock timeout in seconds: 10, 30, 60 (default: 30).
     */
    fun getAutoLockTimeoutSeconds(context: Context): Int {
        return getPrefs(context).getInt(KEY_AUTO_LOCK_TIMEOUT, 30)
    }

    fun setAutoLockTimeoutSeconds(context: Context, seconds: Int) {
        getPrefs(context).edit().putInt(KEY_AUTO_LOCK_TIMEOUT, seconds).apply()
    }

    fun getLastBackgroundTimestamp(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_BACKGROUND_TIME, 0L)
    }

    fun setLastBackgroundTimestamp(context: Context, timestamp: Long) {
        getPrefs(context).edit().putLong(KEY_LAST_BACKGROUND_TIME, timestamp).apply()
    }

    // Deprecated disguise compatibility stubs
    fun isCalculatorDisguiseEnabled(context: Context): Boolean = false
    fun setCalculatorDisguiseEnabled(context: Context, enabled: Boolean) {}
    fun getCalculatorSecretCode(context: Context): String = "8942="
    fun setCalculatorSecretCode(context: Context, code: String) {}
}
