package com.autocar.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.autocar.launcher.util.LogUtil
import com.autocar.launcher.util.PreferencesManager

/**
 * 主屏幕切换广播接收器
 * 监听桌面切换事件，用于检测是否为当前桌面
 * 
 * 功能：
 * - 监听 HOME 屏幕切换
 * - 检测凹凸桌面是否被设为默认桌面
 * - 记录其他桌面包名
 */
class HomeChangeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "HomeChangeReceiver"
        
        // 凹凸桌面包名
        const val AUTOHUB_PACKAGE = "com.autocar.launcher"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        // 检查是否是 MAIN 动作
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            handleHomeChange(context)
        }
    }
    
    /**
     * 处理桌面切换
     */
    private fun handleHomeChange(context: Context) {
        val packageManager = context.packageManager
        
        // 解析 intent 获取启动的 Activity 信息
        val resolveInfo = packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
        )
        
        val currentHomePackage = resolveInfo?.activityInfo?.packageName ?: "unknown"
        
        LogUtil.d(TAG, "当前桌面: $currentHomePackage")
        
        // 检查是否是凹凸桌面
        val isAutoHubHome = currentHomePackage == AUTOHUB_PACKAGE
        
        // 保存状态
        val preferencesManager = PreferencesManager(context)
        preferencesManager.setIsCurrentHome(isAutoHubHome)
        
        if (isAutoHubHome) {
            LogUtil.d(TAG, "凹凸桌面是当前默认桌面")
            onAutoHubBecameHome(context)
        } else {
            LogUtil.d(TAG, "凹凸桌面不是当前默认桌面")
            onAutoHubLostHome(context)
        }
    }
    
    /**
     * 凹凸桌面成为主桌面时回调
     */
    private fun onAutoHubBecameHome(context: Context) {
        // TODO: 执行相关操作
        // - 显示提示
        // - 初始化快捷方式
        // - 更新小组件
    }
    
    /**
     * 凹凸桌面不再是主桌面时回调
     */
    private fun onAutoHubLostHome(context: Context) {
        // TODO: 执行相关操作
        // - 显示设置提示
        // - 保存用户状态
    }
}
