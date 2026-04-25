/**
 * 主界面ViewModel
 */
package com.autocar.launcher.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autocar.launcher.data.model.AppInfo
import com.autocar.launcher.data.model.UserProfile
import com.autocar.launcher.data.repository.UserRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val userRepository by lazy { UserRepository() }
    
    // 用户信息
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile
    
    // 应用列表
    private val _apps = MutableLiveData<List<AppInfo>>()
    val apps: LiveData<List<AppInfo>> = _apps
    
    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 错误信息
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // 会员状态
    private val _memberLevel = MutableLiveData<Int>()
    val memberLevel: LiveData<Int> = _memberLevel
    
    // 系统状态
    private val _systemStatus = MutableLiveData<SystemStatus>()
    val systemStatus: LiveData<SystemStatus> = _systemStatus
    
    data class SystemStatus(
        val memoryUsage: Float = 0f,
        val cpuUsage: Float = 0f,
        val temperature: Float = 0f,
        val batteryLevel: Int = 0,
        val networkConnected: Boolean = false,
        val gpsEnabled: Boolean = false,
        val bluetoothEnabled: Boolean = false
    )
    
    init {
        loadUserProfile()
    }
    
    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = userRepository.getUserProfile()
                _userProfile.value = profile
                _memberLevel.value = profile?.memberLevel ?: 0
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadApps() {
        viewModelScope.launch {
            try {
                // 在实际应用中，这里会从PackageManager获取应用列表
                _apps.value = emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateSystemStatus() {
        viewModelScope.launch {
            // 更新系统状态
            val status = SystemStatus(
                memoryUsage = 0.5f,
                cpuUsage = 0.3f,
                temperature = 40f,
                batteryLevel = 80,
                networkConnected = true,
                gpsEnabled = true,
                bluetoothEnabled = false
            )
            _systemStatus.value = status
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
