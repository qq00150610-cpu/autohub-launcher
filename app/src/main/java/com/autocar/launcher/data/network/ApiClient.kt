/**
 * API客户端 - Retrofit配置
 */
package com.autocar.launcher.data.network

import com.autocar.launcher.data.model.*
import com.autocar.launcher.util.PreferencesManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

object ApiClient {

    // API基础URL - 请根据实际部署修改
    private const val BASE_URL = "https://api.autohub.com/"
    
    // 超时时间（秒）
    private const val TIMEOUT = 30L
    
    // OkHttpClient
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(LogInterceptor())
            .retryOnConnectionFailure(true)
            .build()
    }
    
    // Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // 认证服务
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    
    // 用户服务
    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }
    
    // 会员服务
    val memberService: MemberService by lazy {
        retrofit.create(MemberService::class.java)
    }
    
    // 通用服务
    val commonService: CommonService by lazy {
        retrofit.create(CommonService::class.java)
    }
    
    /**
     * 认证拦截器 - 添加Token
     */
    class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            
            // 获取Token
            val token = PreferencesManager.getInstance().getToken()
            
            // 如果没有Token，直接请求
            if (token.isNullOrBlank()) {
                return chain.proceed(originalRequest)
            }
            
            // 添加认证头
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
            
            return chain.proceed(newRequest)
        }
    }
    
    /**
     * 日志拦截器
     */
    class LogInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            
            val startTime = System.nanoTime()
            
            val response = chain.proceed(request)
            
            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1_000_000
            
            // 打印日志
            android.util.Log.d("ApiClient", """
                请求: ${request.url}
                方法: ${request.method}
                耗时: ${duration}ms
                响应: ${response.code}
            """.trimIndent())
            
            return response
        }
    }
}

/**
 * 认证服务接口
 */
interface AuthService {
    
    // 手机号注册
    @POST("api/auth/register/phone")
    suspend fun registerByPhone(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<AuthResponse>>
    
    // 邮箱注册
    @POST("api/auth/register/email")
    suspend fun registerByEmail(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<AuthResponse>>
    
    // 手机号登录
    @POST("api/auth/login/phone")
    suspend fun loginByPhone(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<AuthResponse>>
    
    // 邮箱登录
    @POST("api/auth/login/email")
    suspend fun loginByEmail(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<AuthResponse>>
    
    // 密码登录
    @POST("api/auth/login/password")
    suspend fun loginByPassword(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<AuthResponse>>
    
    // 微信登录
    @POST("api/auth/login/wechat")
    suspend fun loginByWechat(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<AuthResponse>>
    
    // 发送短信验证码
    @POST("api/auth/send-sms")
    suspend fun sendSms(
        @Query("phone") phone: String,
        @Query("purpose") purpose: String
    ): retrofit2.Response<ApiResponse<Any>>
    
    // 发送邮箱验证码
    @POST("api/auth/send-email")
    suspend fun sendEmail(
        @Query("email") email: String,
        @Query("purpose") purpose: String
    ): retrofit2.Response<ApiResponse<Any>>
    
    // 刷新Token
    @POST("api/auth/refresh-token")
    suspend fun refreshToken(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<AuthResponse>>
    
    // 退出登录
    @POST("api/auth/logout")
    suspend fun logout(): retrofit2.Response<ApiResponse<Any>>
    
    // 重置密码
    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<Any>>
}

/**
 * 用户服务接口
 */
interface UserService {
    
    // 获取用户信息
    @GET("api/user/profile")
    suspend fun getProfile(): retrofit2.Response<ApiResponse<UserProfile>>
    
    // 更新用户信息
    @PUT("api/user/profile")
    suspend fun updateProfile(
        @Body body: Map<String, Any?>
    ): retrofit2.Response<ApiResponse<Any>>
    
    // 修改密码
    @PUT("api/user/password")
    suspend fun changePassword(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<Any>>
    
    // 上传头像
    @Multipart
    @POST("api/user/avatar")
    suspend fun uploadAvatar(
        @Part avatar: okhttp3.MultipartBody.Part
    ): retrofit2.Response<ApiResponse<Map<String, String>>>
    
    // 绑定手机号
    @POST("api/user/bind-phone")
    suspend fun bindPhone(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<Any>>
    
    // 绑定邮箱
    @POST("api/user/bind-email")
    suspend fun bindEmail(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<Any>>
}

/**
 * 会员服务接口
 */
interface MemberService {
    
    // 获取会员信息
    @GET("api/member/info")
    suspend fun getMemberInfo(): retrofit2.Response<ApiResponse<MemberInfo>>
    
    // 获取会员权益
    @GET("api/member/benefits")
    suspend fun getMemberBenefits(): retrofit2.Response<ApiResponse<List<MemberBenefit>>>
    
    // 获取会员产品
    @GET("api/member/products")
    suspend fun getMemberProducts(): retrofit2.Response<ApiResponse<List<MemberProduct>>>
    
    // 创建订单
    @POST("api/member/orders")
    suspend fun createOrder(
        @Query("product_type") productType: String,
        @Query("payment_method") paymentMethod: String
    ): retrofit2.Response<ApiResponse<Map<String, Any>>>
    
    // 支付订单
    @POST("api/member/pay")
    suspend fun payOrder(
        @Body body: Map<String, String>
    ): retrofit2.Response<ApiResponse<Map<String, Any>>>
    
    // 获取订单列表
    @GET("api/member/orders")
    suspend fun getOrders(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): retrofit2.Response<ApiResponse<List<Any>>>
}

/**
 * 通用服务接口
 */
interface CommonService {
    
    // 获取配置
    @GET("api/common/config")
    suspend fun getConfig(): retrofit2.Response<ApiResponse<SystemConfig>>
    
    // 获取主题列表
    @GET("api/common/themes")
    suspend fun getThemes(): retrofit2.Response<ApiResponse<List<ThemeInfo>>>
    
    // 获取壁纸列表
    @GET("api/common/wallpapers")
    suspend fun getWallpapers(
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): retrofit2.Response<ApiResponse<List<WallpaperInfo>>>
    
    // 上报错误
    @POST("api/common/report-error")
    suspend fun reportError(
        @Body body: Map<String, Any>
    ): retrofit2.Response<ApiResponse<Any>>
    
    // 获取公告
    @GET("api/common/announcements")
    suspend fun getAnnouncements(): retrofit2.Response<ApiResponse<List<Any>>>
}
