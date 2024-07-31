package com.itsha123.autosilent.singletons

import com.itsha123.autosilent.utilities.GeofenceData
import kotlinx.coroutines.flow.MutableStateFlow

object Variables {
    var geofenceData: GeofenceData? = null
    var geofence = MutableStateFlow(false)
    var buttonText = MutableStateFlow("Turn Off")
    var database = MutableStateFlow(true)
    var internet = MutableStateFlow(true)
}