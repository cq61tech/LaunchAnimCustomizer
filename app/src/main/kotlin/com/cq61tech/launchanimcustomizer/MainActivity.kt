package com.cq61tech.launchanimcustomizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cq61tech.launchanimcustomizer.ui.AppTheme
import com.cq61tech.launchanimcustomizer.ui.CustomizeScreen
import com.cq61tech.launchanimcustomizer.ui.FloatingNavBar
import com.cq61tech.launchanimcustomizer.ui.HomeScreen
import com.cq61tech.launchanimcustomizer.ui.NavItem
import com.cq61tech.launchanimcustomizer.ui.SettingsScreen

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
    val items = listOf(
        NavItem(stringResource(R.string.nav_home), Icons.Filled.Home),
        NavItem(stringResource(R.string.nav_customize), Icons.Filled.Edit),
        NavItem(stringResource(R.string.nav_settings), Icons.Filled.Settings)
    )
    Box {
        Crossfade(targetState = tab, label = "tab") { page ->
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 140.dp)
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        )
    }
}
