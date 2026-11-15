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

    val dirty: Boolean get() = draft != saved

    fun load(context: Context) {
        local = context.getSharedPreferences(Keys.GROUP, Context.MODE_PRIVATE)
        saved = Config.read(local)
        draft = saved
    }

    fun attachRemote(prefs: SharedPreferences?) {
        remote = prefs
        prefs?.let { saved.write(it) }
    }

    fun edit(transform: (Config) -> Config) {
        draft = transform(draft)
    }

    fun apply() {
        commit(draft)
    }

    fun discard() {
        draft = saved
    }

    fun restore(config: Config) {
        commit(config)
    }

    fun setEnabled(value: Boolean) {
        commit(draft.copy(enabled = value))
    }

    fun setDebug(value: Boolean) {
        commit(saved.copy(debug = value))
        draft = draft.copy(debug = value)
    }

    private fun commit(config: Config) {
        saved = config
        draft = config
        config.write(local)
        remote?.let { config.write(it) }
    }
}
