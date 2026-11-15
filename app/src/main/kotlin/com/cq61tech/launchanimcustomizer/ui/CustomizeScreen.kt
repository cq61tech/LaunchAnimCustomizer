package com.cq61tech.launchanimcustomizer.ui

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.cq61tech.launchanimcustomizer.Config
import com.cq61tech.launchanimcustomizer.Limits
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
                    checked = draft.enabled,
                    onCheckedChange = { value -> SettingsStore.edit { it.copy(enabled = value) } }
                )
            }
        }
        PresetCard(draft)
        SpringCard(
            title = stringResource(R.string.open_label),
            spring = draft.open,
            onChange = { value ->
                SettingsStore.edit { it.copy(mode = Presets.CUSTOM, open = value) }
            }
        )
        SpringCard(
            title = stringResource(R.string.close_label),
            spring = draft.close,
            onChange = { value ->
                SettingsStore.edit { it.copy(mode = Presets.CUSTOM, close = value) }
            }
        )
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
private fun PreviewCard(config: Config) {
    val colors = MaterialTheme.colorScheme
    val progress = remember { Animatable(0f) }
    val open = if (config.mode == Presets.CLASSIC) Spring(800f, 0.9f) else config.open
    val close = if (config.mode == Presets.CLASSIC) Spring(800f, 0.9f) else config.close

    LaunchedEffect(open, close) {
        progress.snapTo(0f)
        delay(350)
        while (true) {
            progress.animateTo(
                1f,
                spring(open.damping, open.stiffness, 0.001f)
            )
            delay(700)
            progress.animateTo(
                0f,
                spring(close.damping, close.stiffness, 0.001f)
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
                val startX = cell * 1 + (cell - iconSize) / 2f
                val startY = size.height * 0.62f
                for (row in 0 until 3) {
                    for (column in 0 until columns) {
                        if (row == 1 && column == 1) continue
                        drawRoundRect(
                            color = colors.outlineVariant,
                            topLeft = Offset(
                                column * cell + (cell - iconSize) / 2f,
                                size.height * 0.38f + row * cell
                            ),
                            size = Size(iconSize, iconSize),
                            cornerRadius = CornerRadius(iconSize * 0.3f)
                        )
                    }
                }
                val p = progress.value
                val width = lerp(iconSize, size.width, p).coerceAtLeast(0f)
                val height = lerp(iconSize, size.height, p).coerceAtLeast(0f)
                val left = lerp(startX, 0f, p)
                val top = lerp(size.height * 0.38f + cell, 0f, p)
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
private fun PresetCard(config: Config) {
    SectionCard {
        SectionTitle(stringResource(R.string.presets_title))
        Presets.all.forEach { preset ->
            val selected = config.mode == preset.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { SettingsStore.edit { it.withPreset(preset) } }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected,
                    onClick = { SettingsStore.edit { it.withPreset(preset) } }
                )
                Column {
                    Text(
                        text = stringResource(preset.name),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(preset.description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = config.mode == Presets.CUSTOM, onClick = null, enabled = false)
            Text(
                text = stringResource(R.string.preset_custom),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SpringCard(title: String, spring: Spring, onChange: (Spring) -> Unit) {
    SectionCard {
        SectionTitle(title)
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
