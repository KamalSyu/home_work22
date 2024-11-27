package com.example.servicesamples

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var messenger: Messenger? = null
    private var isBound = false
    private lateinit var progressTextView: TextView
    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            MyService.MSG_START -> {
                // Обработка сообщения о старте сервиса
            }
            MyService.MSG_STOP -> {
                // Обработка сообщения о остановке сервиса
            }
            else -> return@Callback false
        }
        true
    })

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            messenger = Messenger(service)
            startService() // Запускаем сервис после подключения
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            messenger = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressTextView = findViewById(R.id.progressTextView)
        val startServiceButton = findViewById<View>(R.id.startServiceButton)

        startServiceButton.setOnClickListener {
            Log.d(TAG, "Starting the service...")
            val serviceIntent = Intent(this, MyService::class.java)
            startService(serviceIntent) // Запускаем сервис
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE) // Подписываемся на сервис
        }
    }

    private fun startService() {
        messenger?.send(Message.obtain(null, MyService.MSG_START)) // Отправляем сообщение на старт сервиса
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            messenger?.send(Message.obtain(null, MyService.MSG_STOP)) // Отправляем сообщение на остановку сервиса
            unbindService(connection) // Отписываемся от сервиса
            isBound = false
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
