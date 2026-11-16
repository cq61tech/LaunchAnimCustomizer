package com.cq61tech.launchanimcustomizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.cq61tech.launchanimcustomizer.ui.AppTheme
import com.cq61tech.launchanimcustomizer.ui.CustomizeScreen
import com.cq61tech.launchanimcustomizer.ui.FloatingNavBar
import com.cq61tech.launchanimcustomizer.ui.HomeScreen
import com.cq61tech.launchanimcustomizer.ui.NavItem
import com.cq61tech.launchanimcustomizer.ui.SettingsScreen
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    var tab by rememberSaveable { mutableStateOf(0) }
    var hidden by remember { mutableStateOf(false) }
    val dock = SettingsStore.dock
    val items = listOf(
        NavItem(stringResource(R.string.nav_home), Icons.Filled.Home),
        NavItem(stringResource(R.string.nav_customize), Icons.Filled.Edit),
        NavItem(stringResource(R.string.nav_settings), Icons.Filled.Settings)
    )
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -6f) {
                    hidden = true
                } else if (available.y > 6f) {
                    hidden = false
                }
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(tab) { hidden = false }
    val slide by animateFloatAsState(
        targetValue = if (hidden && dock.hideOnScroll) 1f else 0f,
        label = "dockSlide"
    )
    val placement = if (dock.floating) {
        Modifier
            .navigationBarsPadding()
            .padding(bottom = dock.bottom.dp)
    } else {
        Modifier
    }

    Box(modifier = Modifier.nestedScroll(connection)) {
        Crossfade(targetState = tab, label = "tab") { page ->
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 150.dp)
            ) {
                when (page) {
                    0 -> HomeScreen()
                    1 -> CustomizeScreen()
                    else -> SettingsScreen()
                }
            }
        }
        FloatingNavBar(
            items = items,
            selected = tab,
            onSelect = { tab = it },
            dock = dock,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, ((dock.height + dock.bottom + 160f).dp.toPx() * slide).roundToInt()) }
                .then(placement)
        )
    }
}
