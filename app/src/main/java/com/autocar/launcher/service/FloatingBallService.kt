package com.autocar.launcher.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.autocar.launcher.AutoHubApplication
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseService
import com.autocar.launcher.ui.activity.MainActivity
import com.autocar.launcher.util.LogUtil

/**
 * 悬浮球服务
 * 在屏幕上显示可拖动的悬浮球，提供快捷操作
 * 
 * 功能：
 * - 显示悬浮球图标
 * - 拖动定位
 * - 点击展开菜单
 * - 自动隐藏/显示
 */
class FloatingBallService : BaseService() {

    companion object {
        private const val TAG = "FloatingBallService"
        private const val NOTIFICATION_ID = 1002
        
        const val ACTION_SHOW = "com.autocar.launcher.action.SHOW_FLOATING_BALL"
        const val ACTION_HIDE = "com.autocar.launcher.action.HIDE_FLOATING_BALL"
        const val ACTION_TOGGLE = "com.autocar.launcher.action.TOGGLE_FLOATING_BALL"
        
        // 悬浮球初始位置
        private const val INITIAL_X = 100
        private const val INITIAL_Y = 300
        
        // 触摸灵敏度
        private const val CLICK_THRESHOLD = 10f
    }
    
    // WindowManager
    private lateinit var windowManager: WindowManager
    
    // 悬浮球视图
    private var floatingBallView: View? = null
    
    // 布局参数
    private lateinit var layoutParams: WindowManager.LayoutParams
    
    // 是否可见
    @Volatile
    private var isVisible = false
    
    // 触摸相关
    private val handler = Handler(Looper.getMainLooper())
    private var isDragging = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var touchStartX = 0f
    private var touchStartY = 0f
    
    // 更新 Runnable
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateFloatingBall()
            handler.postDelayed(this, 1000)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        LogUtil.d(TAG, "悬浮球服务已创建")
    }
    
    override fun onHandleStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showFloatingBall()
            ACTION_HIDE -> hideFloatingBall()
            ACTION_TOGGLE -> toggleFloatingBall()
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
                showFloatingBall()
            }
        }
        return START_STICKY
    }
    
    override fun onHandleBind(intent: Intent?): android.os.IBinder? = null
    
    /**
     * 显示悬浮球
     */
    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun showFloatingBall() {
        if (isVisible) return
        
        try {
            // 创建悬浮球视图
            floatingBallView = LayoutInflater.from(this).inflate(R.layout.view_floating_ball, null)
            
            // 设置布局参数
            layoutParams = WindowManager.LayoutParams().apply {
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                format = PixelFormat.RGBA_8888
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                gravity = Gravity.START or Gravity.TOP
                x = INITIAL_X
                y = INITIAL_Y
            }
            
            // 设置触摸监听
            floatingBallView?.setOnTouchListener { _, event ->
                handleTouch(event)
            }
            
            // 设置点击监听
            floatingBallView?.setOnClickListener {
                onFloatingBallClicked()
            }
            
            // 添加到窗口
            windowManager.addView(floatingBallView, layoutParams)
            
            isVisible = true
            LogUtil.d(TAG, "悬浮球已显示")
            
        } catch (e: Exception) {
            LogUtil.e(TAG, "显示悬浮球失败", e)
        }
    }
    
    /**
     * 隐藏悬浮球
     */
    private fun hideFloatingBall() {
        if (!isVisible) return
        
        try {
            floatingBallView?.let {
                windowManager.removeView(it)
            }
            floatingBallView = null
            isVisible = false
            LogUtil.d(TAG, "悬浮球已隐藏")
        } catch (e: Exception) {
            LogUtil.e(TAG, "隐藏悬浮球失败", e)
        }
    }
    
    /**
     * 切换悬浮球显示状态
     */
    private fun toggleFloatingBall() {
        if (isVisible) {
            hideFloatingBall()
        } else {
            showFloatingBall()
        }
    }
    
    /**
     * 处理触摸事件
     */
    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = false
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                touchStartX = event.rawX
                touchStartY = event.rawY
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - lastTouchX
                val deltaY = event.rawY - lastTouchY
                
                // 判断是否移动超过阈值
                if (!isDragging) {
                    val moveDistance = Math.sqrt(
                        ((event.rawX - touchStartX) * (event.rawX - touchStartX) +
                        (event.rawY - touchStartY) * (event.rawY - touchStartY)).toDouble()
                    )
                    isDragging = moveDistance > CLICK_THRESHOLD
                }
                
                if (isDragging) {
                    layoutParams.x += deltaX.toInt()
                    layoutParams.y += deltaY.toInt()
                    windowManager.updateViewLayout(floatingBallView, layoutParams)
                    
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                }
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    // 点击事件
                    onFloatingBallClicked()
                }
                return true
            }
        }
        return false
    }
    
    /**
     * 悬浮球点击处理
     */
    private fun onFloatingBallClicked() {
        LogUtil.d(TAG, "悬浮球被点击")
        // 打开主界面
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }
    
    /**
     * 更新悬浮球状态
     */
    private fun updateFloatingBall() {
        // TODO: 更新悬浮球显示内容（如时间、电量等）
    }
    
    /**
     * 创建前台通知
     */
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val hideIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FloatingBallService::class.java).apply { action = ACTION_HIDE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(this, AutoHubApplication.CHANNEL_FLOATING)
                .setContentTitle("悬浮球服务")
                .setContentText("点击返回桌面")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_hide, "隐藏", hideIntent)
                .setOngoing(true)
                .setPriority(android.app.Notification.PRIORITY_LOW)
                .build()
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(this)
                .setContentTitle("悬浮球服务")
                .setContentText("点击返回桌面")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_hide, "隐藏", hideIntent)
                .setOngoing(true)
                .setPriority(android.app.Notification.PRIORITY_LOW)
                .build()
        }
    }
    
    override fun onDestroy() {
        hideFloatingBall()
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
        LogUtil.d(TAG, "悬浮球服务已销毁")
    }
}
