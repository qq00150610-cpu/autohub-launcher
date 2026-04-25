package com.autocar.launcher.ui.activity

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.databinding.ActivityThemePickerBinding
import com.autocar.launcher.util.LogUtil

/**
 * 主题选择 Activity
 * 提供主题颜色选择功能
 */
class ThemePickerActivity : BaseActivity<ActivityThemePickerBinding>() {

    companion object {
        private const val TAG = "ThemePickerActivity"
    }
    
    override fun createBinding(): ActivityThemePickerBinding {
        return ActivityThemePickerBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        setupToolbar()
        loadThemes()
    }
    
    override fun initData() {
        // 加载当前主题
    }
    
    override fun initListener() {
        binding.toolbar?.setNavigationOnClickListener {
            finish()
        }
    }
    
    /**
     * 设置 Toolbar
     */
    private fun setupToolbar() {
        binding.toolbar?.title = "选择主题"
    }
    
    /**
     * 加载主题列表
     */
    private fun loadThemes() {
        // TODO: 加载预设主题
        LogUtil.d(TAG, "加载主题列表")
    }
    
    /**
     * 选择主题
     */
    fun selectTheme(view: View) {
        val color = view.tag as? Int ?: return
        applyTheme(color)
    }
    
    /**
     * 应用主题
     */
    private fun applyTheme(color: Int) {
        val prefs = com.autocar.launcher.util.PreferencesManager(this)
        prefs.setThemeColor(color)
        LogUtil.d(TAG, "应用主题颜色: $color")
        
        // TODO: 更新界面主题
    }
}
