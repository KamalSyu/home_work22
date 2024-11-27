package com.example.servicesamples

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class MyService : Service() {

    private val binder = MyBinder()
    private var countdownTime = 20 // Установите 20 секунд
    private val handler = Handler()
    private lateinit var runnable: Runnable

    inner class MyBinder : Binder() {
        fun getService(): MyService = this@MyService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        startCountdown() // Запустите обратный отсчет
        return START_STICKY // Убедитесь, что сервис будет перезапущен, если система убьет его
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun startCountdown() {
        countdownTime = 20 // Сбросить время обратного отсчета
        startForegroundService() // Запустить сервис в foreground
        runnable = object : Runnable {
            override fun run() {
                if (countdownTime > 0) {
                    Log.d(TAG, "Countdown: $countdownTime")
                    updateNotification(countdownTime)
                    countdownTime--
                    handler.postDelayed(this, 1000)
                } else {
                    stopSelf() // Остановить сервис, когда обратный отсчет завершен
                    Log.d(TAG, "Service finished countdown")
                }
            }
        }
        handler.post(runnable) // Запуск обратного отсчета
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service Running")
            .setContentText("Countdown: $countdownTime seconds")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(20, countdownTime, false)
            .build()

        startForeground(1, notification) // Запуск foreground-сервиса
    }

    private fun updateNotification(progress: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service Running")
            .setContentText("Countdown: $progress seconds")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(20, progress, false)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification) // Обновление уведомления
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Удалить обратный вызов при остановке сервиса
        Log.d(TAG, "Service destroyed")
    }

    fun getCurrentProgress(): Int {
        return countdownTime // Возвращаем текущее значение обратного отсчета
    }

    companion object {
        private const val TAG = "MyService"
        private const val CHANNEL_ID = "ForegroundServiceChannel"
    }
}
