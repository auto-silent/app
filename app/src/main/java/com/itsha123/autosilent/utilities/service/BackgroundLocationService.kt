package com.itsha123.autosilent.utilities.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.itsha123.autosilent.R
import com.itsha123.autosilent.utilities.fetchLocation

const val NOTIFICATION_ID = 1

class BackgroundLocationService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val interval = 15 * 1000L
    lateinit var audioManager: AudioManager
    private val runnable = object : Runnable {
        override fun run() {
            Log.i("backgroundService", "Service is running")
            fetchLocation(this@BackgroundLocationService, audioManager)
            handler.postDelayed(this, interval)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("backgroundService", "Service started")
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        if (!sharedPref.getBoolean("enabled", true)) {
            Log.i("backgroundService", "Service stopped due to user preference")
            stopService(Intent(this, BackgroundLocationService::class.java))
        }
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        val notification = getNotification()
        startForeground(NOTIFICATION_ID, notification)
        handler.post(runnable)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "1"
            val channelName = "Foreground Service Channel"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun getNotification(): Notification {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "1" else ""
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Auto Silent")
            .setContentText("Your app is running in foreground.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        return notificationBuilder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}