package com.example.servicesamples

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.Messenger
import android.os.IBinder
import android.util.Log

class MyService : Service() {

    private lateinit var messenger: Messenger
    private lateinit var handlerThread: HandlerThread
    private lateinit var serviceHandler: ServiceHandler // Объявляем переменную serviceHandler
    private var progress = 0
    private var isRunning = false

    private inner class ServiceHandler(looper: android.os.Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_START -> startService()
                MSG_STOP -> stopService()
                else -> super.handleMessage(msg)
            }
        }
    }

    companion object {
        private const val TAG = "MyService"
        const val MSG_START = 1
        const val MSG_STOP = 2
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        startForegroundService()
        handlerThread = HandlerThread("ServiceThread")
        handlerThread.start()
        serviceHandler = ServiceHandler(handlerThread.looper) // Инициализируем serviceHandler
        messenger = Messenger(serviceHandler) // Инициализация Messenger
    }

    private fun startForegroundService() {
        val notificationChannelId = "MyServiceChannel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelId, "My Service Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, notificationChannelId)
            .setContentTitle("My Service")
            .setContentText("Service is running...")
            .setSmallIcon(R.drawable.ic_service_icon) // Замените на ваш значок
            .build()

        startForeground(1, notification) // Запускаем сервис в фоновом режиме с уведомлением
    }

    private fun startService() {
        Log.d(TAG, "Starting service...")
        isRunning = true
        progress = 20 // Установим прогресс в 20 секунд
        serviceHandler.postDelayed(object : Runnable {
            override fun run() {
                if (progress > 0 && isRunning) {
                    progress--
                    // Здесь вы можете отправить сообщение обратно в активность, если нужно
                } else {
                    stopSelf() // Останавливаем сервис, когда прогресс достигает 0
                }
            }
        }, 1000)
    }

    private fun stopService() {
        Log.d(TAG, "Stopping service...")
        isRunning = false
        handlerThread.quitSafely()
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder {
        return messenger.binder // Возвращаем IBinder для связи с активностью
    }

    override fun onDestroy() {
        isRunning = false
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }
}
