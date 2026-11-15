package com.cq61tech.launchanimcustomizer.util

import android.content.Context
import android.net.Uri
import com.cq61tech.launchanimcustomizer.Config
import org.json.JSONObject

object Backup {
    fun export(context: Context, uri: Uri, config: Config): Boolean = runCatching {
        val text = JSONObject(config.toRaw()).toString(2)
        context.contentResolver.openOutputStream(uri, "wt")!!.use {
            it.write(text.toByteArray())
        }
    }.isSuccess

    fun import(context: Context, uri: Uri): Config? = runCatching {
        val text = context.contentResolver.openInputStream(uri)!!.use {
            it.readBytes().toString(Charsets.UTF_8)
        }
        Config.parse(text)
    }.getOrNull()
}
