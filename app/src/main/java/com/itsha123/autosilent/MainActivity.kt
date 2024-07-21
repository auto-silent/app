package com.itsha123.autosilent

import SettingsScreen
import android.Manifest
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.itsha123.autosilent.ui.theme.AutoSilentTheme
import com.opencsv.CSVReader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.StringReader
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private lateinit var fusedLocationClient: FusedLocationProviderClient

data class LocationData(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double
)

data class GeofenceData(
    val name: String?, val address: String?, val inGeofence: Boolean
)

private var locationData: LocationData? = null

private var geofenceData: GeofenceData? = null
var geofence = MutableStateFlow(false)
var buttonText = MutableStateFlow("Turn Off")

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    @Composable
    fun MainScreen(navController: NavController) {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.revokeSelfPermissionOnKill(Manifest.permission.ACCESS_FINE_LOCATION)
                this.revokeSelfPermissionOnKill(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        AutoSilentTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
                    DNDPermissionRequestScreen {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    }
                } else if (ActivityCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    LocationPermissionRequestScreen {
                        ActivityCompat.requestPermissions(
                            this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                        )
                    }
                } else if (ActivityCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    BackgroundLocationPermissionRequestScreen {
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            1
                        )
                    }
                } else if (ActivityCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationsPermissionRequestScreen {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                1
                            )
                        }
                    }
                } else {
                    val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                    if (sharedPref.getBoolean("firstRun", true)) {
                        with(sharedPref.edit()) {
                            putBoolean("firstRun", false)
                            apply()
                        }
                        if (!isServiceRunning(BackgroundLocationService::class.java, this)) {
                            startForegroundService(
                                Intent(
                                    this,
                                    BackgroundLocationService::class.java
                                )
                            )
                        }
                    }
                    UI({
                        if (buttonText.value == "Turn Off") {
                            stopService(Intent(this, BackgroundLocationService::class.java))

                        } else {
                            if (!isServiceRunning(BackgroundLocationService::class.java, this)) {
                                startForegroundService(
                                    Intent(
                                        this,
                                        BackgroundLocationService::class.java
                                    )
                                )
                            }
                        }
                    },
                        if (geofence.collectAsState().value) {
                            "You are in ${geofenceData!!.name} at ${geofenceData!!.address}."
                        } else "You are currently not within a masjid.",
                        geofence.collectAsState().value,
                        {
                            startActivity(
                                this, Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/itsha123/Auto-Silent-Database/issues")
                                ), null
                            )
                        }, navController
                    )
                }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, BackgroundLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isServiceRunning(BackgroundLocationService::class.java, this)) {
            startForegroundService(serviceIntent)
            }
        }
        setContent {
            AutoSilentTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    composable("main") {
                        MainScreen(navController)
                    }
                    composable("settings") {
                        SettingsScreen(navController)
                    }
                    composable("general_settings") {
                        GeneralSettingsScreen(navController, this@MainActivity)
                    }
                    composable("cache_settings") {
                        CacheSettingsScreen(navController, this@MainActivity)
                    }
                }
            }
        }
    }
}
fun isServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun fetchLocationFromInternet(userLat: Double, userLong: Double, context: Context): GeofenceData? {
    var geofence = false
    val filename = "${userLat.toInt()}%2C%20${userLong.toInt()}.csv"
    val file = File(context.filesDir, filename)
    val data: String
    val lastFileUpdate = file.lastModified()
    if (file.exists() && System.currentTimeMillis() - lastFileUpdate < 86400000) {
        Log.i("Internet", "Reading location data from file")
        val inputStream = context.openFileInput(filename)

        // Read the data from the file
        data = inputStream.bufferedReader().use { it.readText() }

        var nextLine: Array<String>?
        val csvReader = CSVReader(StringReader(data))
        while (csvReader.readNext().also { nextLine = it } != null && !geofence) {
            locationData = LocationData(
                nextLine!![n(1)],
                nextLine!![n(2)],
                nextLine!![n(3)].toDouble(),
                nextLine!![n(4)].toDouble(),
                nextLine!![n(5)].toDouble()
            )
            geofence = isUserInGeofence(
                userLat,
                userLong,
                locationData!!.latitude,
                locationData!!.longitude,
                locationData!!.radius
            )
        }

        // Close the input stream
        inputStream.close()
    } else {
        Log.i("Internet", "Fetching location data")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://raw.githubusercontent.com/Auto-Silent/Auto-Silent-Database/main/${userLat.toInt()}%2C%20${userLong.toInt()}.csv")
            .build()
        Log.i("Internet", "Fetching location data")
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val s = response.body?.string()
            // Open file output stream
            val outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)

            // Write the data to the file
            if (s != null) {
                outputStream.write(s.toByteArray())
            }

            // Close the output stream
            outputStream.close()
            var nextLine: Array<String>?
            val csvReader = CSVReader(StringReader(s))
            while (csvReader.readNext().also { nextLine = it } != null && !geofence) {
                locationData = LocationData(
                    nextLine!![n(1)],
                    nextLine!![n(2)],
                    nextLine!![n(3)].toDouble(),
                    nextLine!![n(4)].toDouble(),
                    nextLine!![n(5)].toDouble()
                )
                geofence = isUserInGeofence(
                    userLat,
                    userLong,
                    locationData!!.latitude,
                    locationData!!.longitude,
                    locationData!!.radius
                )
            }
        } catch (e: IOException) {
            Log.e("Internet", "Failed to fetch location data", e)
        }
    }
    Log.i("geofenceEvent", geofence.toString())
    geofenceData = if (geofence) {
        GeofenceData(
            locationData!!.name, locationData!!.address, true
        )
    } else {
        GeofenceData(
            null, null, false
        )
    }
    Log.i("geofenceEvent", geofenceData.toString())
    return geofenceData
}

@OptIn(DelicateCoroutinesApi::class)
fun fetchLocation(context: Context, audioManager: AudioManager) {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY, null
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                GlobalScope.launch(Dispatchers.IO) {
                    geofenceData = fetchLocationFromInternet(
                        location.latitude, location.longitude, context
                    )
                    withContext(Dispatchers.Main) {
                        Log.i("Location", "location updated")
                        geofence.value = geofenceData!!.inGeofence
                        if (geofence.value) {
                            val sharedPref =
                                context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                            if (sharedPref.getBoolean("vibrateChecked", false)) {
                                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                            } else {
                                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                            }
                        } else {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                        }
                    }
                }
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
    Log.i(
        "geofenceEvent",
        "userLat: $userLat, userLng: $userLng, geofenceLat: $geofenceLat, geofenceLng: $geofenceLng, geofenceRadius: $geofenceRadius"
    )
    val earthRadius = 6371

    val latDistance = Math.toRadians(geofenceLat - userLat)
    val lngDistance = Math.toRadians(geofenceLng - userLng)

    val a = sin(latDistance / 2).pow(2.0) + cos(Math.toRadians(userLat)) * cos(
        Math.toRadians(geofenceLat)
    ) * sin(lngDistance / 2).pow(2.0)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    val distance = earthRadius * c

    val geofenceRadiusInKm = geofenceRadius / 1000

    return distance <= geofenceRadiusInKm
}

fun n(n: Int): Int {
    //Function that converts regular counting to computer counting for sanity
    val rn = n - 1
    return rn
}

@Composable
fun UI(
    onClick: () -> Unit,
    geofenceText: String,
    inGeofence: Boolean,
    link: () -> Unit,
    navController: NavController
) {
    val showDialog = remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(onClick = {
            navController.navigate("settings")
            Log.d("test:)", "Settings button clicked")
        }, modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = geofenceText, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (!inGeofence) {
            TextButton(onClick = { showDialog.value = true }) {
                Text(
                    "In a masjid?",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Button(onClick = {
            onClick()
            if (buttonText.value == "Turn Off") {
                buttonText.value = "Turn On"
            } else {
                buttonText.value = "Turn Off"
            }
        }) {
            Text(buttonText.collectAsState().value)
        }
        if (showDialog.value) {
            AlertDialog(onDismissRequest = { showDialog.value = false }, text = {
                Text("It looks like your masjid is not in our database. Please visit our GitHub page and submit an issue to help us add your masjid.")
            }, confirmButton = {
                TextButton(onClick = {
                    link()
                    showDialog.value = false
                }) {
                    Text("Open GitHub")
                }
            }, dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Close")
                }
            })

        }
    }
}


@Composable
@Preview(showBackground = true)
fun UIPreview() {
    AutoSilentTheme {
        UI({}, "You are currently not within a masjid.", false, {}, rememberNavController())
    }
}