package com.itsha123.autosilent

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.itsha123.autosilent.ui.theme.AutoSilentTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private lateinit var fusedLocationClient: FusedLocationProviderClient

class MainActivity : ComponentActivity() {
    @Composable
    fun MainScreen() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setContent {
            AutoSilentTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
                    DNDPermissionRequestScreen {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    }
                } else {
                    val geofence = remember { mutableStateOf(false) }

                    fun fetchLocation() {
                        if (ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                1
                            )
                        } else {
                            fusedLocationClient.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY,
                                null
                            )
                                .addOnSuccessListener { location: Location? ->
                                    geofence.value = isUserInGeofence(
                                        location!!.latitude,
                                        location.longitude,
                                        REDACTED,
                                        REDACTED,
                                        50.0
                                    )
                                    Log.i("Location", "location updated")

                                    if (geofence.value) {
                                        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                                    } else {
                                        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                                    }
                                }
                        }
                    }

                    fetchLocation()

                    UI(
                        { fetchLocation() },
                        if (geofence.value) "Inside Geofence" else "Outside Geofence"
                    )
                }

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoSilentTheme {
                MainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setContent {
            AutoSilentTheme {
                MainScreen()

            }
        }
                }
            }

fun isUserInGeofence(
    userLat: Double,
    userLng: Double,
    geofenceLat: Double,
    geofenceLng: Double,
    geofenceRadius: Double
): Boolean {
    val earthRadius = 6371

    val latDistance = Math.toRadians(geofenceLat - userLat)
    val lngDistance = Math.toRadians(geofenceLng - userLng)

    val a = sin(latDistance / 2).pow(2.0) +
            cos(Math.toRadians(userLat)) * cos(Math.toRadians(geofenceLat)) *
            sin(lngDistance / 2).pow(2.0)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    val distance = earthRadius * c

    val geofenceRadiusInKm = geofenceRadius / 1000

    return distance <= geofenceRadiusInKm
}

@Composable
fun UI(onClick: () -> Unit, geofenceText: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = geofenceText, modifier = Modifier.padding(16.dp))
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onClick) {
            Text("Update Location")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AutoSilentTheme {
        UI({}, "preview")
    }
}
//Add help message