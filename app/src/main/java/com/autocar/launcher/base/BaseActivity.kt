package com.autocar.launcher.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 基础 Activity 类
 * 所有 Activity 的基类，提供通用功能
 * 
 * 功能：
 * - ViewBinding 初始化
 * - 生命周期日志
 * - 协程作用域管理
 * - 权限请求封装
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    companion object {
        private const val TAG = "BaseActivity"
    }
    
    // ViewBinding 实例
    protected lateinit var binding: VB
        private set
    
    /**
     * 创建 ViewBinding
     * 子类实现具体创建逻辑
     */
    protected abstract fun createBinding(): VB
    
    /**
     * 初始化视图
     * 在 onCreate 中调用，用于设置视图
     */
    protected abstract fun initView()
    
    /**
     * 初始化数据
     * 在 onCreate 中调用，用于加载数据
     */
    protected abstract fun initData()
    
    /**
     * 初始化监听器
     * 在 onCreate 中调用，用于设置事件监听
     */
    protected abstract fun initListener()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // 创建 ViewBinding
            binding = createBinding()
            setContentView(binding.root)
            
            // 禁止截屏（可选，保护隐私）
            // window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            
            LogUtil.d(TAG, "onCreate: ${javaClass.simpleName}")
            
            // 初始化
            initView()
            initData()
            initListener()
        } catch (e: Exception) {
            LogUtil.e(TAG, "onCreate 初始化失败", e)
            // 尝试显示错误
            finish()
        }
    }
    
    override fun onStart() {
        super.onStart()
        LogUtil.d(TAG, "onStart: ${javaClass.simpleName}")
    }
    
    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume: ${javaClass.simpleName}")
    }
    
    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause: ${javaClass.simpleName}")
    }
    
    override fun onStop() {
        super.onStop()
        LogUtil.d(TAG, "onStop: ${javaClass.simpleName}")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy: ${javaClass.simpleName}")
    }
    
    /**
     * 启动协程（Lifecycle STARTED 时自动取消）
     */
    protected fun launchWhenStarted(block: suspend () -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
            }
        }
    }
    
    /**
     * 启动协程（Lifecycle RESUMED 时自动取消）
     */
    protected fun launchWhenResumed(block: suspend () -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                block()
            }
        }
    }
    
    /**
     * 收集 Flow（Lifecycle STARTED 时自动开始收集）
     */
    protected fun <T> collectFlow(flow: Flow<T>, collector: suspend (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect(collector)
            }
        }
    }
}
