package com.itsha123.autosilent.utilities

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.net.HttpURLConnection
import java.net.URL

fun isServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun n(n: Int): Int {
    //Function that converts regular counting to computer counting for sanity
    return n - 1
}


fun isInternetAvailable(): Boolean {
    val url = URL("https://www.google.com")
    return try {
        (url.openConnection() as HttpURLConnection).run {
            requestMethod = "HEAD"
            connectTimeout = 10000
            responseCode in 200..299
        }
    } catch (e: Exception) {
        false
    }
}

fun permsCheck(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ) == PackageManager.PERMISSION_GRANTED
}