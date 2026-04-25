/**
 * 主Activity - 完整的凹凸桌面主界面
 */
package com.autocar.launcher.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.autocar.launcher.AutoHubApplication
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.data.model.AppInfo
import com.autocar.launcher.data.network.ApiClient
import com.autocar.launcher.databinding.ActivityMainBinding
import com.autocar.launcher.service.FloatingBallService
import com.autocar.launcher.ui.adapter.AppGridAdapter
import com.autocar.launcher.ui.viewmodel.MainViewModel
import com.autocar.launcher.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar
import android.location.LocationManager

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {
        private const val TAG = "MainActivity"
        private const val DOUBLE_CLICK_INTERVAL = 1500L
        private const val NAVIGATION_ACTIVITY_REQUEST = 1001
        private const val MUSIC_ACTIVITY_REQUEST = 1002
    }

    // ViewModel
    private val viewModel by lazy { MainViewModel() }
    
    // 应用列表
    private val appList = mutableListOf<AppInfo>()
    private lateinit var appGridAdapter: AppGridAdapter
    
    // 上次点击时间（用于双击检测）
    private var lastClickTime = 0L
    
    // 是否处于驾驶模式
    private var isDrivingMode = false
    
    // 当前显示区域
    private var currentArea = Area.NAVIGATION
    
    // 区域枚举
    private enum class Area {
        NAVIGATION, MUSIC, APPS
    }
    
    // 底部Dock应用
    private val dockApps = mutableListOf<AppInfo>()

    // Activity Result
    private val navigationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 导航返回处理
    }
    
    private val musicLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 音乐返回处理
    }

    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 全屏沉浸模式
        hideSystemUI()
        
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // 初始化应用网格
        setupAppGrid()
        
        // 初始化底部Dock
        setupDockBar()
        
        // 初始化顶部信息区
        setupTopBar()
        
        // 初始化中央内容区
        setupCentralArea()
        
        LogUtil.d(TAG, "主界面初始化完成")
    }

    override fun initData() {
        // 加载应用列表
        loadApps()
        
        // 加载用户信息
        loadUserInfo()
        
        // 检查悬浮球状态
        checkFloatingBallStatus()
        
        // 检查是否为默认桌面
        checkHomeStatus()
        
        // 注册广播接收器
        registerReceivers()
    }

    override fun initListener() {
        // 设置按钮
        binding.statusArea?.weatherCard?.setOnClickListener {
            openSettings()
        }
        
        // AI助手按钮
        binding.aiAssistant?.setOnClickListener {
            openAiAssistant()
        }
        
        // 导航Dock点击
        binding.dockBar?.dockNavigation?.root?.setOnClickListener {
            onDockClicked(DockType.NAVIGATION)
        }
        
        // 音乐Dock点击
        binding.dockBar?.dockMusic?.root?.setOnClickListener {
            onDockClicked(DockType.MUSIC)
        }
        
        // 应用Dock点击
        binding.dockBar?.dockStore?.root?.setOnClickListener {
            onDockClicked(DockType.APPS)
        }
        
        // 车辆服务Dock点击
        binding.dockBar?.dockCar?.root?.setOnClickListener {
            openCarServices()
        }
        
        // 会员中心点击 (通过AI助手访问)
        // binding.cardMember?.setOnClickListener {
        //     openMemberCenter()
        // }
        
        // 长按Dock进入编辑模式
        setupDockLongPress()
        
        // 双击主页返回桌面
        setupDoubleClickHome()
    }

    /**
     * 隐藏系统UI
     */
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        }
    }

    /**
     * 设置应用网格
     */
    private fun setupAppGrid() {
        appGridAdapter = AppGridAdapter(this, appList) { app, position ->
            launchApp(app)
        }
        
        binding.contentArea?.appGridLayout?.frequentlyUsedRecyclerView?.adapter = appGridAdapter
        
        // 长按应用图标
        binding.contentArea?.appGridLayout?.frequentlyUsedRecyclerView?.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, view, position, _ ->
            showAppInfo(appList[position])
            true
        }
    }

    /**
     * 设置底部Dock栏
     */
    private fun setupDockBar() {
        // 加载Dock应用
        loadDockApps()
        
        // 设置Dock图标点击效果
        setupDockTouchAnimation()
    }

    /**
     * 设置Dock长按编辑
     */
    private fun setupDockLongPress() {
        listOf(
            binding.dockBar?.dockNavigation?.root,
            binding.dockBar?.dockMusic?.root,
            binding.dockBar?.dockStore?.root,
            binding.dockBar?.dockCar?.root
        ).forEach { view ->
            view?.setOnLongClickListener {
                enterDockEditMode()
                true
            }
        }
    }

    /**
     * 设置双击主页返回桌面
     */
    private fun setupDoubleClickHome() {
        binding.root?.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < DOUBLE_CLICK_INTERVAL) {
                // 双击 - 返回系统桌面
                returnToSystemHome()
            }
            lastClickTime = currentTime
        }
    }

    /**
     * 设置顶部状态栏
     */
    private fun setupTopBar() {
        // 更新时间
        updateTime()
        
        // 更新日期
        updateDate()
        
        // 启动定时器更新状态
        startStatusUpdateTimer()
    }

    /**
     * 设置中央内容区
     */
    private fun setupCentralArea() {
        // 默认显示导航
        showNavigationArea()
    }

    /**
     * 加载应用列表
     */
    private fun loadApps() {
        lifecycleScope.launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                    getInstalledApps()
                }
                appList.clear()
                appList.addAll(apps.filter { !it.isSystemApp })
                appGridAdapter.notifyDataSetChanged()
                
                LogUtil.d(TAG, "加载了 ${appList.size} 个应用")
            } catch (e: Exception) {
                LogUtil.e(TAG, "加载应用失败", e)
            }
        }
    }

    /**
     * 获取已安装应用
     */
    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        return pm.queryIntentActivities(intent, 0).mapNotNull { resolveInfo ->
            try {
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(pm).toString()
                val icon = resolveInfo.loadIcon(pm)
                val isSystemApp = isSystemPackage(packageName)
                
                AppInfo(
                    packageName = packageName,
                    appName = appName,
                    icon = icon,
                    isSystemApp = isSystemApp
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName }
    }

    /**
     * 判断是否为系统应用
     */
    private fun isSystemPackage(packageName: String): Boolean {
        val systemPackages = listOf(
            "com.android.",
            "com.google.android.",
            "com.autocar.launcher"
        )
        return systemPackages.any { packageName.startsWith(it) }
    }

    /**
     * 加载Dock应用
     */
    private fun loadDockApps() {
        // 默认Dock应用
        dockApps.clear()
        
        // 添加默认快捷方式
        // 这里可以从数据库或SharedPreferences加载用户自定义的Dock应用
    }

    /**
     * 加载用户信息
     */
    private fun loadUserInfo() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.userService.getProfile()
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    // 更新用户名显示 (通过AI助手)
                    // binding.aiAssistant?.userName = data?.nickname ?: "未登录"
                    
                    // 更新会员状态 (通过AI助手)
                    // if ((data?.memberLevel ?: 0) > 0) {
                    //     showMemberBadge()
                    // }
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "加载用户信息失败", e)
            }
        }
    }

    /**
     * 检查悬浮球状态
     */
    private fun checkFloatingBallStatus() {
        if (Settings.canDrawOverlays(this)) {
            startFloatingBallService()
        } else {
            // 请求悬浮窗权限
            requestOverlayPermission()
        }
    }

    /**
     * 检查桌面状态
     */
    private fun checkHomeStatus() {
        val isHome = HomeHelper.isCurrentHomeLauncher(this)
        LogUtil.d(TAG, "是否为默认桌面: $isHome")
        
        if (!isHome) {
            showSetAsHomeDialog()
        }
    }

    /**
     * 显示设为默认桌面提示
     */
    private fun showSetAsHomeDialog() {
        AlertDialog.Builder(this)
            .setTitle("设为默认桌面")
            .setMessage("凹凸桌面尚未设为默认桌面，是否现在设置？")
            .setPositiveButton("设置") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    LogUtil.e(TAG, "打开设置失败", e)
                }
            }
            .setNegativeButton("稍后", null)
            .show()
    }

    /**
     * 启动悬浮球服务
     */
    private fun startFloatingBallService() {
        val intent = Intent(this, FloatingBallService::class.java).apply {
            action = FloatingBallService.ACTION_SHOW
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    /**
     * 请求悬浮窗权限
     */
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("需要权限")
                .setMessage("悬浮球功能需要您授权\"显示在其他应用上层\"权限，是否授权？")
                .setPositiveButton("去授权") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    /**
     * 注册广播接收器
     */
    private fun registerReceivers() {
        // 屏幕旋转
        registerReceiver(screenRotateReceiver, IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED))
        
        // 网络变化
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        
        // 包变化
        registerReceiver(packageChangeReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        })
    }

    /**
     * 卸载广播接收器
     */
    private fun unregisterReceivers() {
        try {
            unregisterReceiver(screenRotateReceiver)
            unregisterReceiver(networkChangeReceiver)
            unregisterReceiver(packageChangeReceiver)
        } catch (e: Exception) {
            LogUtil.e(TAG, "注销广播失败", e)
        }
    }

    // 广播接收器
    private val screenRotateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                handleOrientationChange()
            }
        }
    }
    
    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateNetworkStatus()
        }
    }
    
    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    LogUtil.d(TAG, "应用安装: $packageName")
                    loadApps()
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    LogUtil.d(TAG, "应用卸载: $packageName")
                    loadApps()
                }
            }
        }
    }

    /**
     * 处理屏幕旋转
     */
    private fun handleOrientationChange() {
        val orientation = resources.configuration.orientation
        LogUtil.d(TAG, "屏幕方向改变: $orientation")
        
        // 横屏
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
        }
    }

    /**
     * 更新网络状态
     */
    private fun updateNetworkStatus() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        val isConnected = capabilities != null
        binding.statusArea?.wifiIcon?.setImageResource(
            if (isConnected) R.drawable.ic_wifi else R.drawable.ic_signal
        )
    }

    /**
     * 更新顶部时间
     */
    private fun updateTime() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val weekDay = getWeekDay(calendar.get(Calendar.DAY_OF_WEEK))
                binding.statusArea?.dateText?.text = String.format("%02d:%02d %02d月%02d日 %s", hour, minute, month, day, weekDay)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    /**
     * 更新日期
     */
    private fun updateDate() {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val weekDay = getWeekDay(calendar.get(Calendar.DAY_OF_WEEK))
        binding.statusArea?.dateText?.text = "${month}月${day}日 $weekDay"
    }

    /**
     * 获取星期几
     */
    private fun getWeekDay(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "周日"
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            else -> ""
        }
    }

    /**
     * 启动状态更新定时器
     */
    private fun startStatusUpdateTimer() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                updateStatus()
                handler.postDelayed(this, 30000) // 每30秒更新一次
            }
        }
        handler.post(runnable)
    }

    /**
     * 更新状态信息
     */
    private fun updateStatus() {
        // 更新内存使用
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val usedMemory = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)
        val totalMemory = memoryInfo.totalMem / (1024 * 1024)
        
        // 更新GPS状态
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        
        // 更新蓝牙状态
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
        val isBluetoothEnabled = bluetoothManager?.adapter?.isEnabled == true
        
        LogUtil.d(TAG, "内存: ${usedMemory}MB/${totalMemory}MB, GPS: $isGpsEnabled, 蓝牙: $isBluetoothEnabled")
    }

    /**
     * Dock类型枚举
     */
    private enum class DockType {
        NAVIGATION, MUSIC, APPS, CAR_SERVICES
    }

    /**
     * Dock点击处理
     */
    private fun onDockClicked(type: DockType) {
        when (type) {
            DockType.NAVIGATION -> showNavigationArea()
            DockType.MUSIC -> showMusicArea()
            DockType.APPS -> showAppsArea()
            DockType.CAR_SERVICES -> openCarServices()
        }
    }

    /**
     * 显示导航区域
     */
    private fun showNavigationArea() {
        currentArea = Area.NAVIGATION
        animateAreaTransition { 
            binding.contentArea?.navigationContainer?.visibility = View.VISIBLE
            binding.contentArea?.musicContainer?.visibility = View.GONE
            binding.contentArea?.appContainer?.visibility = View.GONE
        }
        updateDockSelection(DockType.NAVIGATION)
    }

    /**
     * 显示音乐区域
     */
    private fun showMusicArea() {
        currentArea = Area.MUSIC
        animateAreaTransition {
            binding.contentArea?.navigationContainer?.visibility = View.GONE
            binding.contentArea?.musicContainer?.visibility = View.VISIBLE
            binding.contentArea?.appContainer?.visibility = View.GONE
        }
        updateDockSelection(DockType.MUSIC)
    }

    /**
     * 显示应用区域
     */
    private fun showAppsArea() {
        currentArea = Area.APPS
        animateAreaTransition {
            binding.contentArea?.navigationContainer?.visibility = View.GONE
            binding.contentArea?.musicContainer?.visibility = View.GONE
            binding.contentArea?.appContainer?.visibility = View.VISIBLE
        }
        updateDockSelection(DockType.APPS)
    }

    /**
     * 区域切换动画
     */
    private fun animateAreaTransition(transition: () -> Unit) {
        val contentView = binding.root?.findViewById<FrameLayout>(R.id.main_content_layer)
        contentView?.let {
            val fadeOut = ObjectAnimator.ofFloat(it, "alpha", 1f, 0f)
            val fadeIn = ObjectAnimator.ofFloat(it, "alpha", 0f, 1f)
            
            fadeOut.duration = 150
            fadeIn.duration = 150
            
            fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    transition()
                    fadeIn.start()
                }
            })
            
            fadeOut.start()
        } ?: transition()
    }

    /**
     * 更新Dock选中状态
     */
    private fun updateDockSelection(selected: DockType) {
        val views = mapOf(
            DockType.NAVIGATION to binding.dockBar?.dockNavigation?.root,
            DockType.MUSIC to binding.dockBar?.dockMusic?.root,
            DockType.APPS to binding.dockBar?.dockStore?.root,
            DockType.CAR_SERVICES to binding.dockBar?.dockCar?.root
        )
        
        views.forEach { (type, view) ->
            view?.isSelected = type == selected
        }
    }

    /**
     * 设置Dock触摸动画
     */
    private fun setupDockTouchAnimation() {
        listOf(
            binding.dockBar?.dockNavigation?.root,
            binding.dockBar?.dockMusic?.root,
            binding.dockBar?.dockStore?.root,
            binding.dockBar?.dockCar?.root
        ).forEach { view ->
            view?.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        animateScale(v, 0.9f, 100)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        animateScale(v, 1f, 100)
                    }
                }
                false
            }
        }
    }

    /**
     * 缩放动画
     */
    private fun animateScale(view: View, scale: Float, duration: Long) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", scale)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", scale)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    /**
     * 进入Dock编辑模式
     */
    private fun enterDockEditMode() {
        Toast.makeText(this, "进入编辑模式，请拖动应用图标进行排序", Toast.LENGTH_SHORT).show()
        // TODO: 实现Dock编辑功能
    }

    /**
     * 启动应用
     */
    private fun launchApp(app: AppInfo) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                startActivity(intent)
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "启动应用失败: ${app.packageName}", e)
            Toast.makeText(this, "无法启动应用", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 显示应用信息
     */
    private fun showAppInfo(app: AppInfo) {
        val intent = Intent(this, AppInfoActivity::class.java).apply {
            putExtra("packageName", app.packageName)
        }
        startActivity(intent)
    }

    /**
     * 打开设置
     */
    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    /**
     * 打开AI助手
     */
    private fun openAiAssistant() {
        // TODO: 打开AI助手
        Toast.makeText(this, "AI助手功能开发中", Toast.LENGTH_SHORT).show()
    }

    /**
     * 打开车主服务
     */
    private fun openCarServices() {
        val intent = Intent(this, CarServicesActivity::class.java)
        startActivity(intent)
    }

    /**
     * 打开会员中心
     */
    private fun openMemberCenter() {
        if (!PreferencesManager.getInstance().isLoggedIn()) {
            // 未登录，跳转到登录页面
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // 已登录，打开会员中心
            startActivity(Intent(this, MemberCenterActivity::class.java))
        }
    }

    /**
     * 返回系统桌面
     */
    private fun returnToSystemHome() {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            LogUtil.e(TAG, "返回桌面失败", e)
        }
    }

    /**
     * 切换驾驶模式
     */
    fun toggleDrivingMode() {
        isDrivingMode = !isDrivingMode
        
        if (isDrivingMode) {
            // 进入驾驶模式
            enterDrivingMode()
        } else {
            // 退出驾驶模式
            exitDrivingMode()
        }
    }

    /**
     * 进入驾驶模式
     */
    private fun enterDrivingMode() {
        // 隐藏Dock栏
        binding.dockBar?.visibility = View.GONE
        
        // 简化顶部状态栏
        binding.topBar?.alpha = 0.5f
        
        // 隐藏悬浮导航
        // 悬浮球保持显示
        
        Toast.makeText(this, "已进入驾驶模式", Toast.LENGTH_SHORT).show()
    }

    /**
     * 退出驾驶模式
     */
    private fun exitDrivingMode() {
        // 显示Dock栏
        binding.dockBar?.visibility = View.VISIBLE
        
        // 恢复顶部状态栏
        binding.topBar?.alpha = 1f
        
        Toast.makeText(this, "已退出驾驶模式", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        updateStatus()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        // 桌面应用禁止返回键退出
        moveTaskToBack(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
    }
}
