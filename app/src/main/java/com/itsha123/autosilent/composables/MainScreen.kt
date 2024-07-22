package com.itsha123.autosilent.composables

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.itsha123.autosilent.MainActivity
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.permissions.BackgroundLocationPermissionRequestScreen
import com.itsha123.autosilent.composables.permissions.DNDPermissionRequestScreen
import com.itsha123.autosilent.composables.permissions.LocationPermissionRequestScreen
import com.itsha123.autosilent.composables.permissions.NotificationsPermissionRequestScreen
import com.itsha123.autosilent.singletons.Variables.buttonText
import com.itsha123.autosilent.singletons.Variables.geofence
import com.itsha123.autosilent.singletons.Variables.geofenceData
import com.itsha123.autosilent.utilities.isServiceRunning
import com.itsha123.autosilent.utilities.service.BackgroundLocationService

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(navController: NavController, context: Context, activity: MainActivity) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.revokeSelfPermissionOnKill(Manifest.permission.ACCESS_FINE_LOCATION)
            context.revokeSelfPermissionOnKill(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
        DNDPermissionRequestScreen {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
    } else if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        LocationPermissionRequestScreen {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }
    } else if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        BackgroundLocationPermissionRequestScreen {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                1
            )
        }
    } else if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        NotificationsPermissionRequestScreen {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    } else {
        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("firstRun", true)) {
            with(sharedPref.edit()) {
                putBoolean("firstRun", false)
                apply()
            }
            if (!isServiceRunning(BackgroundLocationService::class.java, context)) {
                context.startForegroundService(
                    Intent(
                        context,
                        BackgroundLocationService::class.java
                    )
                )
            }
        }
        UI(
            {
                if (buttonText.value == context.getString(R.string.turn_off)) {
                    context.stopService(Intent(context, BackgroundLocationService::class.java))

                } else {
                    if (!isServiceRunning(BackgroundLocationService::class.java, context)) {
                        context.startForegroundService(
                            Intent(
                                context,
                                BackgroundLocationService::class.java
                            )
                        )
                    }
                }
            },
            if (geofence.collectAsState().value) {
                stringResource(
                    R.string.current_masjid_details,
                    geofenceData!!.name!!,
                    geofenceData!!.address!!
                )
            } else stringResource(R.string.not_in_masjid),
            geofence.collectAsState().value,
            {
                startActivity(
                    context, Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/itsha123/Auto-Silent-Database/issues")
                    ), null
                )
            }, navController, context
        )
    }

}