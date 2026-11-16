package com.cq61tech.launchanimcustomizer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cq61tech.launchanimcustomizer.BuildConfig
import com.cq61tech.launchanimcustomizer.DockLimits
import com.cq61tech.launchanimcustomizer.R
import com.cq61tech.launchanimcustomizer.SettingsStore
import com.cq61tech.launchanimcustomizer.util.Backup
import com.cq61tech.launchanimcustomizer.util.Root
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var logText by remember { mutableStateOf<String?>(null) }
    var iconHidden by remember { mutableStateOf(isIconHidden(context)) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val done = Backup.export(context, uri, SettingsStore.saved)
            toast(context, if (done) R.string.backup_done else R.string.backup_failed)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val config = Backup.import(context, uri)
            if (config != null) {
                SettingsStore.restore(config)
                toast(context, R.string.backup_done)
            } else {
                toast(context, R.string.backup_failed)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionCard {
            SectionTitle(stringResource(R.string.maintenance_title))
            Text(
                text = stringResource(R.string.maintenance_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        val result = Root.restartLauncher(context)
                        toast(context, if (result.ok) R.string.root_done else R.string.root_failed)
                    }
                }
            ) {
                Text(stringResource(R.string.restart_launcher))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        val result = Root.restartSystemUi()
                        toast(context, if (result.ok) R.string.root_done else R.string.root_failed)
                    }
                }
            ) {
                Text(stringResource(R.string.restart_systemui))
            }
        }

        DockCard()

        SectionCard {
            SectionTitle(stringResource(R.string.backup_title))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { exportLauncher.launch("launchanim-settings.json") }
            ) {
                Text(stringResource(R.string.backup_export))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }
            ) {
                Text(stringResource(R.string.backup_import))
            }
        }

        SectionCard {
            SectionTitle(stringResource(R.string.app_section_title))
            SwitchRow(
                title = stringResource(R.string.hide_icon),
                description = stringResource(R.string.hide_icon_desc),
                checked = iconHidden,
                onChange = {
                    setIconHidden(context, it)
                    iconHidden = it
                }
            )
            SwitchRow(
                title = stringResource(R.string.debug_title),
                description = stringResource(R.string.debug_desc),
                checked = SettingsStore.saved.debug,
                onChange = { SettingsStore.setDebug(it) }
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        val result = Root.readLog()
                        logText = if (result.ok && result.output.isNotBlank()) {
                            result.output
                        } else {
                            context.getString(R.string.debug_empty)
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.debug_view))
            }
            Text(
                text = stringResource(R.string.version_label, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    logText?.let { text ->
        AlertDialog(
            onDismissRequest = { logText = null },
            title = { Text(stringResource(R.string.debug_title)) },
            text = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { logText = null }) {
                    Text(stringResource(R.string.close))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        manager.setPrimaryClip(ClipData.newPlainText("log", text))
                    }
                ) {
                    Text(stringResource(R.string.debug_copy))
                }
            }
        )
    }
}

@Composable
private fun DockCard() {
    val dock = SettingsStore.dock
    SectionCard {
        SectionTitle(stringResource(R.string.dock_title))
        SwitchRow(
            title = stringResource(R.string.dock_floating),
            description = stringResource(R.string.dock_floating_desc),
            checked = dock.floating,
            onChange = { value -> SettingsStore.updateDock { it.copy(floating = value) } }
        )
        LabeledSlider(
            label = stringResource(R.string.dock_width),
            value = dock.width,
            display = "${(dock.width * 100f).roundToInt()}%",
            range = DockLimits.WIDTH_MIN..DockLimits.WIDTH_MAX,
            enabled = dock.floating,
            onChange = { value -> SettingsStore.updateDock { it.copy(width = value) } }
        )
        LabeledSlider(
            label = stringResource(R.string.dock_height),
            value = dock.height,
            display = "${dock.height.roundToInt()} dp",
            range = DockLimits.HEIGHT_MIN..DockLimits.HEIGHT_MAX,
            onChange = { value -> SettingsStore.updateDock { it.copy(height = value) } }
        )
        LabeledSlider(
            label = stringResource(R.string.dock_opacity),
            value = dock.opacity,
            display = "${(dock.opacity * 100f).roundToInt()}%",
            range = DockLimits.OPACITY_MIN..DockLimits.OPACITY_MAX,
            onChange = { value -> SettingsStore.updateDock { it.copy(opacity = value) } }
        )
        LabeledSlider(
            label = stringResource(R.string.dock_text),
            value = dock.textSize,
            display = "${dock.textSize.roundToInt()} sp",
            range = DockLimits.TEXT_MIN..DockLimits.TEXT_MAX,
            onChange = { value -> SettingsStore.updateDock { it.copy(textSize = value) } }
        )
        LabeledSlider(
            label = stringResource(R.string.dock_bottom),
            value = dock.bottom,
            display = "${dock.bottom.roundToInt()} dp",
            range = DockLimits.BOTTOM_MIN..DockLimits.BOTTOM_MAX,
            enabled = dock.floating,
            onChange = { value -> SettingsStore.updateDock { it.copy(bottom = value) } }
        )
        SwitchRow(
            title = stringResource(R.string.dock_hide),
            description = stringResource(R.string.dock_hide_desc),
            checked = dock.hideOnScroll,
            onChange = { value -> SettingsStore.updateDock { it.copy(hideOnScroll = value) } }
        )
        SwitchRow(
            title = stringResource(R.string.dock_labels),
            description = "",
            checked = dock.labels,
            onChange = { value -> SettingsStore.updateDock { it.copy(labels = value) } }
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private fun toast(context: Context, message: Int) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun aliasName(context: Context) = ComponentName(context, "com.cq61tech.launchanimcustomizer.LauncherAlias")

private fun isIconHidden(context: Context): Boolean =
    context.packageManager.getComponentEnabledSetting(aliasName(context)) ==
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED

private fun setIconHidden(context: Context, hidden: Boolean) {
    context.packageManager.setComponentEnabledSetting(
        aliasName(context),
        if (hidden) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        },
        PackageManager.DONT_KILL_APP
    )
}
