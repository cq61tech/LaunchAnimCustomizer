package com.cq61tech.launchanimcustomizer

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SettingsStore {
    private lateinit var local: SharedPreferences
    private var remote: SharedPreferences? = null

    var saved by mutableStateOf(Config())
        private set

    var draft by mutableStateOf(Config())
        private set

    var userPresets by mutableStateOf<List<UserPreset>>(emptyList())
        private set

    var dock by mutableStateOf(DockSettings())
        private set

    val dirty: Boolean get() = draft != saved

    fun load(context: Context) {
        local = context.getSharedPreferences(Keys.GROUP, Context.MODE_PRIVATE)
        saved = Config.fromRaw(local.getString(Keys.CONFIG, null))
        draft = saved
        userPresets = userPresetsFromRaw(local.getString(Keys.USER_PRESETS, null))
        dock = DockSettings.fromRaw(local.getString(Keys.DOCK, null))
    }

    fun updateDock(change: (DockSettings) -> DockSettings) {
        dock = change(dock)
        local.edit().putString(Keys.DOCK, dock.toRaw()).apply()
    }

    fun attachRemote(prefs: SharedPreferences?) {
        remote = prefs
        prefs?.edit()?.putString(Keys.CONFIG, saved.toRaw())?.apply()
    }

    fun edit(transform: (Config) -> Config) {
        draft = transform(draft)
    }

    fun apply() {
        commit(draft)
    }

    fun restore(config: Config) {
        commit(config)
    }

    fun setEnabled(value: Boolean) {
        commit(draft.copy(enabled = value))
    }

    fun setDebug(value: Boolean) {
        saved = saved.copy(debug = value)
        draft = draft.copy(debug = value)
        persist(saved)
    }

    fun saveUserPreset(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        val preset = UserPreset(System.currentTimeMillis().toString(), clean, draft.open, draft.close)
        userPresets = userPresets + preset
        local.edit().putString(Keys.USER_PRESETS, userPresetsToRaw(userPresets)).apply()
        draft = draft.copy(mode = Modes.USER_PREFIX + preset.id)
    }

    fun deleteUserPreset(id: String) {
        userPresets = userPresets.filterNot { it.id == id }
        local.edit().putString(Keys.USER_PRESETS, userPresetsToRaw(userPresets)).apply()
        if (draft.mode == Modes.USER_PREFIX + id) draft = draft.copy(mode = Modes.CUSTOM)
    }

    private fun commit(config: Config) {
        saved = config
        draft = config
        persist(config)
    }

    private fun persist(config: Config) {
        val raw = config.toRaw()
        local.edit().putString(Keys.CONFIG, raw).apply()
        remote?.edit()?.putString(Keys.CONFIG, raw)?.apply()
    }
}
