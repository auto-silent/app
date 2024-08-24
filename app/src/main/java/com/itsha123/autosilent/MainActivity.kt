package com.itsha123.autosilent

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.itsha123.autosilent.composables.MainScreen
import com.itsha123.autosilent.composables.permissions.NotificationsPermissionRequestScreen
import com.itsha123.autosilent.composables.settings.CacheSettingsScreen
import com.itsha123.autosilent.composables.settings.FAQScreen
import com.itsha123.autosilent.composables.settings.GeneralSettingsScreen
import com.itsha123.autosilent.composables.settings.HelpScreen
import com.itsha123.autosilent.composables.settings.SettingsScreen
import com.itsha123.autosilent.services.location.BackgroundLocationService
import com.itsha123.autosilent.singletons.Routes
import com.itsha123.autosilent.singletons.Variables.recompose
import com.itsha123.autosilent.singletons.Variables.serviceui
import com.itsha123.autosilent.ui.theme.AutoSilentTheme
import com.itsha123.autosilent.utilities.isServiceRunning
import com.itsha123.autosilent.utilities.permsCheck


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        if (permsCheck(this)) {
            val serviceIntent = Intent(this, BackgroundLocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!isServiceRunning(BackgroundLocationService::class.java, this)) {
                    startForegroundService(serviceIntent)
                }
            }
        } else {
            with(sharedPref.edit()) {
                putBoolean("firstRun", true)
                apply()
            }
        }
        if (isServiceRunning(BackgroundLocationService::class.java, this)) {
            serviceui.value = true
        } else {
            serviceui.value = false
        }
        setContent {
            AutoSilentTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Routes.MAIN,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    composable(Routes.MAIN) {
                        MainScreen(navController, this@MainActivity, this@MainActivity)
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(navController, this@MainActivity)
                    }
                    composable(Routes.GENERALSETTINGS) {
                        GeneralSettingsScreen(navController, this@MainActivity)
                    }
                    composable(Routes.CACHESETTINGS) {
                        CacheSettingsScreen(navController, this@MainActivity)
                    }
                    composable(Routes.HELP) {
                        HelpScreen(navController, this@MainActivity)
                    }
                    composable(Routes.FAQ) {
                        FAQScreen(navController)
                    }
                    composable(Routes.NOTIFICATIONPERMISSION) {
                        val launcher =
                            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                                if (!isGranted && sharedPref.getInt(
                                        "notificationRequests",
                                        0
                                    ) < 2
                                ) {
                                    with(sharedPref.edit()) {
                                        putInt(
                                            "notificationRequests",
                                            sharedPref.getInt("notificationRequests", 0) + 1
                                        )
                                    }.apply()
                                }
                                if (isGranted) {
                                    navController.popBackStack()
                                }
                            }
                        val settingsActivityResultLauncher: ActivityResultLauncher<Intent> =
                            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
                        NotificationsPermissionRequestScreen(navController, this@MainActivity) {
                            recompose.value = !recompose.value
                            if (sharedPref.getInt("notificationRequests", 0) < 2) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts(
                                            "package",
                                            this@MainActivity.packageName,
                                            null
                                        )
                                    }
                                settingsActivityResultLauncher.launch(intent)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        serviceui.value = isServiceRunning(BackgroundLocationService::class.java, this)
        recompose.value = !recompose.value
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        serviceui.value = isServiceRunning(BackgroundLocationService::class.java, this)
    }
}



