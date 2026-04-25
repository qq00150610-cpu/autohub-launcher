package com.autocar.launcher

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.autocar.launcher.util.LogUtil
import com.autocar.launcher.util.PreferencesManager

/**
 * 凹凸桌面 Application 类
 * 应用全局上下文，管理应用生命周期
 * 
 * 功能：
 * - 初始化全局配置
 * - 创建前台服务通知渠道
 * - 管理全局单例
 * - 设置未捕获异常处理
 */
class AutoHubApplication : Application() {

    companion object {
        private const val TAG = "AutoHubApplication"
        
        // 通知渠道 ID
        const val CHANNEL_BACKGROUND = "autohub_background"
        const val CHANNEL_FLOATING = "autohub_floating"
        const val CHANNEL_MEDIA = "autohub_media"
        const val CHANNEL_LOCATION = "autohub_location"
        const val CHANNEL_CAR = "autohub_car"
        const val CHANNEL_MONITOR = "autohub_monitor"
        const val CHANNEL_VOICE = "autohub_voice"
        const val CHANNEL_NOTIFICATION = "autohub_notification"
        
        @Volatile
        private var instance: AutoHubApplication? = null
        
        fun getInstance(): AutoHubApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
    
    // 全局上下文
    lateinit var context: Context
        private set
    
    // 偏好设置管理器
    lateinit var preferencesManager: PreferencesManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        context = applicationContext
        
        // 初始化日志
        LogUtil.init()
        
        // 初始化偏好设置
        preferencesManager = PreferencesManager(this)
        
        // 创建通知渠道
        createNotificationChannels()
        
        // 设置异常处理
        setupExceptionHandler()
        
        LogUtil.d(TAG, "凹凸桌面 Application 初始化完成")
    }
    
    /**
     * 创建所有前台服务通知渠道
     * Android 8.0+ 必须创建通知渠道才能显示通知
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // 后台服务通知渠道
            val backgroundChannel = NotificationChannel(
                CHANNEL_BACKGROUND,
                "后台服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持应用后台运行的低优先级通知"
                setShowBadge(false)
            }
            
            // 悬浮球服务通知渠道
            val floatingChannel = NotificationChannel(
                CHANNEL_FLOATING,
                "悬浮球服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "悬浮球快捷操作服务"
                setShowBadge(false)
            }
            
            // 媒体播放通知渠道
            val mediaChannel = NotificationChannel(
                CHANNEL_MEDIA,
                "媒体播放",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "音乐和视频播放控制"
                setShowBadge(true)
            }
            
            // 位置服务通知渠道
            val locationChannel = NotificationChannel(
                CHANNEL_LOCATION,
                "位置服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "GPS 定位和导航服务"
                setShowBadge(false)
            }
            
            // 车辆连接通知渠道
            val carChannel = NotificationChannel(
                CHANNEL_CAR,
                "车辆连接",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "OBD 和车辆诊断服务"
                setShowBadge(true)
            }
            
            // 系统监控通知渠道
            val monitorChannel = NotificationChannel(
                CHANNEL_MONITOR,
                "系统监控",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "系统状态监控通知"
                setShowBadge(false)
            }
            
            // 语音助手通知渠道
            val voiceChannel = NotificationChannel(
                CHANNEL_VOICE,
                "语音助手",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "语音识别和助手服务"
                setShowBadge(false)
            }
            
            // 普通通知渠道
            val notificationChannel = NotificationChannel(
                CHANNEL_NOTIFICATION,
                "系统通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "应用普通通知"
                setShowBadge(true)
            }
            
            // 注册所有渠道
            notificationManager.createNotificationChannels(listOf(
                backgroundChannel,
                floatingChannel,
                mediaChannel,
                locationChannel,
                carChannel,
                monitorChannel,
                voiceChannel,
                notificationChannel
            ))
        }
    }
    
    /**
     * 设置全局未捕获异常处理
     * 记录崩溃日志，便于问题排查
     */
    private fun setupExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            LogUtil.e(TAG, "Uncaught exception on thread: ${thread.name}", throwable)
            
            // 调用默认处理（可选：记录后不上报）
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        LogUtil.d(TAG, "凹凸桌面 Application 终止")
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        LogUtil.w(TAG, "系统内存不足")
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        LogUtil.d(TAG, "内存修剪: level=$level")
    }
}
