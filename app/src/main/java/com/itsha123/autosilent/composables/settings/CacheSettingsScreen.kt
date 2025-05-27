package com.itsha123.autosilent.composables.settings

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.settings.components.SettingsItemButton
import com.itsha123.autosilent.singletons.Routes
import com.itsha123.autosilent.utilities.isInternetAvailable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun CacheSettingsScreen(navController: NavController? = null, context: Context? = null) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        MediumTopAppBar(
            colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            title = { Text(stringResource(R.string.cache_settings_title)) },
            navigationIcon = {
                IconButton(onClick = {
                    navController?.popBackStack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            })
    }) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                SettingsItemButton(stringResource(R.string.add_mosques)) {
                    navController?.navigate(Routes.MOSQUES)
                }
            }
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
                            if (files!!.size > 1) {
                                files.forEach { file ->
                                    val filename = file.name
                                    if (filename != "profileInstalled") {
                                        Log.d("cacheSettingsScreen", "Checking $filename")
                                        val request = Request.Builder()
                                            .url("https://raw.githubusercontent.com/auto-silent/database/main/$filename")
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
                                            Log.w(
                                                "CacheSettingsScreen",
                                                "Error updating $filename",
                                                e
                                            )
                                        }
                                    }
                                }
                                Log.i("CacheSettingsScreen", "Updating cache finished")
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(context.getString(R.string.updating_cache_finished_snackbar_message))
                                }
                            } else {
                                Log.d("CacheSettingsScreen", "No files to update")
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar("No cache found")
                                }
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

@Preview(showBackground = true)
@Composable
fun PreviewCacheSettingsScreen() {
    CacheSettingsScreen()
}