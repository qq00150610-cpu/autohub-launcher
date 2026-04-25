/**
 * 登录ViewModel
 */
package com.autocar.launcher.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autocar.launcher.data.network.ApiClient
import com.autocar.launcher.util.PreferencesManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    // 登录状态
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val userId: Long, val token: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }
    
    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState
    
    // 验证码发送状态
    private val _codeSent = MutableLiveData<Boolean>()
    val codeSent: LiveData<Boolean> = _codeSent
    
    // 倒计时
    private val _countdown = MutableLiveData<Int>()
    val countdown: LiveData<Int> = _countdown
    
    // 是否已登录
    val isLoggedIn: Boolean
        get() = PreferencesManager.getInstance().isLoggedIn()
    
    fun sendSmsCode(phone: String, purpose: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = ApiClient.authService.sendSms(phone, purpose)
                if (response.isSuccessful && response.body()?.success == true) {
                    _codeSent.value = true
                    startCountdown()
                } else {
                    _loginState.value = LoginState.Error(response.body()?.message ?: "发送失败")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "网络错误")
            }
        }
    }
    
    fun sendEmailCode(email: String, purpose: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = ApiClient.authService.sendEmail(email, purpose)
                if (response.isSuccessful && response.body()?.success == true) {
                    _codeSent.value = true
                    startCountdown()
                } else {
                    _loginState.value = LoginState.Error(response.body()?.message ?: "发送失败")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "网络错误")
            }
        }
    }
    
    fun loginByPhone(phone: String, code: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = ApiClient.authService.loginByPhone(phone, code)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val userId = data?.userId ?: 0L
                    val token = data?.accessToken ?: ""
                    
                    // 保存登录状态
                    PreferencesManager.getInstance().apply {
                        saveToken(token)
                        saveUserId(userId)
                        setLoggedIn(true)
                    }
                    
                    _loginState.value = LoginState.Success(userId, token)
                } else {
                    _loginState.value = LoginState.Error(response.body()?.message ?: "登录失败")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "网络错误")
            }
        }
    }
    
    fun loginByEmail(email: String, code: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = ApiClient.authService.loginByEmail(email, code)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val userId = data?.userId ?: 0L
                    val token = data?.accessToken ?: ""
                    
                    PreferencesManager.getInstance().apply {
                        saveToken(token)
                        saveUserId(userId)
                        setLoggedIn(true)
                    }
                    
                    _loginState.value = LoginState.Success(userId, token)
                } else {
                    _loginState.value = LoginState.Error(response.body()?.message ?: "登录失败")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "网络错误")
            }
        }
    }
    
    fun loginByPassword(account: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = ApiClient.authService.loginByPassword(account, password)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val userId = data?.userId ?: 0L
                    val token = data?.accessToken ?: ""
                    
                    PreferencesManager.getInstance().apply {
                        saveToken(token)
                        saveUserId(userId)
                        setLoggedIn(true)
                    }
                    
                    _loginState.value = LoginState.Success(userId, token)
                } else {
                    _loginState.value = LoginState.Error(response.body()?.message ?: "登录失败")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "网络错误")
            }
        }
    }
    
    fun registerByPhone(phone: String, code: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = ApiClient.authService.registerByPhone(phone, code, password)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val userId = data?.userId ?: 0L
                    val token = data?.accessToken ?: ""
                    
                    PreferencesManager.getInstance().apply {
                        saveToken(token)
                        saveUserId(userId)
                        setLoggedIn(true)
                    }
                    
                    _loginState.value = LoginState.Success(userId, token)
                } else {
                    _loginState.value = LoginState.Error(response.body()?.message ?: "注册失败")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "网络错误")
            }
        }
    }
    
    fun registerByEmail(email: String, code: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = ApiClient.authService.registerByEmail(email, code, password)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val userId = data?.userId ?: 0L
                    val token = data?.accessToken ?: ""
                    
                    PreferencesManager.getInstance().apply {
                        saveToken(token)
                        saveUserId(userId)
                        setLoggedIn(true)
                    }
                    
                    _loginState.value = LoginState.Success(userId, token)
                } else {
                    _loginState.value = LoginState.Error(response.body()?.message ?: "注册失败")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "网络错误")
            }
        }
    }
    
    private fun startCountdown() {
        viewModelScope.launch {
            for (i in 60 downTo 0) {
                _countdown.value = i
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    fun logout() {
        PreferencesManager.getInstance().apply {
            setLoggedIn(false)
            clearToken()
        }
    }
    
    fun resetState() {
        _loginState.value = LoginState.Idle
        _codeSent.value = false
    }
}
