package com.itsha123.autosilent.services.persistence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.itsha123.autosilent.services.location.BackgroundLocationService
import com.itsha123.autosilent.services.location.ServiceIntentProvider.getServiceIntent
import com.itsha123.autosilent.utilities.isServiceRunning

class ServiceCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ServiceCheckReceiver", "Service check triggered")
        if (!isServiceRunning(BackgroundLocationService::class.java, context!!)) {
            Log.i("ServiceCheckReceiver", "BackgroundLocationService is not running, starting it")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(getServiceIntent(context))
            } else {
                context.startService(getServiceIntent(context))
            }
        } else {
            Log.i("ServiceCheckReceiver", "BackgroundLocationService is already running")
        }
    }
}