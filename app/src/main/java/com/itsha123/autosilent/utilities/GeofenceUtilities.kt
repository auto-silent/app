package com.itsha123.autosilent.utilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.itsha123.autosilent.singletons.Variables.geofence
import com.itsha123.autosilent.singletons.Variables.geofenceData
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

data class GeofenceData(
    val name: String?, val address: String?, val inGeofence: Boolean
)

data class LocationData(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double
)

lateinit var fusedLocationClient: FusedLocationProviderClient
var locationData: LocationData? = null

fun fetchLocationFromInternet(userLat: Double, userLong: Double, context: Context): GeofenceData? {
    var geofence = false
    val filename = "${userLat.toInt()}%2C%20${userLong.toInt()}.csv"
    val file = File(context.filesDir, filename)
    val data: String
    val lastFileUpdate = file.lastModified()
    if (file.exists() && System.currentTimeMillis() - lastFileUpdate < 86400000) {
        Log.i("geofenceEvent", "Reading location data from file: $filename")
        val inputStream = context.openFileInput(filename)

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

        inputStream.close()
    } else {
        Log.i("geofenceEvent", "Fetching location data")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://raw.githubusercontent.com/Auto-Silent/Auto-Silent-Database/main/${userLat.toInt()}%2C%20${userLong.toInt()}.csv")
            .build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val s = response.body?.string()
            val outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)

            if (s != null) {
                outputStream.write(s.toByteArray())
            }

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
    Log.d(
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