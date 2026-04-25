package com.autocar.launcher.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.autocar.launcher.AutoHubApplication
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseService
import com.autocar.launcher.ui.activity.MainActivity
import com.autocar.launcher.util.LogUtil
import com.google.android.gms.location.*

/**
 * 位置服务
 * 提供 GPS 定位和位置跟踪功能
 * 
 * 功能：
 * - GPS 实时定位
 * - 位置更新监听
 * - 地理围栏
 * - 位置信息共享
 */
class LocationService : BaseService() {

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 1004
        
        const val ACTION_START_TRACKING = "com.autocar.launcher.action.START_LOCATION_TRACKING"
        const val ACTION_STOP_TRACKING = "com.autocar.launcher.action.STOP_LOCATION_TRACKING"
        
        // 位置更新间隔（毫秒）
        private const val UPDATE_INTERVAL = 5000L
        private const val FASTEST_INTERVAL = 2000L
    }
    
    // FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    // LocationCallback
    private lateinit var locationCallback: LocationCallback
    
    // 当前位置
    private var currentLocation: Location? = null
    
    // 位置更新监听器列表
    private val locationListeners = mutableListOf<OnLocationUpdateListener>()
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onLocationUpdated(location)
                }
            }
            
            override fun onLocationAvailability(availability: LocationAvailability) {
                LogUtil.d(TAG, "位置可用性: ${availability.isLocationAvailable}")
            }
        }
        
        LogUtil.d(TAG, "位置服务已创建")
    }
    
    override fun onHandleStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startLocationTracking()
            ACTION_STOP_TRACKING -> stopLocationTracking()
            else -> startForeground(NOTIFICATION_ID, createNotification())
        }
        return START_STICKY
    }
    
    override fun onHandleBind(intent: Intent?): android.os.IBinder? = null
    
    /**
     * 开始位置跟踪
     */
    private fun startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            LogUtil.e(TAG, "缺少位置权限")
            return
        }
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            .setWaitForAccurateLocation(false)
            .build()
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        
        startForeground(NOTIFICATION_ID, createNotification())
        LogUtil.d(TAG, "开始位置跟踪")
    }
    
    /**
     * 停止位置跟踪
     */
    private fun stopLocationTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        LogUtil.d(TAG, "停止位置跟踪")
    }
    
    /**
     * 位置更新回调
     */
    private fun onLocationUpdated(location: Location) {
        currentLocation = location
        LogUtil.d(TAG, "位置更新: lat=${location.latitude}, lng=${location.longitude}")
        
        // 通知所有监听器
        locationListeners.forEach { listener ->
            listener.onLocationUpdate(location)
        }
    }
    
    /**
     * 获取当前位置
     */
    fun getCurrentLocation(): Location? = currentLocation
    
    /**
     * 添加位置监听器
     */
    fun addLocationListener(listener: OnLocationUpdateListener) {
        if (!locationListeners.contains(listener)) {
            locationListeners.add(listener)
        }
    }
    
    /**
     * 移除位置监听器
     */
    fun removeLocationListener(listener: OnLocationUpdateListener) {
        locationListeners.remove(listener)
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
        
        val locationText = currentLocation?.let {
            "位置: ${String.format("%.6f", it.latitude)}, ${String.format("%.6f", it.longitude)}"
        } ?: "正在获取位置..."
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, AutoHubApplication.CHANNEL_LOCATION)
                .setContentTitle("位置服务")
                .setContentText(locationText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("位置服务")
                .setContentText(locationText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build()
        }
    }
    
    override fun onDestroy() {
        stopLocationTracking()
        locationListeners.clear()
        super.onDestroy()
        LogUtil.d(TAG, "位置服务已销毁")
    }
    
    /**
     * 位置更新监听器接口
     */
    interface OnLocationUpdateListener {
        fun onLocationUpdate(location: Location)
    }
}
