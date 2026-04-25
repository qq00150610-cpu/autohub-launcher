/**
 * 认证服务接口
 */
package com.autocar.launcher.data.network

import com.autocar.launcher.data.model.AuthResponse
import com.autocar.launcher.data.model.ApiResponse
import retrofit2.Call
import retrofit2.http.*

interface AuthService {

    /**
     * 手机号注册
     */
    @POST("auth/register/phone")
    fun registerByPhone(
        @Field("phone") phone: String,
        @Field("password") password: String,
        @Field("code") code: String
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 邮箱注册
     */
    @POST("auth/register/email")
    fun registerByEmail(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("code") code: String
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 手机号登录
     */
    @POST("auth/login/phone")
    fun loginByPhone(
        @Field("phone") phone: String,
        @Field("password") password: String
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 邮箱登录
     */
    @POST("auth/login/email")
    fun loginByEmail(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 账号密码登录
     */
    @POST("auth/login/password")
    fun loginByPassword(
        @Field("account") account: String,
        @Field("password") password: String
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 微信登录
     */
    @POST("auth/login/wechat")
    fun loginByWechat(
        @Field("code") code: String,
        @Field("nickname") nickname: String?
    ): Call<ApiResponse<AuthResponse>>

    /**
     * 发送手机验证码
     */
    @POST("auth/sms/send")
    fun sendSms(
        @Field("phone") phone: String,
        @Field("type") type: String
    ): Call<ApiResponse<Unit>>

    /**
     * 发送邮箱验证码
     */
    @POST("auth/email/send")
    fun sendEmail(
        @Field("email") email: String,
        @Field("type") type: String
    ): Call<ApiResponse<Unit>>

    /**
     * 刷新Token
     */
    @POST("auth/token/refresh")
    fun refreshToken(
        @Field("refreshToken") refreshToken: String
    ): Call<ApiResponse<AuthResponse>>
}
