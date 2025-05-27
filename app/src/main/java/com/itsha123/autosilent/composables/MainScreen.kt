package com.itsha123.autosilent.composables

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.permissions.BackgroundLocationPermissionRequestScreen
import com.itsha123.autosilent.composables.permissions.DNDPermissionRequestScreen
import com.itsha123.autosilent.composables.permissions.LocationPermissionRequestScreen
import com.itsha123.autosilent.services.location.BackgroundLocationService
import com.itsha123.autosilent.services.location.ServiceIntentProvider.getServiceIntent
import com.itsha123.autosilent.singletons.Routes
import com.itsha123.autosilent.singletons.Variables.database
import com.itsha123.autosilent.singletons.Variables.geofence
import com.itsha123.autosilent.singletons.Variables.geofenceData
import com.itsha123.autosilent.singletons.Variables.internet
import com.itsha123.autosilent.singletons.Variables.location
import com.itsha123.autosilent.singletons.Variables.recompose
import com.itsha123.autosilent.singletons.Variables.service
import com.itsha123.autosilent.utilities.isServiceRunning
import com.itsha123.autosilent.utilities.permsCheck


@Composable
fun MainScreen(navController: NavController, context: Context) {
    val showDialog = remember { mutableStateOf(false) }
    if (showDialog.value) {
        AlertDialog(onDismissRequest = { showDialog.value = false }, text = {
            Text(stringResource(R.string.enable_app))
        }, confirmButton = {
            TextButton(onClick = {
                showDialog.value = false
                navController.navigate(Routes.GENERALSETTINGS)
            }) {
                Text(stringResource(R.string.go_to_settings))
            }
        }, dismissButton = {
            TextButton(onClick = { showDialog.value = false }) {
                Text(stringResource(R.string.close))
            }
        })

    }
    val settingsActivityResultLauncher: ActivityResultLauncher<Intent> =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            recompose.value = !recompose.value
        }
    val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    recompose.collectAsState().value
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (!notificationManager.isNotificationPolicyAccessGranted) {
        DNDPermissionRequestScreen {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
    } else if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted && sharedPref.getInt("fineLocationRequests", 0) < 2) {
                    sharedPref.edit {
                        putInt(
                            "fineLocationRequests",
                            sharedPref.getInt("fineLocationRequests", 0) + 1
                        )
                    }
                }
                recompose.value = !recompose.value
            }
        LocationPermissionRequestScreen(context) {
            if (sharedPref.getInt("fineLocationRequests", 0) < 2) {
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                settingsActivityResultLauncher.launch(intent)
            }
        }
    } else if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    ) {
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted && sharedPref.getInt("backgroundLocationRequests", 0) < 2) {
                    sharedPref.edit {
                        putInt(
                            "backgroundLocationRequests",
                            sharedPref.getInt("backgroundLocationRequests", 0) + 1
                        )
                    }
                }
                recompose.value = !recompose.value
            }
        BackgroundLocationPermissionRequestScreen(context) {
            if (sharedPref.getInt("backgroundLocationRequests", 0) < 2) {
                launcher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                settingsActivityResultLauncher.launch(intent)
            }
        }
    } else {
        if (permsCheck(context)) {
            if (sharedPref.getBoolean("firstRun", true)) {
                with(sharedPref.edit()) {
                    putBoolean("firstRun", false)
                    putInt("fineLocationRequests", 0)
                    putInt("backgroundLocationRequests", 0)
                    putInt("notificationRequests", 0)
                    apply()
                }
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    navController.navigate(Routes.NOTIFICATIONPERMISSION)
                }
                if (!isServiceRunning(BackgroundLocationService::class.java, context)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(
                            Intent(
                                context,
                                BackgroundLocationService::class.java
                            )
                        )
                    } else {
                        context.startService(
                            Intent(
                                context,
                                BackgroundLocationService::class.java
                            )
                        )
                    }
                }
            }
        }
        UI(
            {
                if (isServiceRunning(BackgroundLocationService::class.java, context)) {
                    context.stopService(getServiceIntent(context))
                    service.value = false
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(
                            Intent(
                                context,
                                BackgroundLocationService::class.java
                            )
                        )
                    } else {
                        context.startService(
                            Intent(
                                context,
                                BackgroundLocationService::class.java
                            )
                        )
                    }
                    service.value = true
                }
                if (!context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                        .getBoolean("enabledChecked", true)
                ) {
                    showDialog.value = true
                }
            },
            when {
                !context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                    .getBoolean(
                        "enabledChecked",
                        true
                    ) -> stringResource(R.string.service_not_running)

                !service.collectAsState().value -> stringResource(R.string.service_not_running)
                !location.collectAsState().value -> stringResource(R.string.location_disabled)
                geofence.collectAsState().value -> stringResource(
                    R.string.current_mosque_details,
                    geofenceData!!.name!!,
                    geofenceData!!.address!!
                )

                !database.collectAsState().value -> stringResource(R.string.mosque_not_in_database_status)
                !internet.collectAsState().value -> stringResource(R.string.no_internet_no_cache)
                else -> stringResource(R.string.not_in_mosque)
            },
            geofence.collectAsState().value,
            {
                navController.navigate(Routes.ADDMOSQUES)
            }, navController
        )
    }

}