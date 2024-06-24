package com.itsha123.autosilent

import android.Manifest
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.itsha123.autosilent.ui.theme.AutoSilentTheme
import com.opencsv.CSVReader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

class MainActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
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
                } else if (ActivityCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    LocationPermissionRequestScreen {
                        ActivityCompat.requestPermissions(
                            this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                        )
                    }
                } else {
                    val geofence = remember { mutableStateOf(false) }

                    fun fetchLocation() {
                        if (ActivityCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                        } else {
                            fusedLocationClient.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY, null
                            ).addOnSuccessListener { location: Location? ->
                                    if (location != null) {
                                        GlobalScope.launch(Dispatchers.IO) {
                                            geofenceData = fetchLocationFromInternet(
                                                location.latitude, location.longitude, this@MainActivity
                                            )
                                            withContext(Dispatchers.Main) {
                                                Log.i("Location", "location updated")
                                                geofence.value = geofenceData!!.inGeofence
                                                if (geofence.value) {
                                                    audioManager.ringerMode =
                                                        AudioManager.RINGER_MODE_SILENT
                                                } else {
                                                    audioManager.ringerMode =
                                                        AudioManager.RINGER_MODE_NORMAL
                                                }
                                            }
                                        }
                                    }
                                }
                        }
                    }

                    fetchLocation()

                    UI(
                        { fetchLocation() }, if (geofence.value) {
                            "You are in ${geofenceData!!.name} at ${geofenceData!!.address}."
                        } else "You are currently not within a masjid.", geofence.value,
                        {
                            startActivity(
                                this,
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/itsha123/Auto-Silent-Database/issues")
                                ), null
                            )
                        }
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


fun fetchLocationFromInternet(userLat: Double, userLong: Double, context: Context): GeofenceData? {
    var geofence = false
    val filename = "${userLat.toInt()}%2C%20${userLong.toInt()}.csv"
    val file = File(context.filesDir, filename)
    val data: String
    if (file.exists()) {
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
            .url("https://raw.githubusercontent.com/itsha123/Auto-Silent-Database/main/${userLat.toInt()}%2C%20${userLong.toInt()}.csv")
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
fun UI(onClick: () -> Unit, geofenceText: String, inGeofence : Boolean, link: () -> Unit) {
    val showDialog = remember { mutableStateOf(false) }

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
            Text(
                text = "In a masjid?",
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showDialog.value = true }
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Button(onClick = onClick) {
            Text("Update Location")
        }
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                text = {
                    Text("It looks like your masjid is not in our database. Please visit our GitHub page and submit an issue to help us add your masjid.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        link()
                        showDialog.value = false
                    }) {
                        Text("Open GitHub")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("Close")
                    }
                }
            )

        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AutoSilentTheme {
        UI({}, "preview", false, {})
    }
}