package com.autocar.launcher.util

import android.util.Log
import com.autocar.launcher.BuildConfig
import timber.log.Timber

/**
 * 日志工具类
 * 封装日志操作，支持分级输出
 * 
 * 使用 Timber 库实现日志功能
 */
object LogUtil {

    // 是否启用日志（Debug 构建启用，Release 构建禁用）
    private var enableLog = true
    
    /**
     * 初始化日志系统
     */
    fun init() {
        try {
            // 初始化 Timber
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            } else {
                Timber.plant(ReleaseTree())
            }
            enableLog = true
        } catch (e: Exception) {
            // Timber 初始化失败时，使用 Android 默认日志
            enableLog = false
            android.util.Log.e("LogUtil", "Timber 初始化失败，使用默认日志", e)
        }
    }
    
    /**
     * 详细日志
     */
    fun v(tag: String, message: String) {
        if (enableLog) {
            Timber.tag(tag).v(message)
        }
    }
    
    /**
     * 调试日志
     */
    fun d(tag: String, message: String) {
        if (enableLog) {
            Timber.tag(tag).d(message)
        }
    }
    
    /**
     * 信息日志
     */
    fun i(tag: String, message: String) {
        if (enableLog) {
            Timber.tag(tag).i(message)
        }
    }
    
    /**
     * 警告日志
     */
    fun w(tag: String, message: String) {
        if (enableLog) {
            Timber.tag(tag).w(message)
        }
    }
    
    /**
     * 错误日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (enableLog) {
            if (throwable != null) {
                Timber.tag(tag).e(throwable, message)
            } else {
                Timber.tag(tag).e(message)
            }
        }
    }
    
    /**
     * Release 构建使用的 Tree
     * 可以在这里添加日志上传逻辑
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Release 版本可以在这里实现日志上传
            // 例如：上传到 Bugly、Firebase Crashlytics 等
            
            // 这里简单打印，不上传
            when (priority) {
                Log.ERROR, Log.ASSERT -> {
                    // 上报严重错误
                    println("[$tag] ERROR: $message")
                    t?.printStackTrace()
                }
                Log.WARN -> {
                    // 记录警告
                    println("[$tag] WARN: $message")
                }
            }
        }
    }
}
