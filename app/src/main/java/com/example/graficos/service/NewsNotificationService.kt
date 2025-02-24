package com.example.graficos.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.graficos.MainActivity
import com.example.graficos.R
import com.example.graficos.api.CryptoNewsApi
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NewsNotificationService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var lastNewsId: String? = null

    private val newsApi = Retrofit.Builder()
        .baseUrl("https://min-api.cryptocompare.com/data/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CryptoNewsApi::class.java)

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification("BitTrend News", "Monitoreando noticias..."))
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_START_SERVICE -> startNewsMonitoring()
                ACTION_STOP_SERVICE -> stopSelf()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
        return START_STICKY
    }

    private fun startNewsMonitoring() {
        if (serviceScope.isActive) return
        
        serviceScope.launch {
            while (isActive) {
                try {
                    val news = newsApi.getNews()
                    val latestNews = news.Data.firstOrNull()
                    
                    if (latestNews != null && latestNews.id != lastNewsId) {
                        lastNewsId = latestNews.id
                        showNewsNotification(latestNews.title, latestNews.source)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(TimeUnit.MINUTES.toMillis(5))
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Crypto News",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones de noticias de criptomonedas"
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun showNewsNotification(title: String, source: String) {
        val notification = createNotification(title, "Fuente: $source")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "crypto_news_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START_SERVICE = "START_SERVICE"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
    }
} 