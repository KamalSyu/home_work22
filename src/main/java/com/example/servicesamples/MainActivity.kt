package com.example.servicesamples

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var myService: MyService? = null
    private var isBound = false
    private lateinit var countdownTextView: TextView

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MyService.MyBinder
            myService = binder.getService()
            myService?.startCountdown() // Запуск обратного отсчета
            isBound = true
            updateCountdown() // Начать обновление обратного отсчета
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countdownTextView = findViewById(R.id.countdownTextView)

        val startServiceButton = findViewById<Button>(R.id.startServiceButton)
        startServiceButton.setOnClickListener {
            Log.d(TAG, "Starting the service...")
            val serviceIntent = Intent(this, MyService::class.java)
            bindService(serviceIntent, connection, BIND_AUTO_CREATE)
            startService(serviceIntent)
        }
    }

    private fun updateCountdown() {
        if (isBound) {
            countdownTextView.postDelayed({
                val currentProgress = myService?.getCurrentProgress() ?: return@postDelayed
                countdownTextView.text = currentProgress.toString()
                if (currentProgress > 0) {
                    updateCountdown()
                }
            }, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
