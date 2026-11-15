package com.cq61tech.launchanimcustomizer.ui

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.cq61tech.launchanimcustomizer.CloseSettings
import com.cq61tech.launchanimcustomizer.Config
import com.cq61tech.launchanimcustomizer.Curve
import com.cq61tech.launchanimcustomizer.CurveFunction
import com.cq61tech.launchanimcustomizer.Limits
import com.cq61tech.launchanimcustomizer.Modes
import com.cq61tech.launchanimcustomizer.OpenSettings
import com.cq61tech.launchanimcustomizer.Presets
import com.cq61tech.launchanimcustomizer.R
import com.cq61tech.launchanimcustomizer.SettingsStore
import com.cq61tech.launchanimcustomizer.Spring
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun CustomizeScreen() {
    val draft = SettingsStore.draft
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PreviewCard(draft)
        EnableCard(draft)
        PresetsCard(draft)
        UserPresetsCard(draft)
        OpenCard(draft.open)
        CloseCard(draft.close)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    SettingsStore.apply()
                    Toast.makeText(context, R.string.applied_toast, Toast.LENGTH_SHORT).show()
                },
                enabled = SettingsStore.dirty,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.apply))
            }
            OutlinedButton(
                onClick = {
                    SettingsStore.edit { Config(debug = it.debug) }
                    Toast.makeText(context, R.string.reset_toast, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.reset))
            }
        }
    }
}

@Composable
private fun EnableCard(config: Config) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionTitle(stringResource(R.string.enable_title))
                Text(
                    text = stringResource(R.string.enable_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = config.enabled,
                onCheckedChange = { value -> SettingsStore.edit { it.copy(enabled = value) } }
            )
        }
    }
}

@Composable
private fun PreviewCard(config: Config) {
    val colors = MaterialTheme.colorScheme
    val progress = remember { Animatable(0f) }
    val classic = config.mode == Modes.CLASSIC
    val open = if (classic) OpenSettings() else config.open
    val close = if (classic) CloseSettings() else config.close
    val easing = remember(open) {
        val function = CurveFunction(open)
        Easing { function.at(it) }
    }
    val closeSpring = if (close.split) close.scale else close.x
    val closeSpeed = close.speed.coerceIn(0.2f, 4f)
    val closeStiffness = (closeSpring.stiffness * closeSpeed * closeSpeed).coerceAtLeast(1f)

    LaunchedEffect(open, close) {
        progress.snapTo(0f)
        delay(300)
        while (true) {
            progress.animateTo(1f, tween(durationMillis = 500, easing = easing))
            delay(700)
            progress.animateTo(
                0f,
                spring(
                    dampingRatio = closeSpring.damping.coerceAtLeast(0.05f),
                    stiffness = closeStiffness,
                    visibilityThreshold = 0.001f
                )
            )
            delay(900)
        }
    }

    SectionCard {
        SectionTitle(stringResource(R.string.preview_title))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 190.dp)
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .clip(MaterialTheme.shapes.large)
                .background(colors.surfaceContainerHighest)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val columns = 4
                val cell = size.width / columns
                val iconSize = cell * 0.62f
                val gridTop = size.height * 0.38f
                for (row in 0 until 3) {
                    for (column in 0 until columns) {
                        if (row == 1 && column == 1) continue
                        drawRoundRect(
                            color = colors.outlineVariant,
                            topLeft = Offset(
                                column * cell + (cell - iconSize) / 2f,
                                gridTop + row * cell
                            ),
                            size = Size(iconSize, iconSize),
                            cornerRadius = CornerRadius(iconSize * 0.3f)
                        )
                    }
                }
                val p = progress.value
                val startX = cell + (cell - iconSize) / 2f
                val startY = gridTop + cell
                val width = lerp(iconSize, size.width, p).coerceAtLeast(0f)
                val height = lerp(iconSize, size.height, p).coerceAtLeast(0f)
                val left = lerp(startX, 0f, p)
                val top = lerp(startY, 0f, p)
                val radius = lerp(iconSize * 0.3f, 36f, p.coerceIn(0f, 1f))
                drawRoundRect(
                    color = colors.primary,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(radius)
                )
                drawRoundRect(
                    color = colors.primaryContainer,
                    topLeft = Offset(left + width * 0.12f, top + height * 0.08f),
                    size = Size(width * 0.76f, height * 0.06f),
                    cornerRadius = CornerRadius(radius / 2f)
                )
            }
        }
        Text(
            text = stringResource(R.string.preview_note),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun PresetTile(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PresetsCard(config: Config) {
    SectionCard {
        SectionTitle(stringResource(R.string.presets_title))
        Presets.all.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { preset ->
                    PresetTile(
                        title = stringResource(preset.name),
                        subtitle = stringResource(preset.description),
                        selected = config.mode == preset.id,
                        modifier = Modifier.weight(1f),
                        onClick = { SettingsStore.edit { it.withPreset(preset) } }
                    )
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun UserPresetsCard(config: Config) {
    var asking by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    val saved = SettingsStore.userPresets

    SectionCard {
        SectionTitle(stringResource(R.string.user_presets_title))
        if (saved.isEmpty()) {
            Text(
                text = stringResource(R.string.user_presets_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        saved.forEach { preset ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetTile(
                    title = preset.name,
                    subtitle = "",
                    selected = config.mode == Modes.USER_PREFIX + preset.id,
                    modifier = Modifier.weight(1f),
                    onClick = { SettingsStore.edit { it.withUserPreset(preset) } }
                )
                TextButton(onClick = { SettingsStore.deleteUserPreset(preset.id) }) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
        OutlinedButton(
            onClick = { asking = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_preset))
        }
    }

    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text(stringResource(R.string.save_preset)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.preset_name_hint)) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        SettingsStore.saveUserPreset(name)
                        name = ""
                        asking = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { asking = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun curveLabel(curve: Curve): Int = when (curve) {
    Curve.SPRING -> R.string.curve_spring
    Curve.OVERSHOOT -> R.string.curve_overshoot
    Curve.ANTICIPATE -> R.string.curve_anticipate
    Curve.BOUNCE -> R.string.curve_bounce
    Curve.DECELERATE -> R.string.curve_decelerate
    Curve.EASE -> R.string.curve_ease
    Curve.BEZIER -> R.string.curve_bezier
    Curve.LINEAR -> R.string.curve_linear
}

@Composable
private fun OpenCard(open: OpenSettings) {
    SectionCard {
        SectionTitle(stringResource(R.string.open_label))
        Text(
            text = stringResource(R.string.curve_label),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Curve.entries.forEach { curve ->
                FilterChip(
                    selected = open.curve == curve,
                    onClick = {
                        SettingsStore.edit { it.editOpen { o -> o.copy(curve = curve) } }
                    },
                    label = { Text(stringResource(curveLabel(curve))) }
                )
            }
        }
        when (open.curve) {
            Curve.SPRING -> SpringSliders(open.spring) { value ->
                SettingsStore.edit { it.editOpen { o -> o.copy(spring = value) } }
            }
            Curve.OVERSHOOT, Curve.ANTICIPATE -> LabeledSlider(
                label = stringResource(R.string.tension_label),
                value = open.tension,
                display = "%.1f".format(open.tension),
                range = Limits.TENSION_MIN..Limits.TENSION_MAX,
                onChange = { value ->
                    SettingsStore.edit { it.editOpen { o -> o.copy(tension = value) } }
                }
            )
            Curve.DECELERATE -> LabeledSlider(
                label = stringResource(R.string.power_label),
                value = open.tension,
                display = "%.1f".format(open.tension),
                range = Limits.TENSION_MIN..Limits.TENSION_MAX,
                onChange = { value ->
                    SettingsStore.edit { it.editOpen { o -> o.copy(tension = value) } }
                }
            )
            Curve.BEZIER -> {
                LabeledSlider(
                    label = stringResource(R.string.bezier_x1),
                    value = open.x1,
                    display = "%.2f".format(open.x1),
                    range = 0f..1f,
                    onChange = { value ->
                        SettingsStore.edit { it.editOpen { o -> o.copy(x1 = value) } }
                    }
                )
                LabeledSlider(
                    label = stringResource(R.string.bezier_y1),
                    value = open.y1,
                    display = "%.2f".format(open.y1),
                    range = Limits.BEZIER_Y_MIN..Limits.BEZIER_Y_MAX,
                    onChange = { value ->
                        SettingsStore.edit { it.editOpen { o -> o.copy(y1 = value) } }
                    }
                )
                LabeledSlider(
                    label = stringResource(R.string.bezier_x2),
                    value = open.x2,
                    display = "%.2f".format(open.x2),
                    range = 0f..1f,
                    onChange = { value ->
                        SettingsStore.edit { it.editOpen { o -> o.copy(x2 = value) } }
                    }
                )
                LabeledSlider(
                    label = stringResource(R.string.bezier_y2),
                    value = open.y2,
                    display = "%.2f".format(open.y2),
                    range = Limits.BEZIER_Y_MIN..Limits.BEZIER_Y_MAX,
                    onChange = { value ->
                        SettingsStore.edit { it.editOpen { o -> o.copy(y2 = value) } }
                    }
                )
            }
            Curve.BOUNCE, Curve.EASE, Curve.LINEAR -> Unit
        }
        LabeledSlider(
            label = stringResource(R.string.speed_label),
            value = open.speed,
            display = "%.2fx".format(open.speed),
            range = Limits.SPEED_MIN..Limits.SPEED_MAX,
            onChange = { value ->
                SettingsStore.edit { it.editOpen { o -> o.copy(speed = value) } }
            }
        )
        Text(
            text = stringResource(R.string.speed_hint_open),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CloseCard(close: CloseSettings) {
    SectionCard {
        SectionTitle(stringResource(R.string.close_label))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.split_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.split_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = close.split,
                onCheckedChange = { on ->
                    SettingsStore.edit { config ->
                        config.editClose { c ->
                            if (on) c.copy(split = true, y = c.x, scale = c.x) else c.copy(split = false)
                        }
                    }
                }
            )
        }
        if (close.split) {
            AxisTitle(stringResource(R.string.axis_x))
            SpringSliders(close.x) { value ->
                SettingsStore.edit { it.editClose { c -> c.copy(x = value) } }
            }
            AxisTitle(stringResource(R.string.axis_y))
            SpringSliders(close.y) { value ->
                SettingsStore.edit { it.editClose { c -> c.copy(y = value) } }
            }
            AxisTitle(stringResource(R.string.axis_scale))
            SpringSliders(close.scale) { value ->
                SettingsStore.edit { it.editClose { c -> c.copy(scale = value) } }
            }
        } else {
            SpringSliders(close.x) { value ->
                SettingsStore.edit { it.editClose { c -> c.copy(x = value) } }
            }
        }
        LabeledSlider(
            label = stringResource(R.string.speed_label),
            value = close.speed,
            display = "%.2fx".format(close.speed),
            range = Limits.SPEED_MIN..Limits.SPEED_MAX,
            onChange = { value ->
                SettingsStore.edit { it.editClose { c -> c.copy(speed = value) } }
            }
        )
        Text(
            text = stringResource(R.string.speed_hint_close),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AxisTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SpringSliders(spring: Spring, onChange: (Spring) -> Unit) {
    LabeledSlider(
        label = stringResource(R.string.stiffness_label),
        value = spring.stiffness,
        display = spring.stiffness.roundToInt().toString(),
        range = Limits.STIFFNESS_MIN..Limits.STIFFNESS_MAX,
        onChange = { onChange(spring.copy(stiffness = it)) }
    )
    LabeledSlider(
        label = stringResource(R.string.damping_label),
        value = spring.damping,
        display = "%.2f".format(spring.damping),
        range = Limits.DAMPING_MIN..Limits.DAMPING_MAX,
        onChange = { onChange(spring.copy(damping = it)) }
    )
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    display: String,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = display,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(value = value.coerceIn(range), onValueChange = onChange, valueRange = range)
    }
}
