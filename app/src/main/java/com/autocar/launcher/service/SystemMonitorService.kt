package com.autocar.launcher.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.autocar.launcher.AutoHubApplication
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseService
import com.autocar.launcher.ui.activity.MainActivity
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 系统监控服务
 * 监控系统状态并收集数据
 * 
 * 功能：
 * - 电池状态监控
 * - 内存使用监控
 * - CPU 温度监控
 * - 网络状态监控
 * - 存储空间监控
 */
class SystemMonitorService : BaseService() {

    companion object {
        private const val TAG = "SystemMonitorService"
        private const val NOTIFICATION_ID = 1006
        
        const val ACTION_START_MONITORING = "com.autocar.launcher.action.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.autocar.launcher.action.STOP_MONITORING"
        
        // 监控更新间隔
        private const val MONITOR_INTERVAL = 5000L
    }
    
    // Handler
    private val handler = Handler(Looper.getMainLooper())
    
    // 监控 Runnable
    private val monitorRunnable = object : Runnable {
        override fun run() {
            updateSystemInfo()
            handler.postDelayed(this, MONITOR_INTERVAL)
        }
    }
    
    // 系统信息状态
    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo.asStateFlow()
    
    // 电池信息
    private var batteryReceiver: BroadcastReceiver? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // 注册电池广播
        registerBatteryReceiver()
        
        LogUtil.d(TAG, "系统监控服务已创建")
    }
    
    override fun onHandleStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
            else -> startForeground(NOTIFICATION_ID, createNotification())
        }
        return START_STICKY
    }
    
    override fun onHandleBind(intent: Intent?): android.os.IBinder? = null
    
    /**
     * 开始监控
     */
    private fun startMonitoring() {
        handler.post(monitorRunnable)
        startForeground(NOTIFICATION_ID, createNotification())
        LogUtil.d(TAG, "开始系统监控")
    }
    
    /**
     * 停止监控
     */
    private fun stopMonitoring() {
        handler.removeCallbacks(monitorRunnable)
        stopForeground(STOP_FOREGROUND_REMOVE)
        LogUtil.d(TAG, "停止系统监控")
    }
    
    /**
     * 注册电池广播接收器
     */
    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        updateBatteryInfo(intent)
                    }
                    Intent.ACTION_BATTERY_LOW -> {
                        LogUtil.w(TAG, "电池电量低")
                        _systemInfo.value = _systemInfo.value.copy(batteryLow = true)
                    }
                    Intent.ACTION_BATTERY_OKAY -> {
                        _systemInfo.value = _systemInfo.value.copy(batteryLow = false)
                    }
                    Intent.ACTION_POWER_CONNECTED -> {
                        _systemInfo.value = _systemInfo.value.copy(isCharging = true)
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        _systemInfo.value = _systemInfo.value.copy(isCharging = false)
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(batteryReceiver, filter)
        }
    }
    
    /**
     * 更新电池信息
     */
    private fun updateBatteryInfo(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val batteryPercent = (level * 100) / scale
        
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0
        
        _systemInfo.value = _systemInfo.value.copy(
            batteryLevel = batteryPercent,
            isCharging = isCharging,
            batteryTemperature = temperature
        )
    }
    
    /**
     * 更新系统信息
     */
    private fun updateSystemInfo() {
        try {
            // 获取内存信息
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val memoryUsage = (usedMemory * 100) / totalMemory
            
            // 获取存储信息
            val storageInfo = getStorageInfo()
            
            // 获取 CPU 信息
            val cpuUsage = getCpuUsage()
            
            // 更新状态
            _systemInfo.value = _systemInfo.value.copy(
                usedMemoryMB = (usedMemory / 1024 / 1024).toInt(),
                totalMemoryMB = (totalMemory / 1024 / 1024).toInt(),
                memoryUsagePercent = memoryUsage.toInt(),
                usedStorageGB = storageInfo.first,
                totalStorageGB = storageInfo.second,
                cpuUsagePercent = cpuUsage
            )
            
            updateNotification()
            
        } catch (e: Exception) {
            LogUtil.e(TAG, "更新系统信息失败", e)
        }
    }
    
    /**
     * 获取存储信息
     */
    private fun getStorageInfo(): Pair<Float, Float> {
        return try {
            val path = android.os.Environment.getDataDirectory()
            val stat = android.os.StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            
            val totalGB = (blockSize * totalBlocks) / (1024f * 1024f * 1024f)
            val usedGB = (blockSize * (totalBlocks - availableBlocks)) / (1024f * 1024f * 1024f)
            
            Pair(usedGB, totalGB)
        } catch (e: Exception) {
            Pair(0f, 0f)
        }
    }
    
    /**
     * 获取 CPU 使用率
     */
    private fun getCpuUsage(): Int {
        return try {
            val reader = java.io.BufferedReader(java.io.FileReader("/proc/stat"))
            val line = reader.readLine()
            reader.close()
            
            if (line != null && line.startsWith("cpu ")) {
                val parts = line.split("\\s+".toRegex())
                if (parts.size >= 5) {
                    val user = parts[1].toLongOrNull() ?: 0
                    val nice = parts[2].toLongOrNull() ?: 0
                    val system = parts[3].toLongOrNull() ?: 0
                    val idle = parts[4].toLongOrNull() ?: 0
                    val total = user + nice + system + idle
                    
                    if (total > 0) {
                        ((total - idle) * 100 / total).toInt()
                    } else 0
                } else 0
            } else 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 创建通知
     */
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val info = _systemInfo.value
        val contentText = "电量: ${info.batteryLevel}% | 内存: ${info.memoryUsagePercent}% | CPU: ${info.cpuUsagePercent}%"
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, AutoHubApplication.CHANNEL_MONITOR)
                .setContentTitle("系统监控")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("系统监控")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build()
        }
    }
    
    /**
     * 更新通知
     */
    private fun updateNotification() {
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onDestroy() {
        handler.removeCallbacks(monitorRunnable)
        batteryReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // 可能已经注销
            }
        }
        super.onDestroy()
        LogUtil.d(TAG, "系统监控服务已销毁")
    }
    
    /**
     * 系统信息数据类
     */
    data class SystemInfo(
        val batteryLevel: Int = 0,
        val batteryTemperature: Double = 0.0,
        val isCharging: Boolean = false,
        val batteryLow: Boolean = false,
        val usedMemoryMB: Int = 0,
        val totalMemoryMB: Int = 0,
        val memoryUsagePercent: Int = 0,
        val usedStorageGB: Float = 0f,
        val totalStorageGB: Float = 0f,
        val cpuUsagePercent: Int = 0
    )
}
