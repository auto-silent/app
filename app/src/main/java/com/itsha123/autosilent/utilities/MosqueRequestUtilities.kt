package com.itsha123.autosilent.utilities

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class RequestData(
    val issueNumber: Int,
    val title: String,
    val address: String,
    val latitude: String,
    val longitude: String
)

fun saveRequest(context: Context, request: RequestData) {
    val jsonFormatter = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    val existingJson = sharedPref.getString("mosqueRequests", null)
    val currentRequests: List<RequestData> = if (!existingJson.isNullOrEmpty()) {
        jsonFormatter.decodeFromString(existingJson)
    } else {
        emptyList()
    }
    val updatedRequests = currentRequests + request
    val json = jsonFormatter.encodeToString(updatedRequests)
    sharedPref.edit().apply {
        putString("mosqueRequests", json)
        apply()
    }
}

fun getRequests(context: Context?): List<RequestData> {
    val jsonFormatter = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val json = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        ?.getString("mosqueRequests", null)
    return if (!json.isNullOrEmpty()) {
        jsonFormatter.decodeFromString(json)
    } else emptyList()
}