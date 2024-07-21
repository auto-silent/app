package com.itsha123.autosilent

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PixelSettingsItemSwitch(
    title: String,
    context: Context? = null,
    onCheck: () -> Unit,
    syncedVariable: String,
    defValue: Boolean
) {
    val sharedPref = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    var clicked by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(true) }// State to manage Switch checked state
    if (sharedPref != null) {
        isChecked = sharedPref.getBoolean(syncedVariable, defValue)
    }
    val scale = animateFloatAsState(targetValue = if (clicked) 0.95f else 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    clicked = true
                },
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .scale(scale.value)
    ) {
        Column {
            Text(text = title, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.weight(1f)) // Spacer with flexible weight to push the Switch to the right
        Switch(checked = isChecked, onCheckedChange = {
            isChecked = it
            onCheck()
        }) // Update isChecked state on change
    }

    LaunchedEffect(clicked) {
        if (clicked) {
            // Reset the click effect after navigation
            clicked = false
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(navController: NavController? = null, context: Context? = null) {
    val sharedPref = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    Column {
        MediumTopAppBar(title = { Text("General") }, navigationIcon = {
            IconButton(onClick = {
                navController?.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        })
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                PixelSettingsItemSwitch("Enable Auto Silent", context, {
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
                PixelSettingsItemSwitch(title = "Vibrate instead of silent", context, onCheck = {
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