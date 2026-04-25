package com.autocar.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 车辆状态广播接收器
 * 监听电池和电源状态
 * 
 * 功能：
 * - 监听电池电量变化
 * - 监听充电状态
 * - 监听电源连接
 * - 车辆状态模拟
 */
class CarStatusReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CarStatusReceiver"
        
        private val _carStatus = MutableStateFlow(CarStatus())
        val carStatus: StateFlow<CarStatus> = _carStatus.asStateFlow()
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                handleBatteryChanged(intent)
            }
            Intent.ACTION_BATTERY_LOW -> {
                handleBatteryLow()
            }
            Intent.ACTION_BATTERY_OKAY -> {
                handleBatteryOkay()
            }
            Intent.ACTION_POWER_CONNECTED -> {
                handlePowerConnected()
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                handlePowerDisconnected()
            }
        }
    }
    
    /**
     * 处理电池变化
     */
    private fun handleBatteryChanged(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val batteryPercent = (level * 100) / scale
        
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargeType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "交流电"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线充电"
            else -> "未充电"
        }
        
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0
        
        _carStatus.value = CarStatus(
            batteryLevel = batteryPercent,
            isCharging = isCharging,
            chargeType = chargeType,
            temperature = temperature,
            voltage = voltage,
            isPowerConnected = plugged != 0
        )
        
        LogUtil.d(TAG, "电池: $batteryPercent%, 充电中: $isCharging, 类型: $chargeType")
    }
    
    /**
     * 处理电池电量低
     */
    private fun handleBatteryLow() {
        _carStatus.value = _carStatus.value.copy(isBatteryLow = true)
        LogUtil.w(TAG, "电池电量低！")
        
        // TODO: 显示低电量警告
    }
    
    /**
     * 处理电池恢复正常
     */
    private fun handleBatteryOkay() {
        _carStatus.value = _carStatus.value.copy(isBatteryLow = false)
        LogUtil.d(TAG, "电池电量恢复正常")
    }
    
    /**
     * 处理电源连接
     */
    private fun handlePowerConnected() {
        _carStatus.value = _carStatus.value.copy(isPowerConnected = true)
        LogUtil.d(TAG, "电源已连接")
        
        // 车辆启动信号
        onCarStarted()
    }
    
    /**
     * 处理电源断开
     */
    private fun handlePowerDisconnected() {
        _carStatus.value = _carStatus.value.copy(isPowerConnected = false)
        LogUtil.d(TAG, "电源已断开")
        
        // 车辆熄火信号
        onCarStopped()
    }
    
    /**
     * 车辆启动回调
     */
    private fun onCarStarted() {
        LogUtil.d(TAG, "=== 车辆启动 ===")
        // TODO:
        // - 启动所有服务
        // - 恢复用户设置
        // - 初始化车辆连接
    }
    
    /**
     * 车辆熄火回调
     */
    private fun onCarStopped() {
        LogUtil.d(TAG, "=== 车辆熄火 ===")
        // TODO:
        // - 保存用户状态
        // - 延迟关闭服务
        // - 进入待机模式
    }
    
    /**
     * 车辆状态数据类
     */
    data class CarStatus(
        val batteryLevel: Int = 100,
        val isCharging: Boolean = false,
        val chargeType: String = "未充电",
        val temperature: Double = 25.0,
        val voltage: Double = 3.7,
        val isPowerConnected: Boolean = false,
        val isBatteryLow: Boolean = false
    )
}
