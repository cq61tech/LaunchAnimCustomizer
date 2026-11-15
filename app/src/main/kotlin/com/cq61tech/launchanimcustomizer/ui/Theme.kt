package com.cq61tech.launchanimcustomizer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cq61tech.launchanimcustomizer.R

@OptIn(ExperimentalTextApi::class)
private fun family(resource: Int, vararg weights: Int): FontFamily =
    FontFamily(
        weights.map { weight ->
            Font(
                resource,
                FontWeight(weight),
                variationSettings = FontVariation.Settings(FontVariation.weight(weight))
            )
        }
    )

private val display = family(R.font.fredoka, 500, 600, 700)
private val body = family(R.font.nunito, 400, 600, 700)
private val label = family(R.font.quicksand, 500, 700)

private fun typography(): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = display),
        displayMedium = base.displayMedium.copy(fontFamily = display),
        displaySmall = base.displaySmall.copy(fontFamily = display),
        headlineLarge = base.headlineLarge.copy(fontFamily = display),
        headlineMedium = base.headlineMedium.copy(fontFamily = display),
        headlineSmall = base.headlineSmall.copy(fontFamily = display, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontFamily = display),
        titleMedium = base.titleMedium.copy(fontFamily = display),
        titleSmall = base.titleSmall.copy(fontFamily = display),
        bodyLarge = base.bodyLarge.copy(fontFamily = body),
        bodyMedium = base.bodyMedium.copy(fontFamily = body),
        bodySmall = base.bodySmall.copy(fontFamily = body),
        labelLarge = base.labelLarge.copy(fontFamily = label, fontWeight = FontWeight.Bold),
        labelMedium = base.labelMedium.copy(fontFamily = label, fontWeight = FontWeight.Bold),
        labelSmall = base.labelSmall.copy(fontFamily = label, fontWeight = FontWeight.Bold)
    )
}

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val scheme = if (isSystemInDarkTheme()) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
    MaterialTheme(colorScheme = scheme, typography = typography(), shapes = shapes) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
