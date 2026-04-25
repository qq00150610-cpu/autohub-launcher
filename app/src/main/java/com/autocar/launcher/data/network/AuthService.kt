/**
 * 认证服务接口
 */
package com.autocar.launcher.data.network

import com.autocar.launcher.data.model.AuthResponse
import com.autocar.launcher.data.model.ApiResponse
import com.autocar.launcher.data.model.LoginRequest
import com.autocar.launcher.data.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.*

interface AuthService {

    /**
     * 手机号注册
     */
    @POST("auth/register/phone")
    fun registerByPhone(
        @Body request: RegisterRequest
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 邮箱注册
     */
    @POST("auth/register/email")
    fun registerByEmail(
        @Body request: RegisterRequest
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 手机号登录
     */
    @POST("auth/login/phone")
    fun loginByPhone(
        @Body request: LoginRequest
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 邮箱登录
     */
    @POST("auth/login/email")
    fun loginByEmail(
        @Body request: LoginRequest
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 账号密码登录
     */
    @POST("auth/login/password")
    fun loginByPassword(
        @Body request: LoginRequest
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 微信登录
     */
    @POST("auth/login/wechat")
    fun loginByWechat(
        @Body request: LoginRequest
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 发送短信验证码
     */
    @POST("auth/sms/send")
    fun sendSms(
        @Body request: SmsRequest
    ): Call<ApiResponse<String>>

    /**
     * 发送邮箱验证码
     */
    @POST("auth/email/send")
    fun sendEmail(
        @Body request: EmailRequest
    ): Call<ApiResponse<String>>

    /**
     * 刷新Token
     */
    @POST("auth/token/refresh")
    fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Call<ApiResponse<AuthResponse>>
}

/**
 * 短信请求
 */
data class SmsRequest(
    val phone: String,
    val type: String
)

/**
 * 邮箱请求
 */
data class EmailRequest(
    val email: String,
    val type: String
)

/**
 * 刷新Token请求
 */
data class RefreshTokenRequest(
    val refreshToken: String
)
