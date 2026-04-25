package com.autocar.launcher.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.autocar.launcher.AutoHubApplication
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseService
import com.autocar.launcher.ui.activity.MainActivity
import com.autocar.launcher.util.LogUtil

/**
 * 后台服务
 * 负责应用整体的后台运行管理
 * 
 * 功能：
 * - 保持应用在后台运行
 * - 管理系统服务状态
 * - 提供服务启动入口
 */
class BackgroundService : BaseService() {

    companion object {
        private const val TAG = "BackgroundService"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "com.autocar.launcher.action.START_BACKGROUND"
        const val ACTION_STOP = "com.autocar.launcher.action.STOP_BACKGROUND"
    }
    
    // 是否正在运行
    @Volatile
    private var isRunning = false
    
    override fun onHandleStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
            else -> start()
        }
        return START_STICKY
    }
    
    /**
     * 启动服务
     */
    private fun start() {
        if (isRunning) {
            LogUtil.d(TAG, "服务已在运行")
            return
        }
        
        isRunning = true
        
        // 创建前台通知
        val notification = createNotification()
        
        // 启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        LogUtil.d(TAG, "后台服务已启动")
        
        // 初始化后台任务
        initBackgroundTasks()
    }
    
    /**
     * 停止服务
     */
    private fun stop() {
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        LogUtil.d(TAG, "后台服务已停止")
    }
    
    /**
     * 创建前台通知
     */
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, BackgroundService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(this, AutoHubApplication.CHANNEL_BACKGROUND)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("凹凸桌面正在后台运行")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_stop, "停止", stopIntent)
                .setOngoing(true)
                .setPriority(android.app.Notification.PRIORITY_LOW)
                .build()
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("凹凸桌面正在后台运行")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_stop, "停止", stopIntent)
                .setOngoing(true)
                .setPriority(android.app.Notification.PRIORITY_LOW)
                .build()
        }
    }
    
    /**
     * 初始化后台任务
     */
    private fun initBackgroundTasks() {
        // TODO: 初始化后台任务
        // - 应用列表监控
        // - 系统状态监控
        // - 定时任务调度
        LogUtil.d(TAG, "后台任务初始化完成")
    }
}
