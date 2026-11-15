package com.cq61tech.launchanimcustomizer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cq61tech.launchanimcustomizer.ModuleState
import com.cq61tech.launchanimcustomizer.R

private const val REPOSITORY = "https://github.com/cq61tech/LaunchAnimCustomizer"

@Composable
fun HomeScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Banner()
        StatusCard()
        FeaturesCard()
        CreditsCard()
    }
}

@Composable
private fun Banner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(MaterialTheme.shapes.extraLarge)
    ) {
        Image(
            painter = painterResource(R.drawable.banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xB3000000))
                    )
                )
        )
        Text(
            text = stringResource(R.string.banner_quote),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        )
    }
}

@Composable
private fun StatusCard() {
    val info = ModuleState.info
    val active = info != null
    val dark = isSystemInDarkTheme()
    val base = if (active) Color(0xFF2E7D32) else Color(0xFFC62828)
    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val container = lerp(lerp(base, surface, if (dark) 0.72f else 0.8f), primary, 0.08f)
    val content = if (dark) lerp(base, Color.White, 0.78f) else lerp(base, Color.Black, 0.62f)
    val muted = content.copy(alpha = 0.72f)

    SectionCard(container = container) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (active) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(34.dp)
            )
            Box(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(
                    if (active) R.string.status_active else R.string.status_inactive
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = content
            )
        }
        if (info != null) {
            val label = if (info.version.isNotEmpty()) "${info.name} ${info.version}" else info.name
            val value = if (info.api > 0) "$label · API ${info.api}" else label
            KeyValueRow(stringResource(R.string.status_framework), value, muted, content)
        }
        Text(
            text = stringResource(
                if (active) R.string.status_hint_active else R.string.status_hint_inactive
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = content
        )
    }
}

@Composable
private fun FeaturesCard() {
    SectionCard(container = MaterialTheme.colorScheme.primaryContainer) {
        val on = MaterialTheme.colorScheme.onPrimaryContainer
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = on, modifier = Modifier.size(30.dp))
            Box(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = stringResource(R.string.features_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = on
                )
                Text(
                    text = stringResource(R.string.features_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = on
                )
            }
        }
        listOf(
            R.string.feature_presets,
            R.string.feature_curves,
            R.string.feature_close,
            R.string.feature_live
        ).forEach {
            Text(
                text = "•  " + stringResource(it),
                style = MaterialTheme.typography.bodyLarge,
                color = on
            )
        }
    }
}

@Composable
private fun CreditsCard() {
    val uri = LocalUriHandler.current
    SectionCard {
        SectionTitle(stringResource(R.string.credits_title))
        Text(
            text = stringResource(R.string.credits_body),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.credits_fonts),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = { uri.openUri(REPOSITORY) }) {
            Text(stringResource(R.string.credits_github))
        }
    }
}
