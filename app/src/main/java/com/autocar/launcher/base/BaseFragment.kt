package com.autocar.launcher.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 基础 Fragment 类
 * 所有 Fragment 的基类，提供通用功能
 * 
 * 功能：
 * - ViewBinding 初始化
 * - 生命周期日志
 * - 协程作用域管理
 * - 懒加载数据
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    companion object {
        private const val TAG = "BaseFragment"
    }
    
    // ViewBinding 实例（可空）
    private var _binding: VB? = null
    
    // 安全访问 binding
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException("Binding is null")
    
    /**
     * 创建 ViewBinding
     */
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    /**
     * 初始化视图
     */
    protected abstract fun initView()
    
    /**
     * 初始化数据
     */
    protected abstract fun initData()
    
    /**
     * 初始化监听器
     */
    protected abstract fun initListener()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            _binding = createBinding(inflater, container)
            LogUtil.d(TAG, "onCreateView: ${javaClass.simpleName}")
            binding.root
        } catch (e: Exception) {
            LogUtil.e(TAG, "onCreateView 初始化失败", e)
            null
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            super.onViewCreated(view, savedInstanceState)
            LogUtil.d(TAG, "onViewCreated: ${javaClass.simpleName}")
            initView()
            initData()
            initListener()
        } catch (e: Exception) {
            LogUtil.e(TAG, "onViewCreated 初始化失败", e)
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        LogUtil.d(TAG, "onDestroyView: ${javaClass.simpleName}")
        _binding = null
    }
    
    /**
     * 懒加载数据
     * 仅在视图可见时加载
     */
    protected open fun lazyLoad() {
        // 子类可重写实现懒加载
    }
    
    /**
     * 启动协程（Lifecycle STARTED 时自动取消）
     */
    protected fun launchWhenStarted(block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
            }
        }
    }
    
    /**
     * 启动协程（Lifecycle RESUMED 时自动取消）
     */
    protected fun launchWhenResumed(block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                block()
            }
        }
    }
    
    /**
     * 收集 Flow
     */
    protected fun <T> collectFlow(flow: Flow<T>, collector: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect(collector)
            }
        }
    }
}
