package com.autocar.launcher.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * 偏好设置管理器
 * 管理应用配置和用户偏好设置
 * 
 * 功能：
 * - 保存/读取配置
 * - 提供默认值
 * - 支持各种数据类型
 */
class PreferencesManager(context: Context) {

    companion object {
        // 文件名
        private const val PREF_NAME = "autohub_preferences"
        
        // Key 名称
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_IS_HOME = "is_current_home"
        private const val KEY_FLOATING_BALL_ENABLED = "floating_ball_enabled"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_AUTO_START = "auto_start_enabled"
        private const val KEY_SHOW_STATUS_BAR = "show_status_bar"
        private const val KEY_SHOW_NAVIGATION_BAR = "show_navigation_bar"
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_THEME_COLOR = "theme_color"
        private const val KEY_GRID_COLUMNS = "grid_columns"
        private const val KEY_GRID_ROWS = "grid_rows"
        private const val KEY_SHOW_APP_NAMES = "show_app_names"
        private const val KEY_SORT_ORDER = "sort_order"
        private const val KEY_LAST_UPDATE_CHECK = "last_update_check"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_VOICE_ENABLED = "voice_enabled"
        private const val KEY_CAR_BT_ADDRESS = "car_bt_address"
        private const val KEY_CAR_BT_NAME = "car_bt_name"
        private const val KEY_HOME_LAYOUT = "home_layout"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    // ==================== 基础配置 ====================
    
    /**
     * 是否首次启动
     */
    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    
    fun setFirstLaunch(isFirst: Boolean) {
        prefs.edit { putBoolean(KEY_FIRST_LAUNCH, isFirst) }
    }
    
    /**
     * 是否当前桌面
     */
    fun isCurrentHome(): Boolean = prefs.getBoolean(KEY_IS_HOME, false)
    
    fun setIsCurrentHome(isHome: Boolean) {
        prefs.edit { putBoolean(KEY_IS_HOME, isHome) }
    }
    
    // ==================== 功能开关 ====================
    
    /**
     * 悬浮球是否启用
     */
    fun isFloatingBallEnabled(): Boolean = prefs.getBoolean(KEY_FLOATING_BALL_ENABLED, true)
    
    fun setFloatingBallEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_FLOATING_BALL_ENABLED, enabled) }
    }
    
    /**
     * 深色模式
     */
    fun isDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)
    
    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DARK_MODE, enabled) }
    }
    
    /**
     * 开机自启
     */
    fun isAutoStartEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_START, true)
    
    fun setAutoStartEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO_START, enabled) }
    }
    
    /**
     * 显示状态栏
     */
    fun isStatusBarShown(): Boolean = prefs.getBoolean(KEY_SHOW_STATUS_BAR, true)
    
    fun setShowStatusBar(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_STATUS_BAR, show) }
    }
    
    /**
     * 显示导航栏
     */
    fun isNavigationBarShown(): Boolean = prefs.getBoolean(KEY_SHOW_NAVIGATION_BAR, true)
    
    fun setShowNavigationBar(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_NAVIGATION_BAR, show) }
    }
    
    /**
     * 通知是否启用
     */
    fun isNotificationEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    
    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATION_ENABLED, enabled) }
    }
    
    /**
     * 语音助手是否启用
     */
    fun isVoiceEnabled(): Boolean = prefs.getBoolean(KEY_VOICE_ENABLED, true)
    
    fun setVoiceEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_VOICE_ENABLED, enabled) }
    }
    
    // ==================== 外观配置 ====================
    
    /**
     * 壁纸 URI
     */
    fun getWallpaperUri(): String? = prefs.getString(KEY_WALLPAPER_URI, null)
    
    fun setWallpaperUri(uri: String?) {
        prefs.edit { putString(KEY_WALLPAPER_URI, uri) }
    }
    
    /**
     * 主题颜色
     */
    fun getThemeColor(): Int = prefs.getInt(KEY_THEME_COLOR, 0xFF2196F3.toInt())
    
    fun setThemeColor(color: Int) {
        prefs.edit { putInt(KEY_THEME_COLOR, color) }
    }
    
    /**
     * 网格列数
     */
    fun getGridColumns(): Int = prefs.getInt(KEY_GRID_COLUMNS, 6)
    
    fun setGridColumns(columns: Int) {
        prefs.edit { putInt(KEY_GRID_COLUMNS, columns) }
    }
    
    /**
     * 网格行数
     */
    fun getGridRows(): Int = prefs.getInt(KEY_GRID_ROWS, 3)
    
    fun setGridRows(rows: Int) {
        prefs.edit { putInt(KEY_GRID_ROWS, rows) }
    }
    
    /**
     * 是否显示应用名称
     */
    fun isShowAppNames(): Boolean = prefs.getBoolean(KEY_SHOW_APP_NAMES, true)
    
    fun setShowAppNames(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_APP_NAMES, show) }
    }
    
    // ==================== 排序配置 ====================
    
    /**
     * 排序方式
     * 0: 按名称
     * 1: 按安装时间
     * 2: 按使用频率
     */
    fun getSortOrder(): Int = prefs.getInt(KEY_SORT_ORDER, 0)
    
    fun setSortOrder(order: Int) {
        prefs.edit { putInt(KEY_SORT_ORDER, order) }
    }
    
    // ==================== 车辆配置 ====================
    
    /**
     * 车辆蓝牙地址
     */
    fun getCarBluetoothAddress(): String? = prefs.getString(KEY_CAR_BT_ADDRESS, null)
    
    fun setCarBluetoothAddress(address: String?) {
        prefs.edit { putString(KEY_CAR_BT_ADDRESS, address) }
    }
    
    /**
     * 车辆蓝牙名称
     */
    fun getCarBluetoothName(): String? = prefs.getString(KEY_CAR_BT_NAME, null)
    
    fun setCarBluetoothName(name: String?) {
        prefs.edit { putString(KEY_CAR_BT_NAME, name) }
    }
    
    // ==================== 其他配置 ====================
    
    /**
     * 首页布局类型
     */
    fun getHomeLayout(): String = prefs.getString(KEY_HOME_LAYOUT, "grid") ?: "grid"
    
    fun setHomeLayout(layout: String) {
        prefs.edit { putString(KEY_HOME_LAYOUT, layout) }
    }
    
    /**
     * 上次检查更新时间
     */
    fun getLastUpdateCheck(): Long = prefs.getLong(KEY_LAST_UPDATE_CHECK, 0)
    
    fun setLastUpdateCheck(time: Long) {
        prefs.edit { putLong(KEY_LAST_UPDATE_CHECK, time) }
    }
    
    /**
     * 清空所有配置
     */
    fun clearAll() {
        prefs.edit { clear() }
    }
}
