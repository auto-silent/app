package com.itsha123.autosilent.composables.settings

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.settings.components.SettingsItemSwitch
import com.itsha123.autosilent.utilities.isServiceRunning
import com.itsha123.autosilent.utilities.service.BackgroundLocationService


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(navController: NavController? = null, context: Context? = null) {
    val sharedPref = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    Column {
        MediumTopAppBar(
            title = { Text(stringResource(R.string.general_settings_title)) },
            navigationIcon = {
            IconButton(onClick = {
                navController?.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        })
        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                        if (!isServiceRunning(BackgroundLocationService::class.java, context!!)) {
                            context.startForegroundService(
                                Intent(
                                    context,
                                    BackgroundLocationService::class.java
                                )
                            )
                        }
                    } else {
                        context?.stopService(Intent(context, BackgroundLocationService::class.java))
                    }
                }, "enabledChecked", true)
            }
            item {
                SettingsItemSwitch(
                    title = stringResource(R.string.vibrate_toggle_title),
                    context,
                    onCheck = {
                    var vibrateChecked = sharedPref?.getBoolean("vibrateChecked", false)
                    if (sharedPref != null) {
                        with(sharedPref.edit()) {
                            putBoolean("vibrateChecked", !vibrateChecked!!)
                            apply()
                        }
                        vibrateChecked = !vibrateChecked!!
                        if (vibrateChecked == true) {
                            val audioManager =
                                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                            }
                        } else {
                            val audioManager =
                                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            if (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                            }
                        }
                    }
                }, "vibrateChecked", false)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GeneralSettingsScreenPreview() {
    GeneralSettingsScreen()
}