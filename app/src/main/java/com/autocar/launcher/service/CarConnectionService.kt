package com.autocar.launcher.service

import android.app.Notification
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Build
import com.autocar.launcher.AutoHubApplication
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseService
import com.autocar.launcher.ui.activity.MainActivity
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * 车辆连接服务
 * 通过蓝牙/OBD 与车辆通信，获取车辆数据
 * 
 * 功能：
 * - 蓝牙连接管理
 * - OBD-II 诊断协议
 * - 车辆数据读取（油耗、转速、车速等）
 * - 故障码读取
 */
class CarConnectionService : BaseService() {

    companion object {
        private const val TAG = "CarConnectionService"
        private const val NOTIFICATION_ID = 1005
        
        const val ACTION_CONNECT = "com.autocar.launcher.action.CONNECT_CAR"
        const val ACTION_DISCONNECT = "com.autocar.launcher.action.DISCONNECT_CAR"
        const val ACTION_READ_DATA = "com.autocar.launcher.action.READ_CAR_DATA"
        
        // OBD 蓝牙 UUID
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        
        // OBD 命令
        object OBDCommands {
            const val GET_DTC = "03"                    // 读取故障码
            const val CLEAR_DTC = "04"                   // 清除故障码
            const val ENGINE_RPM = "010C"               // 发动机转速
            const val VEHICLE_SPEED = "010D"            // 车速
            const val THROTTLE = "0111"                 // 节气门位置
            const val COOLANT_TEMP = "0105"             // 冷却液温度
            const val FUEL_LEVEL = "012F"               // 燃油液位
            const val ENGINE_LOAD = "0104"              // 发动机负荷
            const val INTAKE_TEMP = "010F"              // 进气温度
        }
    }
    
    // 协程作用域
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Bluetooth
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    // 连接状态
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    // 车辆数据
    private val _carData = MutableStateFlow(CarData())
    val carData: StateFlow<CarData> = _carData.asStateFlow()
    
    // 读取任务
    private var readJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        LogUtil.d(TAG, "车辆连接服务已创建")
    }
    
    override fun onHandleStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val deviceAddress = intent.getStringExtra("device_address")
                if (deviceAddress != null) {
                    connect(deviceAddress)
                }
            }
            ACTION_DISCONNECT -> disconnect()
            ACTION_READ_DATA -> readAllData()
        }
        return START_STICKY
    }
    
    override fun onHandleBind(intent: Intent?): android.os.IBinder? = null
    
    /**
     * 连接车辆
     */
    private fun connect(deviceAddress: String) {
        serviceScope.launch {
            try {
                _connectionState.value = ConnectionState.CONNECTING
                
                val device: BluetoothDevice = bluetoothAdapter?.getRemoteDevice(deviceAddress)
                    ?: throw IOException("无法找到蓝牙设备")
                
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothAdapter?.cancelDiscovery()
                
                bluetoothSocket?.connect()
                
                inputStream = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream
                
                // 初始化 OBD
                if (!initOBD()) {
                    throw IOException("OBD 初始化失败")
                }
                
                _connectionState.value = ConnectionState.CONNECTED
                startForeground(NOTIFICATION_ID, createNotification())
                
                // 开始读取数据
                startDataReading()
                
                LogUtil.d(TAG, "车辆连接成功")
                
            } catch (e: Exception) {
                LogUtil.e(TAG, "车辆连接失败", e)
                _connectionState.value = ConnectionState.ERROR
                disconnect()
            }
        }
    }
    
    /**
     * 断开连接
     */
    private fun disconnect() {
        readJob?.cancel()
        
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            LogUtil.e(TAG, "关闭连接时出错", e)
        } finally {
            inputStream = null
            outputStream = null
            bluetoothSocket = null
            _connectionState.value = ConnectionState.DISCONNECTED
            stopForeground(STOP_FOREGROUND_REMOVE)
            LogUtil.d(TAG, "车辆连接已断开")
        }
    }
    
    /**
     * 初始化 OBD 适配器
     */
    private suspend fun initOBD(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 发送 ATZ 重置
            sendCommand("ATZ")
            delay(1000)
            
            // 关闭回显
            sendCommand("ATE0")
            delay(200)
            
            // 关闭换行
            sendCommand("ATL0")
            delay(200)
            
            // 设置协议为自动
            sendCommand("ATSP0")
            delay(200)
            
            true
        } catch (e: Exception) {
            LogUtil.e(TAG, "OBD 初始化失败", e)
            false
        }
    }
    
    /**
     * 发送 OBD 命令
     */
    private suspend fun sendCommand(command: String): String = withContext(Dispatchers.IO) {
        try {
            outputStream?.write("$command\r".toByteArray())
            outputStream?.flush()
            
            delay(200) // 等待响应
            
            val buffer = ByteArray(1024)
            val bytesRead = inputStream?.read(buffer) ?: 0
            
            if (bytesRead > 0) {
                String(buffer, 0, bytesRead).trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "发送命令失败: $command", e)
            ""
        }
    }
    
    /**
     * 开始数据读取
     */
    private fun startDataReading() {
        readJob = serviceScope.launch {
            while (isActive && _connectionState.value == ConnectionState.CONNECTED) {
                readAllData()
                delay(1000) // 每秒读取一次
            }
        }
    }
    
    /**
     * 读取所有数据
     */
    private suspend fun readAllData() {
        if (_connectionState.value != ConnectionState.CONNECTED) return
        
        try {
            val rpm = parseRPM(sendCommand(OBDCommands.ENGINE_RPM))
            val speed = parseSpeed(sendCommand(OBDCommands.VEHICLE_SPEED))
            val coolantTemp = parseTemperature(sendCommand(OBDCommands.COOLANT_TEMP))
            val fuelLevel = parseFuelLevel(sendCommand(OBDCommands.FUEL_LEVEL))
            val throttle = parseThrottle(sendCommand(OBDCommands.THROTTLE))
            
            _carData.value = CarData(
                rpm = rpm,
                speed = speed,
                coolantTemperature = coolantTemp,
                fuelLevel = fuelLevel,
                throttlePosition = throttle,
                isConnected = true
            )
            
            updateNotification()
            
        } catch (e: Exception) {
            LogUtil.e(TAG, "读取车辆数据失败", e)
        }
    }
    
    // 解析方法（简化版）
    private fun parseRPM(response: String): Int {
        // 实际解析需要根据 OBD 响应格式实现
        return try {
            if (response.contains("41 0C")) {
                val hex = response.replace("41 0C", "").trim().replace(" ", "")
                if (hex.length >= 4) {
                    val value = hex.substring(0, 4).toInt(16)
                    (value / 4).toInt()
                } else 0
            } else 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun parseSpeed(response: String): Int {
        return try {
            if (response.contains("41 0D")) {
                val hex = response.replace("41 0D", "").trim().replace(" ", "")
                if (hex.isNotEmpty()) {
                    hex.toInt(16)
                } else 0
            } else 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun parseTemperature(response: String): Int {
        return try {
            if (response.contains("41 05")) {
                val hex = response.replace("41 05", "").trim().replace(" ", "")
                if (hex.isNotEmpty()) {
                    hex.toInt(16) - 40
                } else 0
            } else 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun parseFuelLevel(response: String): Int {
        return try {
            if (response.contains("41 2F")) {
                val hex = response.replace("41 2F", "").trim().replace(" ", "")
                if (hex.isNotEmpty()) {
                    (hex.toInt(16) * 100 / 255)
                } else 0
            } else 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun parseThrottle(response: String): Int {
        return try {
            if (response.contains("41 11")) {
                val hex = response.replace("41 11", "").trim().replace(" ", "")
                if (hex.isNotEmpty()) {
                    (hex.toInt(16) * 100 / 255)
                } else 0
            } else 0
        } catch (e: Exception) {
            0
        }
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
        
        val carData = _carData.value
        val contentText = if (carData.isConnected) {
            "车速: ${carData.speed} km/h | 转速: ${carData.rpm} RPM"
        } else {
            "正在连接车辆..."
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, AutoHubApplication.CHANNEL_CAR)
                .setContentTitle("车辆连接")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("车辆连接")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build()
        }
    }
    
    /**
     * 更新通知
     */
    private fun updateNotification() {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }
    
    override fun onDestroy() {
        disconnect()
        serviceScope.cancel()
        super.onDestroy()
        LogUtil.d(TAG, "车辆连接服务已销毁")
    }
    
    /**
     * 连接状态枚举
     */
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }
    
    /**
     * 车辆数据数据类
     */
    data class CarData(
        val rpm: Int = 0,
        val speed: Int = 0,
        val coolantTemperature: Int = 0,
        val fuelLevel: Int = 0,
        val throttlePosition: Int = 0,
        val isConnected: Boolean = false
    )
}
