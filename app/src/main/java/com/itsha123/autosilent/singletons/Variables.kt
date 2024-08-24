package com.itsha123.autosilent.singletons

import android.media.AudioManager.RINGER_MODE_NORMAL
import com.itsha123.autosilent.utilities.GeofenceData
import kotlinx.coroutines.flow.MutableStateFlow

object Variables {
    var geofenceData: GeofenceData? = null
    var geofence = MutableStateFlow(false)
    var database = MutableStateFlow(true)
    var internet = MutableStateFlow(true)
    var recompose = MutableStateFlow(false)
    var location = MutableStateFlow(true)
    var serviceui = MutableStateFlow(true)
    var ringerMode = MutableStateFlow(RINGER_MODE_NORMAL)
}