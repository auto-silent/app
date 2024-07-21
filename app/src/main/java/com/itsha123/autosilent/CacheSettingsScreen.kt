package com.itsha123.autosilent

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@Composable
fun PixelSettingsItemButton(title: String, onClick: () -> Unit) {
    var clicked by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(targetValue = if (clicked) 0.95f else 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    clicked = true
                    onClick()
                },
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .scale(scale.value)
    ) {
        Column {
            Text(text = title, fontSize = 20.sp)
        }
    }

    LaunchedEffect(clicked) {
        if (clicked) {
            // Reset the click effect after navigation
            clicked = false
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun CacheSettingsScreen(navController: NavController? = null, context: Context? = null) {

    Column {
        MediumTopAppBar(title = { Text("Cache") }, navigationIcon = {
            IconButton(onClick = {
                navController?.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        })
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                PixelSettingsItemButton("Update Cache") {
                    if (context == null) {
                        Log.e("CacheSettingsScreen", "Context is null")
                        return@PixelSettingsItemButton
                    }

                    val cacheDir = context.filesDir
                    if (cacheDir == null) {
                        Log.e("CacheSettingsScreen", "Cache directory is null")
                        return@PixelSettingsItemButton
                    }

                    val client = OkHttpClient()
                    Log.i("CacheSettingsScreen", "Updating cache started")

                    GlobalScope.launch(Dispatchers.IO) {
                        val files = cacheDir.listFiles()
                        if (files == null) {
                            Log.e("CacheSettingsScreen", "No files found in cache directory")
                            return@launch
                        }

                        files.forEach { file ->
                            val filename = file.name
                            if (filename != "profileInstalled") {
                                Log.i("CacheSettingsScreen", "Checking $filename")
                                val request = Request.Builder()
                                    .url("https://raw.githubusercontent.com/Auto-Silent/Auto-Silent-Database/main/$filename")
                                    .build()

                                try {
                                    val response = client.newCall(request).execute()
                                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                    val remoteContent = response.body?.string()
                                    val localContent = file.readText()

                                    if (remoteContent != localContent) {
                                        // Update the file
                                        file.writeText(remoteContent ?: "")
                                        Log.i("CacheSettingsScreen", "Updated $filename")
                                    } else {
                                        Log.i(
                                            "CacheSettingsScreen",
                                            "No update needed for $filename"
                                        )
                                    }
                                } catch (e: IOException) {
                                    Log.e("CacheSettingsScreen", "Error updating $filename", e)
                                }
                            }
                        }
                        Log.i("CacheSettingsScreen", "Updating cache finished")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewCacheSettingsScreen() {
    CacheSettingsScreen()
}