package com.itsha123.autosilent.composables.settings

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.itsha123.autosilent.composables.settings.components.SettingsItemButton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


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
                SettingsItemButton("Update Cache") {

                    val cacheDir = context?.filesDir

                    val client = OkHttpClient()
                    Log.i("cacheSettingsScreen", "Updating cache started")

                    GlobalScope.launch(Dispatchers.IO) {
                        val files = cacheDir?.listFiles()
                        if (files == null) {
                            Log.i("cacheSettingsScreen", "No files found in cache directory")
                        }

                        files?.forEach { file ->
                            val filename = file.name
                            if (filename != "profileInstalled") {
                                Log.d("cacheSettingsScreen", "Checking $filename")
                                val request = Request.Builder()
                                    .url("https://raw.githubusercontent.com/Auto-Silent/Auto-Silent-Database/main/$filename")
                                    .build()

                                try {
                                    val response = client.newCall(request).execute()
                                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                    val remoteContent = response.body?.string()
                                    val localContent = file.readText()

                                    if (remoteContent != localContent) {
                                        file.writeText(remoteContent ?: "")
                                        Log.d("cacheSettingsScreen", "Updated $filename")
                                    } else {
                                        Log.d(
                                            "CacheSettingsScreen",
                                            "No update needed for $filename"
                                        )
                                    }
                                } catch (e: IOException) {
                                    Log.w("CacheSettingsScreen", "Error updating $filename", e)
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