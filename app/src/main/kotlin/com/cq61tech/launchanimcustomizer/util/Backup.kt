package com.cq61tech.launchanimcustomizer.util

import android.content.Context
import android.net.Uri
import com.cq61tech.launchanimcustomizer.Config
import com.cq61tech.launchanimcustomizer.Presets
import com.cq61tech.launchanimcustomizer.Spring
import org.json.JSONObject

object Backup {
    fun export(context: Context, uri: Uri, config: Config): Boolean = runCatching {
        val json = JSONObject()
            .put("enabled", config.enabled)
            .put("mode", config.mode)
            .put("openStiffness", config.open.stiffness.toDouble())
            .put("openDamping", config.open.damping.toDouble())
            .put("closeStiffness", config.close.stiffness.toDouble())
            .put("closeDamping", config.close.damping.toDouble())
            .put("debug", config.debug)
        context.contentResolver.openOutputStream(uri, "wt")!!.use {
            it.write(json.toString(2).toByteArray())
        }
    }.isSuccess

    fun import(context: Context, uri: Uri): Config? = runCatching {
        val text = context.contentResolver.openInputStream(uri)!!.use {
            it.readBytes().toString(Charsets.UTF_8)
        }
        val json = JSONObject(text)
        val base = Config()
        Config(
            enabled = json.optBoolean("enabled", base.enabled),
            mode = json.optString("mode", Presets.CLASSIC),
            open = Spring(
                json.optDouble("openStiffness", base.open.stiffness.toDouble()).toFloat(),
                json.optDouble("openDamping", base.open.damping.toDouble()).toFloat()
            ),
            close = Spring(
                json.optDouble("closeStiffness", base.close.stiffness.toDouble()).toFloat(),
                json.optDouble("closeDamping", base.close.damping.toDouble()).toFloat()
            ),
            debug = json.optBoolean("debug", base.debug)
        )
    }.getOrNull()
}
