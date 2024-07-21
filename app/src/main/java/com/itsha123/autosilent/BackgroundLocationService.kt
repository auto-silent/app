package com.itsha123.autosilent

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

const val NOTIFICATION_ID = 1

class BackgroundLocationService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val interval = 15 * 1000L // 2 minutes
    lateinit var audioManager: AudioManager
    private val runnable = object : Runnable {
        override fun run() {
            Log.d("test:)", "Service is running")
            fetchLocation(this@BackgroundLocationService, audioManager)
            handler.postDelayed(this, interval)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        if (!sharedPref.getBoolean("enabled", true)) {
            Log.i("test:)", "Service stopped")
            stopService(Intent(this, BackgroundLocationService::class.java))
        }
        Log.d("test:)", "Service started")
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        val notification = getNotification()
        startForeground(NOTIFICATION_ID, notification)
        handler.post(runnable)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "YourChannelId"
            val channelName = "Foreground Service Channel"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun getNotification(): Notification {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "YourChannelId" else ""
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