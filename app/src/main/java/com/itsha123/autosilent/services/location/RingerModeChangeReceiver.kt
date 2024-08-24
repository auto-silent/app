package com.itsha123.autosilent.services.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.itsha123.autosilent.singletons.Variables.geofence
import com.itsha123.autosilent.singletons.Variables.ringerMode

class RingerModeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            Log.d("ringerMode", "Ringer mode changed: ${audioManager.ringerMode}")
            if (!geofence.value) {
                ringerMode.value = audioManager.ringerMode
                Log.d("ringerMode", "not in geofence")
            }
        }
    }
}