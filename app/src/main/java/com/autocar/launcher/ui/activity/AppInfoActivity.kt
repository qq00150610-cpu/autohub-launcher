package com.autocar.launcher.ui.activity

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.databinding.ActivityAppInfoBinding
import com.autocar.launcher.util.LogUtil

/**
 * 应用详情 Activity
 * 显示应用信息和快捷操作
 */
class AppInfoActivity : BaseActivity<ActivityAppInfoBinding>() {

    companion object {
        private const val TAG = "AppInfoActivity"
        
        const val EXTRA_PACKAGE_NAME = "package_name"
    }
    
    private var packageName: String? = null
    
    override fun createBinding(): ActivityAppInfoBinding {
        return ActivityAppInfoBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        
        if (packageName.isNullOrEmpty()) {
            finish()
            return
        }
        
        setupToolbar()
        loadAppInfo()
    }
    
    override fun initData() {
        // 加载应用数据
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
        binding.toolbar?.title = "应用信息"
    }
    
    /**
     * 加载应用信息
     */
    private fun loadAppInfo() {
        packageName?.let { pkg ->
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val appIcon = packageManager.getApplicationIcon(pkg)
                
                binding.tvAppName?.text = appName
                binding.ivAppIcon?.setImageDrawable(appIcon)
                binding.tvPackageName?.text = pkg
                
                LogUtil.d(TAG, "加载应用信息: $appName")
            } catch (e: Exception) {
                LogUtil.e(TAG, "加载应用信息失败", e)
                finish()
            }
        }
    }
    
    /**
     * 打开应用
     */
    fun openApp() {
        packageName?.let {
            com.autocar.launcher.util.HomeHelper.openApp(this, it)
        }
    }
    
    /**
     * 卸载应用
     */
    fun uninstallApp() {
        packageName?.let {
            val intent = android.content.Intent(android.content.Intent.ACTION_DELETE).apply {
                data = android.net.Uri.parse("package:$it")
            }
            startActivity(intent)
        }
    }
    
    /**
     * 应用信息
     */
    fun openAppSettings() {
        packageName?.let {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$it")
            }
            startActivity(intent)
        }
    }
}
