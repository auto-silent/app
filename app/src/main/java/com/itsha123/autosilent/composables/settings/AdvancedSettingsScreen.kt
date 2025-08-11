package com.itsha123.autosilent.composables.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.settings.components.SettingsItemSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(navController: NavController? = null, context: Context? = null) {
    var showDialog by remember { mutableStateOf(false) }
    var dontShowAgain by remember { mutableStateOf(false) }

    val sharedPref = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Are you sure?") },
            text = {
                Column {
                    Text("Enabling this option will log your exact location (latitude and longitude). If this log data falls into the wrong hands, it could be used to track your location. Are you sure you want to enable this?")
                    Row(
                        modifier = Modifier.clickable { dontShowAgain = !dontShowAgain },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = dontShowAgain, onCheckedChange = { dontShowAgain = it })
                        Text("Don't show again")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sharedPref?.edit {
                            putBoolean("advancedChecked", true)
                            if (dontShowAgain) {
                                putBoolean("showSensitiveDataDialog", false)
                            }
                        }
                        showDialog = false
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(stringResource(R.string.advanced)) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                SettingsItemSwitch(
                    title = "Log sensitive data",
                    context,
                    onCheck = {
                        val isChecked = sharedPref?.getBoolean("advancedChecked", false)
                        val showAgain = sharedPref?.getBoolean("showSensitiveDataDialog", true)
                        // If the switch is currently checked, the user wants to uncheck it.
                        if (isChecked!!) {
                            sharedPref.edit {
                                putBoolean("advancedChecked", false)
                            }
                        } else { // If the switch is unchecked, the user wants to check it.
                            if (!showAgain!!) {
                                sharedPref.edit {
                                    putBoolean("advancedChecked", true)
                                }
                            } else {
                                showDialog = true
                            }
                        }
                    }, "advancedChecked", false
                )
            }
        }
    }
}

@Composable
@Preview
fun AdvancedSettingsScreenPreview() {
    AdvancedSettingsScreen()
}