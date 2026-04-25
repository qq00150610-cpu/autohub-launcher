package com.autocar.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.autocar.launcher.util.LogUtil

/**
 * 应用包变化广播接收器
 * 监听应用安装、更新、卸载事件
 * 
 * 功能：
 * - 监听应用安装
 * - 监听应用卸载
 * - 监听应用更新
 * - 更新应用列表缓存
 */
class PackageChangeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PackageChangeReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart
        
        LogUtil.d(TAG, "收到包变化广播: ${intent.action}, 包名: $packageName")
        
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                handlePackageAdded(context, packageName)
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                handlePackageRemoved(context, packageName)
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                handlePackageUpdated(context, packageName)
            }
        }
    }
    
    /**
     * 处理应用安装
     */
    private fun handlePackageAdded(context: Context, packageName: String?) {
        if (packageName.isNullOrEmpty()) return
        
        LogUtil.d(TAG, "应用已安装: $packageName")
        
        // TODO: 
        // - 更新应用列表
        // - 检查是否是导航/音乐应用
        // - 发送通知
    }
    
    /**
     * 处理应用卸载
     */
    private fun handlePackageRemoved(context: Context, packageName: String?) {
        if (packageName.isNullOrEmpty()) return
        
        LogUtil.d(TAG, "应用已卸载: $packageName")
        
        // TODO:
        // - 从应用列表移除
        // - 检查是否在快捷方式中
        // - 清理相关缓存
    }
    
    /**
     * 处理应用更新
     */
    private fun handlePackageUpdated(context: Context, packageName: String?) {
        if (packageName.isNullOrEmpty()) return
        
        LogUtil.d(TAG, "应用已更新: $packageName")
        
        // TODO:
        // - 刷新应用信息
        // - 重新加载图标
    }
}
