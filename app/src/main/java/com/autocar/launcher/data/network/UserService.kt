/**
 * 用户服务接口
 */
package com.autocar.launcher.data.network

import com.autocar.launcher.data.model.ApiResponse
import com.autocar.launcher.data.model.UserProfile
import retrofit2.Call
import retrofit2.http.*

interface UserService {

    /**
     * 获取用户资料
     */
    @GET("user/profile")
    fun getProfile(): Call<ApiResponse<UserProfile>>

    /**
     * 更新用户资料
     */
    @POST("user/profile/update")
    fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Call<ApiResponse<UserProfile>>

    /**
     * 修改密码
     */
    @POST("user/password/change")
    fun changePassword(
        @Body request: ChangePasswordRequest
    ): Call<ApiResponse<Unit>>
}

/**
 * 更新资料请求
 */
data class UpdateProfileRequest(
    val nickname: String? = null,
    val avatar: String? = null
)

/**
 * 修改密码请求
 */
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
