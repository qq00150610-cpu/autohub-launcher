package com.autocar.launcher.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 基础 ViewModel 类
 * 提供 ViewModel 通用功能
 * 
 * 功能：
 * - Lifecycle 支持
 * - 协程作用域管理
 * - 状态保存
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 启动协程（ViewModel scope）
     */
    protected fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}

/**
 * 简单 ViewModel（无 Application）
 */
open class SimpleViewModel : ViewModel() {
    
    /**
     * 启动协程（ViewModel scope）
     */
    protected fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}
