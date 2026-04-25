package com.autocar.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.autocar.launcher.service.BackgroundService
import com.autocar.launcher.service.FloatingBallService
import com.autocar.launcher.util.LogUtil
import com.autocar.launcher.util.PreferencesManager

/**
 * 开机启动广播接收器
 * 设备启动完成后自动启动应用服务
 * 
 * 功能：
 * - 监听 BOOT_COMPLETED 广播
 * - 自动启动后台服务
 * - 自动显示悬浮球
 * - 可在设置中禁用自动启动
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        
        LogUtil.d(TAG, "收到广播: $action")
        
        // 检查是否是启动完成广播
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            
            handleBootCompleted(context)
        }
    }
    
    /**
     * 处理开机完成
     */
    private fun handleBootCompleted(context: Context) {
        LogUtil.d(TAG, "设备启动完成，准备启动凹凸桌面")
        
        // 延迟启动，等待系统就绪
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            
            // 启动后台服务
            val backgroundIntent = Intent(context, BackgroundService::class.java).apply {
                action = BackgroundService.ACTION_START
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(backgroundIntent)
            } else {
                context.startService(backgroundIntent)
            }
            
            LogUtil.d(TAG, "后台服务已启动")
            
            // 检查是否启用悬浮球
            val preferencesManager = PreferencesManager(context)
            if (preferencesManager.isFloatingBallEnabled()) {
                val floatingIntent = Intent(context, FloatingBallService::class.java).apply {
                    action = FloatingBallService.ACTION_SHOW
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(floatingIntent)
                } else {
                    context.startService(floatingIntent)
                }
                
                LogUtil.d(TAG, "悬浮球服务已启动")
            }
            
        }, 3000) // 延迟 3 秒启动
    }
}
