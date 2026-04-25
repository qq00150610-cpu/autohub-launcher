package com.autocar.launcher.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限帮助类
 * 管理运行时权限请求
 */
object PermissionHelper {

    private const val TAG = "PermissionHelper"
    
    // 权限请求码
    object RequestCode {
        const val STORAGE = 1001
        const val LOCATION = 1002
        const val NOTIFICATION = 1003
        const val OVERLAY = 1004
        const val BLUETOOTH = 1005
        const val MICROPHONE = 1006
        const val ALL_PERMISSIONS = 9999
    }
    
    /**
     * 存储权限
     */
    val storagePermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    
    /**
     * 位置权限
     */
    val locationPermissions: Array<String>
        get() = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    
    /**
     * 蓝牙权限
     */
    val bluetoothPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    
    /**
     * 麦克风权限
     */
    val microphonePermissions: Array<String>
        get() = arrayOf(Manifest.permission.RECORD_AUDIO)
    
    /**
     * 通知权限（Android 13+）
     */
    val notificationPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    
    /**
     * 检查权限是否已授予
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查多个权限是否已授予
     */
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }
    
    /**
     * 请求权限
     */
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }
    
    /**
     * 检查并请求存储权限
     */
    fun checkAndRequestStorage(activity: Activity, requestCode: Int = RequestCode.STORAGE): Boolean {
        return if (!hasPermissions(activity, storagePermissions)) {
            requestPermissions(activity, storagePermissions, requestCode)
            false
        } else {
            true
        }
    }
    
    /**
     * 检查并请求位置权限
     */
    fun checkAndRequestLocation(activity: Activity, requestCode: Int = RequestCode.LOCATION): Boolean {
        return if (!hasPermissions(activity, locationPermissions)) {
            requestPermissions(activity, locationPermissions, requestCode)
            false
        } else {
            true
        }
    }
    
    /**
     * 检查并请求蓝牙权限
     */
    fun checkAndRequestBluetooth(activity: Activity, requestCode: Int = RequestCode.BLUETOOTH): Boolean {
        return if (!hasPermissions(activity, bluetoothPermissions)) {
            requestPermissions(activity, bluetoothPermissions, requestCode)
            false
        } else {
            true
        }
    }
    
    /**
     * 检查并请求麦克风权限
     */
    fun checkAndRequestMicrophone(activity: Activity, requestCode: Int = RequestCode.MICROPHONE): Boolean {
        return if (!hasPermissions(activity, microphonePermissions)) {
            requestPermissions(activity, microphonePermissions, requestCode)
            false
        } else {
            true
        }
    }
    
    /**
     * 检查并请求通知权限（Android 13+）
     */
    fun checkAndRequestNotification(activity: Activity, requestCode: Int = RequestCode.NOTIFICATION): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermissions(activity, notificationPermissions)) {
                requestPermissions(activity, notificationPermissions, requestCode)
                false
            } else {
                true
            }
        } else {
            true // Android 13 以下不需要请求通知权限
        }
    }
    
    /**
     * 权限被拒绝时是否应该显示解释
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * 获取所有需要请求的权限
     */
    fun getAllRequiredPermissions(): Array<String> {
        return storagePermissions + locationPermissions + bluetoothPermissions + microphonePermissions + notificationPermissions
    }
}
