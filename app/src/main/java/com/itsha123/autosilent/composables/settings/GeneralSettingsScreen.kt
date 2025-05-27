package com.itsha123.autosilent.composables.settings

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.settings.components.SettingsItemDropdown
import com.itsha123.autosilent.composables.settings.components.SettingsItemSwitch
import com.itsha123.autosilent.services.location.BackgroundLocationService
import com.itsha123.autosilent.services.location.ServiceIntentProvider.getServiceIntent
import com.itsha123.autosilent.singletons.Variables.geofence
import com.itsha123.autosilent.singletons.Variables.service
import com.itsha123.autosilent.ui.theme.AutoSilentTheme
import com.itsha123.autosilent.utilities.isServiceRunning


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(navController: NavController? = null, context: Context? = null) {
    Scaffold(topBar = {
        MediumTopAppBar(
            colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            title = { Text(stringResource(R.string.general_settings_title)) },
            navigationIcon = {
                IconButton(onClick = {
                    navController?.popBackStack()
                    service.value =
                        isServiceRunning(BackgroundLocationService::class.java, context!!)
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            })
    }) { innerPadding ->
        val sharedPref = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
                item {
                    SettingsItemSwitch(stringResource(R.string.app_toggle_title), context, {
                        var enabledChecked = sharedPref?.getBoolean("enabledChecked", true)
                        if (sharedPref != null) {
                            with(sharedPref.edit()) {
                                putBoolean("enabledChecked", !enabledChecked!!)
                                apply()
                            }
                            enabledChecked = !enabledChecked!!
                        }
                        if (enabledChecked == true) {
                            if (!isServiceRunning(
                                    BackgroundLocationService::class.java, context!!
                                )
                            ) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(
                                        Intent(
                                            context, BackgroundLocationService::class.java
                                        )
                                    )
                                } else {
                                    context.startService(
                                        Intent(
                                            context, BackgroundLocationService::class.java
                                        )
                                    )
                                }
                            }
                        } else {
                            context?.stopService(
                                getServiceIntent(context)
                            )
                        }
                    }, "enabledChecked", true)
                }
                item {
                    SettingsItemDropdown(
                        stringResource(R.string.vibrate_toggle_title),
                        context,
                        "vibrateChecked",
                        false,
                        listOf("Silent", "Vibrate")
                    ) {
                        var vibrateChecked = sharedPref?.getBoolean("vibrateChecked", false)
                        if (sharedPref != null) {
                            with(sharedPref.edit()) {
                                putBoolean("vibrateChecked", !vibrateChecked!!)
                                apply()
                            }
                            vibrateChecked = !vibrateChecked!!
                            if (vibrateChecked == true) {
                                if (geofence.value) {
                                    val audioManager =
                                        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                    if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                                        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                                    }
                                }
                            } else {
                                if (geofence.value) {
                                    val audioManager =
                                        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                    if (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                                        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                                        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
}

@Preview(showBackground = true)
@Composable
fun GeneralSettingsScreenPreview() {
    AutoSilentTheme {
        GeneralSettingsScreen()
    }
}