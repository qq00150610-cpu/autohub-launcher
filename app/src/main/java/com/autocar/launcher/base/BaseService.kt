package com.autocar.launcher.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ServiceLifecycleDispatcher
import com.autocar.launcher.util.LogUtil

/**
 * 基础 Service 类
 * 所有前台服务的基类，支持 Lifecycle
 * 
 * 功能：
 * - Lifecycle 支持
 * - 前台服务通知
 * - 生命周期日志
 */
abstract class BaseService : Service(), LifecycleOwner {

    companion object {
        private const val TAG = "BaseService"
    }
    
    // Lifecycle 调度器
    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)
    
    // Lifecycle Registry
    private val lifecycleRegistry = LifecycleRegistry(this)
    
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    
    /**
     * 服务创建时调用
     */
    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        LogUtil.d(TAG, "onCreate: ${javaClass.simpleName}")
    }
    
    /**
     * 启动服务
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleDispatcher.onServicePreSuperOnStart()
        LogUtil.d(TAG, "onStartCommand: ${javaClass.simpleName}, startId=$startId")
        return onHandleStartCommand(intent, flags, startId)
    }
    
    /**
     * 处理启动命令
     * 子类实现具体逻辑
     */
    protected abstract fun onHandleStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    
    /**
     * 绑定服务
     */
    override fun onBind(intent: Intent?): IBinder? {
        lifecycleDispatcher.onServicePreSuperOnBind()
        LogUtil.d(TAG, "onBind: ${javaClass.simpleName}")
        return onHandleBind(intent)
    }
    
    /**
     * 处理绑定
     * 子类可重写实现
     */
    protected open fun onHandleBind(intent: Intent?): IBinder? = null
    
    /**
     * 服务销毁
     */
    override fun onDestroy() {
        lifecycleDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy: ${javaClass.simpleName}")
    }
    
    /**
     * 任务完成且没有绑定的客户端时调用
     */
    override fun onLowMemory() {
        super.onLowMemory()
        LogUtil.w(TAG, "onLowMemory: ${javaClass.simpleName}")
    }
    
    /**
     * 系统内存不足时调用
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        LogUtil.d(TAG, "onTrimMemory: ${javaClass.simpleName}, level=$level")
    }
}
