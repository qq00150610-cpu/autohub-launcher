package com.autocar.launcher.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.autocar.launcher.util.LogUtil

/**
 * 首页/桌面工具类
 * 提供桌面相关的辅助功能
 */
object HomeHelper {

    private const val TAG = "HomeHelper"
    
    /**
     * 检查是否为当前默认桌面
     */
    fun isCurrentHomeLauncher(context: Context): Boolean {
        val packageManager = context.packageManager
        val resolveInfo = packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.MATCH_DEFAULT_ONLY
        )
        
        val currentHomePackage = resolveInfo?.activityInfo?.packageName ?: ""
        val isHome = currentHomePackage == context.packageName
        
        LogUtil.d(TAG, "是否为当前桌面: $isHome, 当前桌面: $currentHomePackage")
        return isHome
    }
    
    /**
     * 打开桌面设置页面
     * 让用户选择默认桌面
     */
    fun openHomeSettings(context: Context) {
        try {
            // 尝试打开桌面选择器
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            LogUtil.e(TAG, "打开桌面设置失败", e)
            
            // 如果失败，尝试打开应用信息页面
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                LogUtil.e(TAG, "打开应用信息失败", e2)
                Toast.makeText(context, "请手动在设置中设置默认桌面", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * 请求设为默认桌面
     */
    fun requestSetAsHome(Context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${Context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            Context.startActivity(intent)
        } catch (e: Exception) {
            LogUtil.e(TAG, "请求设置失败", e)
        }
    }
    
    /**
     * 打开指定应用
     */
    fun openApp(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                LogUtil.d(TAG, "打开应用: $packageName")
            } else {
                LogUtil.w(TAG, "无法打开应用: $packageName")
                Toast.makeText(context, "无法打开该应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "打开应用失败: $packageName", e)
            Toast.makeText(context, "打开应用失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 获取已安装的启动器列表
     */
    fun getLauncherList(context: Context): List<ResolveInfo> {
        val packageManager = context.packageManager
        
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
    }
    
    /**
     * 获取应用图标
     */
    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            LogUtil.e(TAG, "获取图标失败: $packageName", e)
            null
        }
    }
    
    /**
     * 获取应用名称
     */
    fun getAppName(context: Context, packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            LogUtil.e(TAG, "获取应用名失败: $packageName", e)
            packageName
        }
    }
    
    /**
     * 检查应用是否已安装
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 打开系统设置
     */
    fun openSystemSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            LogUtil.e(TAG, "打开设置失败", e)
        }
    }
    
    /**
     * 打开通知设置
     */
    fun openNotificationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            LogUtil.e(TAG, "打开通知设置失败", e)
        }
    }
    
    /**
     * 打开悬浮窗设置
     */
    fun openOverlaySettings(context: Context) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            LogUtil.e(TAG, "打开悬浮窗设置失败", e)
        }
    }
    
    /**
     * 检查是否有悬浮窗权限
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}
