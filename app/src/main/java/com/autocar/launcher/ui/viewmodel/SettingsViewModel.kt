/**
 * 设置ViewModel
 */
package com.autocar.launcher.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocar.launcher.util.PreferencesManager

class SettingsViewModel : ViewModel() {

    // 主题设置
    private val _theme = MutableLiveData<String>()
    val theme: LiveData<String> = _theme
    
    // 悬浮球设置
    private val _floatingBallEnabled = MutableLiveData<Boolean>()
    val floatingBallEnabled: LiveData<Boolean> = _floatingBallEnabled
    
    // 驾驶模式设置
    private val _drivingModeEnabled = MutableLiveData<Boolean>()
    val drivingModeEnabled: LiveData<Boolean> = _drivingModeEnabled
    
    // 自动启动设置
    private val _autoStartEnabled = MutableLiveData<Boolean>()
    val autoStartEnabled: LiveData<Boolean> = _autoStartEnabled
    
    // 云同步设置
    private val _cloudSyncEnabled = MutableLiveData<Boolean>()
    val cloudSyncEnabled: LiveData<Boolean> = _cloudSyncEnabled
    
    init {
        loadSettings()
    }
    
    fun loadSettings() {
        val prefs = PreferencesManager.getInstance()
        
        _theme.value = prefs.getString("theme", "auto")
        _floatingBallEnabled.value = prefs.getBoolean("floating_ball_enabled", true)
        _drivingModeEnabled.value = prefs.getBoolean("driving_mode_enabled", true)
        _autoStartEnabled.value = prefs.getBoolean("auto_start_enabled", true)
        _cloudSyncEnabled.value = prefs.getBoolean("cloud_sync_enabled", false)
    }
    
    fun setTheme(theme: String) {
        PreferencesManager.getInstance().putString("theme", theme)
        _theme.value = theme
    }
    
    fun setFloatingBallEnabled(enabled: Boolean) {
        PreferencesManager.getInstance().putBoolean("floating_ball_enabled", enabled)
        _floatingBallEnabled.value = enabled
    }
    
    fun setDrivingModeEnabled(enabled: Boolean) {
        PreferencesManager.getInstance().putBoolean("driving_mode_enabled", enabled)
        _drivingModeEnabled.value = enabled
    }
    
    fun setAutoStartEnabled(enabled: Boolean) {
        PreferencesManager.getInstance().putBoolean("auto_start_enabled", enabled)
        _autoStartEnabled.value = enabled
    }
    
    fun setCloudSyncEnabled(enabled: Boolean) {
        PreferencesManager.getInstance().putBoolean("cloud_sync_enabled", enabled)
        _cloudSyncEnabled.value = enabled
    }
}
