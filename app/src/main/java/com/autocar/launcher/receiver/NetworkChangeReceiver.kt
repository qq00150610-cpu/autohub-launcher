package com.autocar.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 网络状态变化广播接收器
 * 监听网络连接状态变化
 * 
 * 功能：
 * - 监听 WiFi 状态
 * - 监听移动数据状态
 * - 监听网络连接状态
 * - 提供网络状态回调
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NetworkChangeReceiver"
        
        // 网络类型
        const val NETWORK_NONE = 0
        const val NETWORK_MOBILE = 1
        const val NETWORK_WIFI = 2
        const val NETWORK_ETHERNET = 3
        const val NETWORK_BLUETOOTH = 4
        
        // 网络状态Flow
        private val _networkState = MutableStateFlow(NetworkState())
        val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                handleConnectivityChange(context)
            }
            "android.net.wifi.WIFI_STATE_CHANGED" -> {
                handleWifiStateChange(intent)
            }
        }
    }
    
    /**
     * 处理连接状态变化
     */
    private fun handleConnectivityChange(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        val isConnected = capabilities != null
        val networkType = getNetworkType(capabilities)
        
        val networkState = NetworkState(
            isConnected = isConnected,
            networkType = networkType,
            isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true,
            isMobile = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true,
            isEthernet = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
        )
        
        _networkState.value = networkState
        
        LogUtil.d(TAG, "网络状态变化: isConnected=$isConnected, type=$networkType")
    }
    
    /**
     * 处理 WiFi 状态变化
     */
    private fun handleWifiStateChange(intent: Intent) {
        val wifiState = intent.getIntExtra("wifi_state", -1)
        
        val stateText = when (wifiState) {
            1 -> "WiFi 正在关闭"
            2 -> "WiFi 已关闭"
            3 -> "WiFi 正在开启"
            4 -> "WiFi 已开启"
            else -> "未知状态"
        }
        
        LogUtil.d(TAG, "WiFi 状态: $stateText")
    }
    
    /**
     * 获取网络类型
     */
    private fun getNetworkType(capabilities: NetworkCapabilities?): Int {
        return when {
            capabilities == null -> NETWORK_NONE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NETWORK_WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NETWORK_MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NETWORK_ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NETWORK_BLUETOOTH
            else -> NETWORK_NONE
        }
    }
    
    /**
     * 网络状态数据类
     */
    data class NetworkState(
        val isConnected: Boolean = false,
        val networkType: Int = NETWORK_NONE,
        val isWifi: Boolean = false,
        val isMobile: Boolean = false,
        val isEthernet: Boolean = false
    )
}
