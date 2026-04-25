/**
 * 导航Activity
 */
package com.autocar.launcher.ui.activity

import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.databinding.ActivityNavigationBinding
import com.autocar.launcher.util.LogUtil

class NavigationActivity : BaseActivity<ActivityNavigationBinding>() {

    companion object {
        private const val TAG = "NavigationActivity"
    }

    // 常用目的地
    private val favoriteDestinations = mutableListOf<Destination>()
    
    // 最近目的地
    private val recentDestinations = mutableListOf<Destination>()

    data class Destination(
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double
    )

    override fun createBinding(): ActivityNavigationBinding {
        return ActivityNavigationBinding.inflate(layoutInflater)
    }

    override fun initView() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // 初始化默认目的地
        initDefaultDestinations()
        
        // 加载目的地
        loadDestinations()
    }

    override fun initData() {
        // 检查已安装的导航应用
        checkInstalledNavigationApps()
    }

    override fun initListener() {
        // 返回
        binding.btnBack?.setOnClickListener {
            finish()
        }
        
        // 搜索框
        binding.etSearch?.setOnClickListener {
            showSearchDialog()
        }
        
        // 主页
        binding.btnGoHome?.setOnClickListener {
            navigateToHome()
        }
        
        // 公司
        binding.btnGoCompany?.setOnClickListener {
            navigateToCompany()
        }
        
        // 高德地图
        binding.cardGaode?.setOnClickListener {
            openNavigationApp("com.autonavi.cdbsglife", "amap://")
        }
        
        // 百度地图
        binding.cardBaidu?.setOnClickListener {
            openNavigationApp("com.baidu.BaiduMap", "baidumap://")
        }
        
        // 腾讯地图
        binding.cardTencent?.setOnClickListener {
            openNavigationApp("com.tencent.map", "qqmap://")
        }
        
        // 浮窗导航
        binding.btnFloatNav?.setOnClickListener {
            enterFloatNavigation()
        }
        
        // 常用目的地点击
        binding.layoutFavorites?.setOnClickListener {
            showFavoritesDialog()
        }
        
        // 历史记录点击
        binding.layoutHistory?.setOnClickListener {
            showHistoryDialog()
        }
    }

    private fun initDefaultDestinations() {
        // 添加默认目的地
        favoriteDestinations.add(
            Destination("家", "北京市朝阳区", 39.9288, 116.4274)
        )
        favoriteDestinations.add(
            Destination("公司", "北京市海淀区", 39.9835, 116.3174)
        )
    }

    private fun loadDestinations() {
        // 从数据库或偏好设置加载目的地
    }

    private fun checkInstalledNavigationApps() {
        val navigationPackages = listOf(
            "com.autonavi.cdbsglife" to "高德地图",
            "com.baidu.BaiduMap" to "百度地图",
            "com.tencent.map" to "腾讯地图",
            "com.sogou.map" to "搜狗地图"
        )
        
        navigationPackages.forEach { (packageName, appName) ->
            val isInstalled = isPackageInstalled(packageName)
            updateAppStatus(packageName, isInstalled)
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun updateAppStatus(packageName: String, installed: Boolean) {
        val card = when (packageName) {
            "com.autonavi.cdbsglife" -> binding.cardGaode
            "com.baidu.BaiduMap" -> binding.cardBaidu
            "com.tencent.map" -> binding.cardTencent
            else -> null
        }
        
        card?.alpha = if (installed) 1f else 0.5f
        card?.isEnabled = installed
    }

    private fun showSearchDialog() {
        // TODO: 实现搜索功能
        Toast.makeText(this, "搜索功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        if (favoriteDestinations.isNotEmpty()) {
            val home = favoriteDestinations.first { it.name == "家" }
            startNavigation(home)
        } else {
            Toast.makeText(this, "请先设置家的位置", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToCompany() {
        if (favoriteDestinations.isNotEmpty()) {
            val company = favoriteDestinations.first { it.name == "公司" }
            startNavigation(company)
        } else {
            Toast.makeText(this, "请先设置公司位置", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startNavigation(destination: Destination) {
        // 优先使用高德地图
        if (isPackageInstalled("com.autonavi.cdbsglife")) {
            openGaodeNavigation(destination)
        } else if (isPackageInstalled("com.baidu.BaiduMap")) {
            openBaiduNavigation(destination)
        } else {
            openWebNavigation(destination)
        }
    }

    private fun openNavigationApp(packageName: String, scheme: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
            } else {
                // 打开应用市场
                openAppStore(packageName)
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "打开导航应用失败", e)
            openAppStore(packageName)
        }
    }

    private fun openGaodeNavigation(destination: Destination) {
        try {
            val uri = Uri.parse(
                "amap://navi?sourceApplication=autohub&lat=${destination.latitude}&lon=${destination.longitude}&dev=1"
            )
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.autonavi.cdbsglife")
            }
            startActivity(intent)
        } catch (e: Exception) {
            LogUtil.e(TAG, "打开高德导航失败", e)
            openWebNavigation(destination)
        }
    }

    private fun openBaiduNavigation(destination: Destination) {
        try {
            // 百度地图坐标需要转换为BD09
            val (lat, lon) = transformToBaiduCoord(destination.latitude, destination.longitude)
            
            val uri = Uri.parse(
                "baidumap://map/navi?location=${lat},${lon}&src=autohub"
            )
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.baidu.BaiduMap")
            }
            startActivity(intent)
        } catch (e: Exception) {
            LogUtil.e(TAG, "打开百度导航失败", e)
            openWebNavigation(destination)
        }
    }

    private fun openWebNavigation(destination: Destination) {
        try {
            val url = "https://uri.amap.com/navigation?to=${destination.longitude},${destination.latitude},${destination.name}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            LogUtil.e(TAG, "打开网页导航失败", e)
            Toast.makeText(this, "无法打开导航", Toast.LENGTH_SHORT).show()
        }
    }

    private fun transformToBaiduCoord(lat: Double, lon: Double): Pair<Double, Double> {
        // WGS84 转 BD09 的简化转换
        // 实际应用中应使用专业的坐标转换库
        return Pair(lat + 0.006, lon + 0.006)
    }

    private fun openAppStore(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "请安装导航应用", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enterFloatNavigation() {
        // 发送广播进入浮窗导航模式
        val intent = Intent("com.autocar.launcher.action.ENTER_FLOAT_NAVIGATION")
        sendBroadcast(intent)
        
        Toast.makeText(this, "已进入浮窗导航模式", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showFavoritesDialog() {
        val items = favoriteDestinations.map { "${it.name}\n${it.address}" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("常用目的地")
            .setItems(items) { _, which ->
                startNavigation(favoriteDestinations[which])
            }
            .setPositiveButton("管理") { _, _ ->
                // TODO: 打开管理页面
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showHistoryDialog() {
        if (recentDestinations.isEmpty()) {
            Toast.makeText(this, "暂无历史记录", Toast.LENGTH_SHORT).show()
            return
        }
        
        val items = recentDestinations.map { "${it.name}\n${it.address}" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("历史记录")
            .setItems(items) { _, which ->
                startNavigation(recentDestinations[which])
            }
            .setNegativeButton("关闭", null)
            .show()
    }
}
