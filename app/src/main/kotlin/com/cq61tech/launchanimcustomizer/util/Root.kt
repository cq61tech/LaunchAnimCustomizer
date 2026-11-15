package com.cq61tech.launchanimcustomizer.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Root {
    data class Result(val code: Int, val output: String) {
        val ok: Boolean get() = code == 0
    }

    suspend fun run(command: String): Result = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            Result(process.waitFor(), output)
        } catch (e: Exception) {
            Result(-1, e.message.orEmpty())
        }
    }

    fun homePackage(context: Context): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return context.packageManager
            .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
    }

    suspend fun restartLauncher(context: Context): Result {
        val target = homePackage(context) ?: return Result(-1, "")
        return run(
            "am force-stop $target; am start -a android.intent.action.MAIN -c android.intent.category.HOME"
        )
    }

    suspend fun restartSystemUi(): Result = run("pkill -f com.android.systemui")

    suspend fun readLog(): Result = run("logcat -d -t 200 -s LaunchAnim:V")
}
