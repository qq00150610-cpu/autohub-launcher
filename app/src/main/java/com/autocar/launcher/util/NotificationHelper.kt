package com.autocar.launcher.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.autocar.launcher.AutoHubApplication
import com.autocar.launcher.R
import com.autocar.launcher.ui.activity.MainActivity

/**
 * 通知帮助类
 * 提供通知创建和管理功能
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"
    
    /**
     * 创建简单通知
     */
    fun createSimpleNotification(
        context: Context,
        channelId: String,
        title: String,
        content: String,
        iconRes: Int = R.drawable.ic_notification
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(iconRes)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(iconRes)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        }
    }
    
    /**
     * 创建通知渠道（Android 8.0+）
     */
    fun createNotificationChannel(
        context: Context,
        channelId: String,
        channelName: String,
        description: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                this.description = description
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 显示通知
     */
    fun showNotification(context: Context, notificationId: Int, notification: Notification) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * 取消通知
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
    
    /**
     * 取消所有通知
     */
    fun cancelAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
