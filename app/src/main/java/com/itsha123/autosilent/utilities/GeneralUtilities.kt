package com.itsha123.autosilent.utilities

import android.app.ActivityManager
import android.content.Context

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
    val rn = n - 1
    return rn
}