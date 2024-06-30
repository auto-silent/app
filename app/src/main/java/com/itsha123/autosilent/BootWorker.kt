package com.itsha123.autosilent

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class BootWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Perform your task here
        Log.i("BootWorker", "Worker is running")
        val serviceIntent = Intent(applicationContext, BackgroundLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent)
        }
        return Result.success()
    }
}