/**
 * 数据模型
 */
package com.autocar.launcher.data.model

import android.graphics.drawable.Drawable

/**
 * 应用信息
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    var position: Int = 0
)

/**
 * 用户信息
 */
data class UserProfile(
    val id: Long = 0,
    val phone: String? = null,
    val email: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val gender: Int = 0,
    val birthday: String? = null,
    val bio: String? = null,
    val memberLevel: Int = 0,
    val memberExpireAt: String? = null,
    val totalStorage: Long = 0,
    val usedStorage: Long = 0,
    val lastLoginAt: String? = null,
    val loginCount: Int = 0,
    val createdAt: String? = null,
    val hasPassword: Boolean = false,
    val hasPhone: Boolean = false,
    val hasEmail: Boolean = false,
    val hasWechat: Boolean = false
)

/**
 * 登录请求
 */
data class LoginRequest(
    val phone: String? = null,
    val email: String? = null,
    val code: String? = null,
    val account: String? = null,
    val password: String? = null
)

/**
 * 注册请求
 */
data class RegisterRequest(
    val phone: String? = null,
    val email: String? = null,
    val code: String? = null,
    val password: String? = null
)

/**
 * 认证响应
 */
data class AuthResponse(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: String,
    val refreshExpiresIn: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val memberLevel: Int = 0,
    val memberExpireAt: String? = null,
    val hasPassword: Boolean = false,
    val hasPhone: Boolean = false,
    val hasEmail: Boolean = false
)

/**
 * 会员信息
 */
data class MemberInfo(
    val memberLevel: Int = 0,
    val memberName: String? = null,
    val memberExpireAt: String? = null,
    val isExpired: Boolean = true,
    val remainingDays: Int = 0,
    val totalStorage: Long = 0,
    val usedStorage: Long = 0,
    val storagePercent: Int = 0
)

/**
 * 会员产品
 */
data class MemberProduct(
    val type: String,
    val name: String,
    val price: Double,
    val duration: Int,
    val benefits: List<String> = emptyList()
)

/**
 * 会员权益
 */
data class MemberBenefit(
    val level: Int,
    val name: String,
    val benefits: List<BenefitItem> = emptyList()
)

/**
 * 权益项
 */
data class BenefitItem(
    val key: String,
    val name: String,
    val description: String? = null
)

/**
 * 订单信息
 */
data class OrderResponse(
    val orderNo: String,
    val amount: Double,
    val productType: String? = null,
    val paymentMethod: String? = null,
    val status: String? = null,
    val createdAt: String? = null
)

/**
 * API统一响应
 */
data class ApiResponse<T>(
    val code: Int,
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val timestamp: String? = null,
    val pagination: Pagination? = null
)

/**
 * 分页信息
 */
data class Pagination(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
)

/**
 * 主题信息
 */
data class ThemeInfo(
    val id: String,
    val name: String,
    val type: String,
    val preview: String? = null,
    val description: String? = null,
    val colors: ThemeColors? = null
)

/**
 * 主题颜色
 */
data class ThemeColors(
    val primary: String,
    val accent: String,
    val background: String,
    val surface: String,
    val text: String
)

/**
 * 壁纸信息
 */
data class WallpaperInfo(
    val id: String,
    val name: String,
    val category: String,
    val thumbnail: String,
    val url: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val isVip: Boolean = false
)

/**
 * 系统配置
 */
data class SystemConfig(
    val version: String,
    val buildNumber: Int,
    val minVersion: String,
    val updateUrl: String,
    val privacyUrl: String,
    val termsUrl: String,
    val supportEmail: String,
    val website: String,
    val appConfig: AppConfig
)

/**
 * App配置
 */
data class AppConfig(
    val maxAppGridCount: Int,
    val maxDockAppCount: Int,
    val maxPlugins: Int,
    val defaultTheme: String,
    val autoStartEnabled: Boolean,
    val floatingBallEnabled: Boolean,
    val drivingModeEnabled: Boolean
)
