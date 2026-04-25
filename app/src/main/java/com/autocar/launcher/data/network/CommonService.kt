/**
 * 通用服务接口
 */
package com.autocar.launcher.data.network

import com.autocar.launcher.data.model.ApiResponse
import com.autocar.launcher.data.model.SystemConfig
import retrofit2.Call
import retrofit2.http.*

interface CommonService {

    /**
     * 获取天气信息
     */
    @GET("common/weather")
    fun getWeather(
        @Query("city") city: String
    ): Call<ApiResponse<WeatherInfo>>

    /**
     * 检查更新
     */
    @GET("common/update/check")
    fun checkUpdate(
        @Query("version") version: String,
        @Query("platform") platform: String = "android"
    ): Call<ApiResponse<UpdateInfo>>
}

/**
 * 天气信息
 */
data class WeatherInfo(
    val city: String,
    val weather: String,
    val temperature: Int,
    val humidity: Int,
    val windSpeed: String,
    val windDirection: String,
    val aqi: Int,
    val aqiLevel: String,
    val updateTime: String,
    val forecast: List<WeatherForecast>? = null
)

/**
 * 天气预报
 */
data class WeatherForecast(
    val date: String,
    val weather: String,
    val tempDay: Int,
    val tempNight: Int,
    val windDirection: String,
    val windSpeed: String
)

/**
 * 更新信息
 */
data class UpdateInfo(
    val hasUpdate: Boolean,
    val version: String,
    val buildNumber: Int,
    val versionName: String,
    val updateContent: String,
    val forceUpdate: Boolean,
    val downloadUrl: String,
    val fileSize: Long,
    val md5: String?
)
