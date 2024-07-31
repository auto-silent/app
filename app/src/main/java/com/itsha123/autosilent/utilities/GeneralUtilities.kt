package com.itsha123.autosilent.utilities

import android.app.ActivityManager
import android.content.Context
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
