/**
 * 用户Repository
 */
package com.autocar.launcher.data.repository

import com.autocar.launcher.data.model.AuthResponse
import com.autocar.launcher.data.model.UserProfile
import com.autocar.launcher.data.network.ApiClient
import com.autocar.launcher.util.LogUtil
import com.autocar.launcher.util.PreferencesManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepository {

    companion object {
        private const val TAG = "UserRepository"
    }
    
    /**
     * 获取用户信息
     */
    suspend fun getUserProfile(): UserProfile? {
        return try {
            val response = ApiClient.userService.getProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data
            } else {
                LogUtil.e(TAG, "获取用户信息失败: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "获取用户信息异常", e)
            null
        }
    }
    
    /**
     * 更新用户信息
     */
    suspend fun updateProfile(
        nickname: String? = null,
        gender: Int? = null,
        birthday: String? = null,
        bio: String? = null
    ): Boolean {
        return try {
            val body = mutableMapOf<String, Any?>()
            nickname?.let { body["nickname"] = it }
            gender?.let { body["gender"] = it }
            birthday?.let { body["birthday"] = it }
            bio?.let { body["bio"] = it }
            
            val response = ApiClient.userService.updateProfile(body)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            LogUtil.e(TAG, "更新用户信息异常", e)
            false
        }
    }
    
    /**
     * 修改密码
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        return try {
            val body = mapOf(
                "oldPassword" to oldPassword,
                "newPassword" to newPassword
            )
            val response = ApiClient.userService.changePassword(body)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            LogUtil.e(TAG, "修改密码异常", e)
            false
        }
    }
    
    /**
     * 上传头像
     */
    suspend fun uploadAvatar(file: File): String? {
        return try {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("avatar", file.name, requestBody)
            
            val response = ApiClient.userService.uploadAvatar(part)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.get("avatar")
            } else {
                null
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "上传头像异常", e)
            null
        }
    }
    
    /**
     * 绑定手机号
     */
    suspend fun bindPhone(phone: String, code: String): Boolean {
        return try {
            val body = mapOf(
                "phone" to phone,
                "code" to code
            )
            val response = ApiClient.userService.bindPhone(body)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            LogUtil.e(TAG, "绑定手机号异常", e)
            false
        }
    }
    
    /**
     * 绑定邮箱
     */
    suspend fun bindEmail(email: String, code: String): Boolean {
        return try {
            val body = mapOf(
                "email" to email,
                "code" to code
            )
            val response = ApiClient.userService.bindEmail(body)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            LogUtil.e(TAG, "绑定邮箱异常", e)
            false
        }
    }
    
    /**
     * 刷新Token
     */
    suspend fun refreshToken(): AuthResponse? {
        return try {
            val refreshToken = PreferencesManager.getInstance().getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                return null
            }
            
            val body = mapOf("refresh_token" to refreshToken)
            val response = ApiClient.authService.refreshToken(body)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                
                // 保存新Token
                data?.let {
                    PreferencesManager.getInstance().apply {
                        saveToken(it.accessToken)
                        saveRefreshToken(it.refreshToken)
                    }
                }
                
                data
            } else {
                LogUtil.e(TAG, "刷新Token失败")
                null
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "刷新Token异常", e)
            null
        }
    }
    
    /**
     * 退出登录
     */
    suspend fun logout(): Boolean {
        return try {
            val response = ApiClient.authService.logout()
            
            // 清除本地登录状态
            PreferencesManager.getInstance().apply {
                setLoggedIn(false)
                clearToken()
            }
            
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            LogUtil.e(TAG, "退出登录异常", e)
            false
        }
    }
}
