package com.autocar.launcher.ui.activity

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.databinding.ActivityWallpaperPickerBinding
import com.autocar.launcher.util.LogUtil
import java.io.File

/**
 * 壁纸选择 Activity
 * 提供壁纸选择和预览功能
 */
class WallpaperPickerActivity : BaseActivity<ActivityWallpaperPickerBinding>() {

    companion object {
        private const val TAG = "WallpaperPickerActivity"
    }
    
    override fun createBinding(): ActivityWallpaperPickerBinding {
        return ActivityWallpaperPickerBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        setupToolbar()
        loadWallpapers()
    }
    
    override fun initData() {
        // 加载已选壁纸
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
        binding.toolbar?.title = "选择壁纸"
    }
    
    /**
     * 加载壁纸列表
     */
    private fun loadWallpapers() {
        // TODO: 从资源或文件加载壁纸列表
        LogUtil.d(TAG, "加载壁纸列表")
    }
    
    /**
     * 预览壁纸
     */
    fun previewWallpaper(view: View) {
        // TODO: 预览选中壁纸
    }
    
    /**
     * 应用壁纸
     */
    fun applyWallpaper(view: View) {
        // TODO: 应用选中壁纸
        LogUtil.d(TAG, "应用壁纸")
    }
    
    /**
     * 从相册选择
     */
    fun pickFromGallery(view: View) {
        // TODO: 打开相册选择
    }
}
