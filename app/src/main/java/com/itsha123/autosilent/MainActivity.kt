package com.itsha123.autosilent

import SettingsScreen
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.itsha123.autosilent.composables.MainScreen
import com.itsha123.autosilent.composables.settings.CacheSettingsScreen
import com.itsha123.autosilent.composables.settings.GeneralSettingsScreen
import com.itsha123.autosilent.singletons.Routes
import com.itsha123.autosilent.ui.theme.AutoSilentTheme
import com.itsha123.autosilent.utilities.isServiceRunning
import com.itsha123.autosilent.utilities.service.BackgroundLocationService


class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, BackgroundLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isServiceRunning(BackgroundLocationService::class.java, this)) {
            startForegroundService(serviceIntent)
            }
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
                        SettingsScreen(navController)
                    }
                    composable(Routes.GENERALSETTINGS) {
                        GeneralSettingsScreen(navController, this@MainActivity)
                    }
                    composable(Routes.CACHESETTINGS) {
                        CacheSettingsScreen(navController, this@MainActivity)
                    }
                }
            }
        }
    }
}



