/**
 * 设置Activity
 */
package com.autocar.launcher.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.databinding.ActivitySettingsBinding
import com.autocar.launcher.ui.viewmodel.SettingsViewModel
import com.autocar.launcher.util.*
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    companion object {
        private const val TAG = "SettingsActivity"
    }

    private val viewModel by lazy { SettingsViewModel() }

    override fun createBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 保持屏幕
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun initData() {
        loadSettings()
    }

    override fun initListener() {
        // 返回按钮
        binding.btnBack?.setOnClickListener {
            finish()
        }
        
        // 主题设置
        binding.layoutTheme?.setOnClickListener {
            openThemePicker()
        }
        
        // 壁纸设置
        binding.layoutWallpaper?.setOnClickListener {
            openWallpaperPicker()
        }
        
        // 声音设置
        binding.layoutSound?.setOnClickListener {
            openSoundSettings()
        }
        
        // 显示设置
        binding.layoutDisplay?.setOnClickListener {
            openDisplaySettings()
        }
        
        // 悬浮球设置
        binding.switchFloatingBall?.setOnCheckedChangeListener { _, isChecked ->
            updateFloatingBallSetting(isChecked)
        }
        
        // 驾驶模式设置
        binding.switchDrivingMode?.setOnCheckedChangeListener { _, isChecked ->
            updateDrivingModeSetting(isChecked)
        }
        
        // 自动启动设置
        binding.switchAutoStart?.setOnCheckedChangeListener { _, isChecked ->
            updateAutoStartSetting(isChecked)
        }
        
        // 清除缓存
        binding.layoutClearCache?.setOnClickListener {
            clearCache()
        }
        
        // 关于
        binding.layoutAbout?.setOnClickListener {
            showAbout()
        }
        
        // 账号设置
        binding.layoutAccount?.setOnClickListener {
            openAccountSettings()
        }
    }

    private fun loadSettings() {
        val prefs = PreferencesManager.getInstance()
        
        // 加载主题设置
        val theme = prefs.getString("theme", "auto")
        binding.tvThemeValue?.text = when (theme) {
            "light" -> "浅色"
            "dark" -> "深色"
            else -> "跟随系统"
        }
        
        // 加载悬浮球设置
        binding.switchFloatingBall?.isChecked = prefs.getBoolean("floating_ball_enabled", true)
        
        // 加载驾驶模式设置
        binding.switchDrivingMode?.isChecked = prefs.getBoolean("driving_mode_enabled", true)
        
        // 加载自动启动设置
        binding.switchAutoStart?.isChecked = prefs.getBoolean("auto_start_enabled", true)
        
        // 更新缓存大小
        updateCacheSize()
    }

    private fun updateCacheSize() {
        lifecycleScope.launch {
            val cacheSize = with(kotlinx.coroutines.Dispatchers.IO) {
                calculateCacheSize()
            }
            binding.tvCacheSize?.text = cacheSize
        }
    }

    private fun calculateCacheSize(): String {
        var size = 0L
        try {
            val cacheDir = cacheDir
            size += getFolderSize(cacheDir)
            
            val externalCacheDir = externalCacheDir
            if (externalCacheDir != null) {
                size += getFolderSize(externalCacheDir)
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "计算缓存大小失败", e)
        }
        return formatFileSize(size)
    }

    private fun getFolderSize(dir: java.io.File): Long {
        var size = 0L
        if (dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    size += if (file.isDirectory) {
                        getFolderSize(file)
                    } else {
                        file.length()
                    }
                }
            }
        }
        return size
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)}MB"
            else -> "${size / (1024 * 1024 * 1024)}GB"
        }
    }

    private fun openThemePicker() {
        startActivity(Intent(this, ThemePickerActivity::class.java))
    }

    private fun openWallpaperPicker() {
        startActivity(Intent(this, WallpaperPickerActivity::class.java))
    }

    private fun openSoundSettings() {
        showToast("声音设置")
    }

    private fun openDisplaySettings() {
        showToast("显示设置")
    }

    private fun updateFloatingBallSetting(enabled: Boolean) {
        PreferencesManager.getInstance().putBoolean("floating_ball_enabled", enabled)
        
        if (enabled) {
            // 启动悬浮球服务
            val intent = Intent(this, com.autocar.launcher.service.FloatingBallService::class.java)
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O -> {
                startForegroundService(intent)
            } ?: startService(intent)
        } else {
            // 停止悬浮球服务
            val intent = Intent(this, com.autocar.launcher.service.FloatingBallService::class.java)
            stopService(intent)
        }
    }

    private fun updateDrivingModeSetting(enabled: Boolean) {
        PreferencesManager.getInstance().putBoolean("driving_mode_enabled", enabled)
    }

    private fun updateAutoStartSetting(enabled: Boolean) {
        PreferencesManager.getInstance().putBoolean("auto_start_enabled", enabled)
        
        if (enabled) {
            // 启用自动启动
            enableAutoStart()
        } else {
            // 禁用自动启动
            disableAutoStart()
        }
    }

    private fun enableAutoStart() {
        try {
            val pm = packageManager
            val componentName = ComponentName(this, com.autocar.launcher.receiver.BootReceiver::class.java)
            pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            LogUtil.e(TAG, "启用自动启动失败", e)
        }
    }

    private fun disableAutoStart() {
        try {
            val pm = packageManager
            val componentName = ComponentName(this, com.autocar.launcher.receiver.BootReceiver::class.java)
            pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            LogUtil.e(TAG, "禁用自动启动失败", e)
        }
    }

    private fun clearCache() {
        AlertDialog.Builder(this)
            .setTitle("清除缓存")
            .setMessage("确定要清除所有缓存吗？")
            .setPositiveButton("确定") { _, _ ->
                lifecycleScope.launch {
                    with(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            cacheDir.deleteRecursively()
                            externalCacheDir?.deleteRecursively()
                        } catch (e: Exception) {
                            LogUtil.e(TAG, "清除缓存失败", e)
                        }
                    }
                    updateCacheSize()
                    showToast("缓存已清除")
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("关于凹凸桌面")
            .setMessage("""
                凹凸桌面 v${getAppVersion()}
                Build: ${getBuildNumber()}
                
                一款专为车载环境打造的智能桌面启动器。
                
                © 2024 AutoHub Team
            """.trimIndent())
            .setPositiveButton("确定", null)
            .show()
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun getBuildNumber(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
        } catch (e: Exception) {
            "1"
        }
    }

    private fun openAccountSettings() {
        if (PreferencesManager.getInstance().isLoggedIn()) {
            // 打开会员中心
            startActivity(Intent(this, MemberCenterActivity::class.java))
        } else {
            // 打开登录页面
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
