package com.itsha123.autosilent.composables.settings

import android.annotation.SuppressLint
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.settings.components.SettingsItemButton
import com.itsha123.autosilent.utilities.isInternetAvailable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun CacheSettingsScreen(navController: NavController? = null, context: Context? = null) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column {
        MediumTopAppBar(
            title = { Text(stringResource(R.string.cache_settings_title)) },
            navigationIcon = {
                IconButton(onClick = {
                    navController?.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            })
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                SettingsItemButton(stringResource(R.string.update_cache_button)) {
                    GlobalScope.launch(Dispatchers.IO) {
                        if (isInternetAvailable()) {
                            Log.i("cacheSettingsScreen", "Updating cache started")
                            scope.launch {
                                snackbarHostState.showSnackbar(context!!.getString(R.string.updating_cache_started_snackbar_message))
                            }
                            val cacheDir = context?.filesDir
                            val client = OkHttpClient()
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
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar(context!!.getString(R.string.updating_cache_finished_snackbar_message))
                            }
                        } else {
                            Log.d("CacheSettingsScreen", "No internet connection")
                            scope.launch {
                                snackbarHostState.showSnackbar(context!!.getString(R.string.no_internet_snackbar_message))
                            }
                        }
                    }
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