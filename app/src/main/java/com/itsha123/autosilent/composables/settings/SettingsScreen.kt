package com.itsha123.autosilent.composables.settings

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.settings.components.SettingsItemIconDesc
import com.itsha123.autosilent.services.location.BackgroundLocationService
import com.itsha123.autosilent.singletons.Routes
import com.itsha123.autosilent.singletons.Variables.service
import com.itsha123.autosilent.utilities.isServiceRunning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController? = null, context: Context? = null) {
    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = {
                    navController?.popBackStack()
                    service.value =
                        isServiceRunning(BackgroundLocationService::class.java, context!!)
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
                item {
                    SettingsItemIconDesc(
                        title = stringResource(R.string.general_settings_title),
                        icon = Icons.Rounded.Settings,
                        navController = navController,
                        navigateTo = Routes.GENERALSETTINGS,
                        description = stringResource(R.string.general_settings_desc)
                    )
                }
                item {
                    SettingsItemIconDesc(
                        title = stringResource(R.string.cache_settings_title),
                        description = stringResource(R.string.cache_settings_desc),
                        icon = Icons.Rounded.Mosque,
                        navController = navController,
                        navigateTo = Routes.CACHESETTINGS
                    )
                }
                item {
                    SettingsItemIconDesc(
                        title = stringResource(R.string.help_settings_title),
                        description = stringResource(R.string.help_settings_desc),
                        icon = Icons.AutoMirrored.Rounded.Help,
                        navController = navController,
                        navigateTo = Routes.HELP
                    )
                }
            }
        }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    SettingsScreen()
}
