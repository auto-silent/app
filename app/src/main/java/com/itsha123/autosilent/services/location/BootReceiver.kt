package com.itsha123.autosilent.services.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startForegroundService
import com.itsha123.autosilent.utilities.permsCheck

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("backgroundService", "BootReceiver triggered")
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            Log.i("backgroundService", "Boot completed received")
            if (permsCheck(context!!)) {
                val serviceIntent = Intent(context, BackgroundLocationService::class.java)
                startForegroundService(context, serviceIntent)
            } else {
                Log.i("backgroundService", "Permissions not granted")
            }
        }
    }
}