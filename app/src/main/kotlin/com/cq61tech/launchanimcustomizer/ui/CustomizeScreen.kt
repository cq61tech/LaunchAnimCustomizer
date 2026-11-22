@file:OptIn(ExperimentalLayoutApi::class)

package com.cq61tech.launchanimcustomizer.ui

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.cq61tech.launchanimcustomizer.CloseSettings
import com.cq61tech.launchanimcustomizer.Config
import com.cq61tech.launchanimcustomizer.Curve
import com.cq61tech.launchanimcustomizer.CurveFunction
import com.cq61tech.launchanimcustomizer.CurveSpec
import com.cq61tech.launchanimcustomizer.Limits
import com.cq61tech.launchanimcustomizer.Modes
import com.cq61tech.launchanimcustomizer.OpenSettings
import com.cq61tech.launchanimcustomizer.Presets
import com.cq61tech.launchanimcustomizer.R
import com.cq61tech.launchanimcustomizer.SettingsStore
import com.cq61tech.launchanimcustomizer.Spring
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CustomizeScreen() {
    val draft = SettingsStore.draft
    val context = LocalContext.current
    var advanced by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PreviewCard(draft)
        EnableCard(draft)
        TuningCard(draft)
        PresetsCard(draft)
        UserPresetsCard(draft)
        SectionCard {
            ToggleRow(
                title = stringResource(R.string.advanced_title),
                description = stringResource(R.string.advanced_desc),
                checked = advanced,
                onChange = { advanced = it }
            )
        }
        if (advanced) {
            OpenCard(draft.open)
            CloseCard(draft.close)
        }
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
private fun TuningCard(config: Config) {
    SectionCard {
        SectionTitle(stringResource(R.string.tuning_title))
        val word = when {
            config.speed < 40f -> stringResource(R.string.speed_slow)
            config.speed > 60f -> stringResource(R.string.speed_fast)
            else -> stringResource(R.string.speed_moderate)
        }
        LabeledSlider(
            label = stringResource(R.string.speed_label),
            value = config.speed,
            display = "${config.speed.roundToInt()} · $word",
            range = Limits.LEVEL_MIN..Limits.LEVEL_MAX,
            onChange = { value -> SettingsStore.edit { it.copy(speed = value) } }
        )
        LabeledSlider(
            label = stringResource(R.string.blur_label),
            value = config.blur,
            display = pct(config.blur),
            range = Limits.BLUR_MIN..Limits.BLUR_MAX,
            onChange = { value -> SettingsStore.edit { it.copy(blur = value) } }
        )
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

private fun springFor(value: Spring, speed: Float): SpringSpec<Float> {
    val factor = speed.coerceIn(0.2f, 4f)
    return spring(
        dampingRatio = value.damping.coerceAtLeast(0.05f),
        stiffness = (value.stiffness * factor * factor).coerceAtLeast(1f),
        visibilityThreshold = 0.001f
    )
}

@Composable
private fun PreviewCard(config: Config) {
    val colors = MaterialTheme.colorScheme
    val classic = config.mode == Modes.CLASSIC
    val open = if (classic) OpenSettings() else config.open
    val close = if (classic) CloseSettings() else config.close
    val factor = config.speedFactor
    val openDuration = open.duration / factor
    val driver = remember { Animatable(0f) }
    val closeX = remember { Animatable(0f) }
    val closeY = remember { Animatable(0f) }
    val closeScale = remember { Animatable(0f) }
    var closing by remember { mutableStateOf(false) }
    val mainFunction = remember(open, openDuration) { CurveFunction(open.main, openDuration) }
    val horizontalFunction = remember(open, openDuration) { CurveFunction(open.horizontal ?: open.main, openDuration) }
    val cornerFunction = remember(open, openDuration) { CurveFunction(open.corner ?: open.main, openDuration) }

    LaunchedEffect(open, close, factor) {
        while (true) {
            closing = false
            driver.snapTo(0f)
            delay(350)
            driver.animateTo(
                1f,
                tween(durationMillis = openDuration.roundToInt().coerceAtLeast(30), easing = LinearEasing)
            )
            delay(700)
            closeX.snapTo(1f)
            closeY.snapTo(1f)
            closeScale.snapTo(1f)
            closing = true
            coroutineScope {
                launch { closeX.animateTo(0f, springFor(close.x, factor)) }
                launch {
                    closeY.animateTo(0f, springFor(if (close.split) close.y else close.x, factor))
                }
                launch {
                    closeScale.animateTo(
                        0f,
                        springFor(if (close.split) close.scale else close.x, factor)
                    )
                }
            }
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
                val t = driver.value
                val isClosing = closing
                val px = if (isClosing) closeX.value else horizontalFunction.at(t)
                val py = if (isClosing) closeY.value else mainFunction.at(t)
                val ps = if (isClosing) closeScale.value else mainFunction.at(t)
                val pr = if (isClosing) closeScale.value else cornerFunction.at(t)
                val roundness = if (isClosing) close.roundness else open.roundness
                val pullX = if (isClosing) 1f else open.originX
                val pullY = if (isClosing) 1f else open.originY
                val startX = (cell + (cell - iconSize) / 2f) * pullX
                val startY = (gridTop + cell) * pullY
                val width = lerp(iconSize, size.width, ps).coerceAtLeast(0f)
                val height = lerp(iconSize, size.height, ps).coerceAtLeast(0f)
                val left = lerp(startX, 0f, px)
                val top = lerp(startY, 0f, py)
                val radius = (lerp(iconSize * 0.3f, 36f, pr.coerceIn(0f, 1f)) * roundness).coerceAtLeast(0f)
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
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun PresetsCard(config: Config) {
    SectionCard {
        SectionTitle(stringResource(R.string.presets_title))
        Presets.all.groupBy { it.category }.forEach { (category, presets) ->
            Text(
                text = stringResource(category.label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                presets.forEach { preset ->
                    PresetTile(
                        title = preset.name,
                        selected = config.mode == preset.id,
                        modifier = Modifier,
                        onClick = { SettingsStore.edit { it.withPreset(preset) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun FlowRow(
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        content()
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
    Curve.ACCELERATE -> R.string.curve_accelerate
    Curve.SMOOTH -> R.string.curve_smooth
    Curve.ELASTIC -> R.string.curve_elastic
    Curve.STEPS -> R.string.curve_steps
    Curve.HOLD -> R.string.curve_hold
    Curve.EASE -> R.string.curve_ease
    Curve.BEZIER -> R.string.curve_bezier
    Curve.LINEAR -> R.string.curve_linear
}

private fun pct(value: Float) = "${(value * 100f).roundToInt()}%"

@Composable
private fun CurveEditor(spec: CurveSpec, onChange: (CurveSpec) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Curve.entries.forEach { curve ->
            FilterChip(
                selected = spec.curve == curve,
                onClick = { onChange(spec.copy(curve = curve)) },
                label = { Text(stringResource(curveLabel(curve))) }
            )
        }
    }
    when (spec.curve) {
        Curve.SPRING -> SpringSliders(spec.spring) { onChange(spec.copy(spring = it)) }
        Curve.OVERSHOOT, Curve.ANTICIPATE, Curve.ELASTIC -> {
            LabeledSlider(
                label = stringResource(R.string.tension_label),
                value = spec.tension,
                display = "%.1f".format(spec.tension),
                range = Limits.TENSION_MIN..Limits.TENSION_MAX,
                onChange = { onChange(spec.copy(tension = it)) }
            )
            if (spec.curve == Curve.ELASTIC) {
                LabeledSlider(
                    label = stringResource(R.string.wobble_label),
                    value = spec.aux,
                    display = "%.2f".format(spec.aux),
                    range = 0.1f..8f,
                    onChange = { onChange(spec.copy(aux = it)) }
                )
            }
        }
        Curve.DECELERATE, Curve.ACCELERATE, Curve.SMOOTH -> LabeledSlider(
            label = stringResource(R.string.power_label),
            value = spec.tension,
            display = "%.1f".format(spec.tension),
            range = Limits.TENSION_MIN..Limits.TENSION_MAX,
            onChange = { onChange(spec.copy(tension = it)) }
        )
        Curve.STEPS -> LabeledSlider(
            label = stringResource(R.string.steps_label),
            value = spec.tension,
            display = spec.tension.roundToInt().toString(),
            range = 2f..24f,
            onChange = { onChange(spec.copy(tension = it)) }
        )
        Curve.HOLD -> {
            LabeledSlider(
                label = stringResource(R.string.hold_label),
                value = spec.aux,
                display = pct(spec.aux),
                range = 0f..0.9f,
                onChange = { onChange(spec.copy(aux = it)) }
            )
            LabeledSlider(
                label = stringResource(R.string.power_label),
                value = spec.tension,
                display = "%.1f".format(spec.tension),
                range = Limits.TENSION_MIN..Limits.TENSION_MAX,
                onChange = { onChange(spec.copy(tension = it)) }
            )
        }
        Curve.BEZIER -> {
            LabeledSlider(
                label = stringResource(R.string.bezier_x1),
                value = spec.x1,
                display = "%.2f".format(spec.x1),
                range = 0f..1f,
                onChange = { onChange(spec.copy(x1 = it)) }
            )
            LabeledSlider(
                label = stringResource(R.string.bezier_y1),
                value = spec.y1,
                display = "%.2f".format(spec.y1),
                range = Limits.BEZIER_Y_MIN..Limits.BEZIER_Y_MAX,
                onChange = { onChange(spec.copy(y1 = it)) }
            )
            LabeledSlider(
                label = stringResource(R.string.bezier_x2),
                value = spec.x2,
                display = "%.2f".format(spec.x2),
                range = 0f..1f,
                onChange = { onChange(spec.copy(x2 = it)) }
            )
            LabeledSlider(
                label = stringResource(R.string.bezier_y2),
                value = spec.y2,
                display = "%.2f".format(spec.y2),
                range = Limits.BEZIER_Y_MIN..Limits.BEZIER_Y_MAX,
                onChange = { onChange(spec.copy(y2 = it)) }
            )
        }
        Curve.BOUNCE, Curve.EASE, Curve.LINEAR -> Unit
    }
}

@Composable
private fun ToggleRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
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
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun OpenCard(open: OpenSettings) {
    SectionCard {
        SectionTitle(stringResource(R.string.open_label))
        LabeledSlider(
            label = stringResource(R.string.open_duration),
            value = open.duration,
            display = "${open.duration.roundToInt()} ms",
            range = Limits.DURATION_MIN..Limits.DURATION_MAX,
            onChange = { value -> SettingsStore.edit { it.editOpen { o -> o.copy(duration = value) } } }
        )
        AxisTitle(stringResource(R.string.motion_main))
        CurveEditor(open.main) { value ->
            SettingsStore.edit { it.editOpen { o -> o.copy(main = value) } }
        }
        ToggleRow(
            title = stringResource(R.string.separate_horizontal_title),
            description = stringResource(R.string.separate_horizontal_desc),
            checked = open.horizontal != null,
            onChange = { on ->
                SettingsStore.edit {
                    it.editOpen { o -> o.copy(horizontal = if (on) o.main else null) }
                }
            }
        )
        val horizontal = open.horizontal
        if (horizontal != null) {
            AxisTitle(stringResource(R.string.motion_horizontal))
            CurveEditor(horizontal) { value ->
                SettingsStore.edit { it.editOpen { o -> o.copy(horizontal = value) } }
            }
        }
        ToggleRow(
            title = stringResource(R.string.separate_radius_title),
            description = stringResource(R.string.separate_radius_desc),
            checked = open.corner != null,
            onChange = { on ->
                SettingsStore.edit {
                    it.editOpen { o -> o.copy(corner = if (on) o.main else null) }
                }
            }
        )
        val corner = open.corner
        if (corner != null) {
            AxisTitle(stringResource(R.string.motion_radius))
            CurveEditor(corner) { value ->
                SettingsStore.edit { it.editOpen { o -> o.copy(corner = value) } }
            }
        }
        LabeledSlider(
            label = stringResource(R.string.roundness_label),
            value = open.roundness,
            display = pct(open.roundness),
            range = Limits.ROUNDNESS_MIN..Limits.ROUNDNESS_MAX,
            onChange = { value -> SettingsStore.edit { it.editOpen { o -> o.copy(roundness = value) } } }
        )
        LabeledSlider(
            label = stringResource(R.string.shadow_label),
            value = open.shadow,
            display = pct(open.shadow),
            range = Limits.SHADOW_MIN..Limits.SHADOW_MAX,
            onChange = { value -> SettingsStore.edit { it.editOpen { o -> o.copy(shadow = value) } } }
        )
        LabeledSlider(
            label = stringResource(R.string.origin_x),
            value = open.originX,
            display = pct(open.originX),
            range = Limits.ORIGIN_MIN..Limits.ORIGIN_MAX,
            onChange = { value -> SettingsStore.edit { it.editOpen { o -> o.copy(originX = value) } } }
        )
        LabeledSlider(
            label = stringResource(R.string.origin_y),
            value = open.originY,
            display = pct(open.originY),
            range = Limits.ORIGIN_MIN..Limits.ORIGIN_MAX,
            onChange = { value -> SettingsStore.edit { it.editOpen { o -> o.copy(originY = value) } } }
        )
        Text(
            text = stringResource(R.string.origin_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LabeledSlider(
            label = stringResource(R.string.fade_start),
            value = open.fadeStart,
            display = pct(open.fadeStart),
            range = 0f..0.95f,
            onChange = { value -> SettingsStore.edit { it.editOpen { o -> o.copy(fadeStart = value) } } }
        )
        LabeledSlider(
            label = stringResource(R.string.fade_end),
            value = open.fadeEnd,
            display = pct(open.fadeEnd),
            range = 0.05f..1f,
            onChange = { value -> SettingsStore.edit { it.editOpen { o -> o.copy(fadeEnd = value) } } }
        )
    }
}

@Composable
private fun CloseCard(close: CloseSettings) {
    SectionCard {
        SectionTitle(stringResource(R.string.close_label))
        ToggleRow(
            title = stringResource(R.string.split_title),
            description = stringResource(R.string.split_desc),
            checked = close.split,
            onChange = { on ->
                SettingsStore.edit { config ->
                    config.editClose { c ->
                        if (on) c.copy(split = true, y = c.x, scale = c.x) else c.copy(split = false)
                    }
                }
            }
        )
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
            label = stringResource(R.string.roundness_label),
            value = close.roundness,
            display = pct(close.roundness),
            range = Limits.ROUNDNESS_MIN..Limits.ROUNDNESS_MAX,
            onChange = { value -> SettingsStore.edit { it.editClose { c -> c.copy(roundness = value) } } }
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
