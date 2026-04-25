package com.autocar.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.session.MediaSessionManager
import android.util.Log
import android.view.KeyEvent
import com.autocar.launcher.service.MediaService
import com.autocar.launcher.util.LogUtil

/**
 * 媒体按钮广播接收器
 * 接收来自车载控制或蓝牙耳机的媒体按键事件
 * 
 * 功能：
 * - 监听播放/暂停按钮
 * - 监听上一首/下一首按钮
 * - 监听停止按钮
 * - 转发到 MediaService 处理
 */
class MediaButtonReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MediaButtonReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MEDIA_BUTTON != intent.action) {
            return
        }
        
        val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        
        if (keyEvent == null) {
            LogUtil.e(TAG, "KeyEvent 为空")
            return
        }
        
        LogUtil.d(TAG, "媒体按键: ${keyEvent.keyCode}, action: ${keyEvent.action}")
        
        // 只处理按键松开事件
        if (keyEvent.action != KeyEvent.ACTION_UP) {
            return
        }
        
        // 处理按键
        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                handlePlay(context)
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                handlePause(context)
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                handlePlayPause(context)
            }
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                handleStop(context)
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                handleNext(context)
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                handlePrevious(context)
            }
            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                handleRewind(context)
            }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                handleFastForward(context)
            }
            else -> {
                LogUtil.d(TAG, "未处理的按键: ${keyEvent.keyCode}")
            }
        }
    }
    
    /**
     * 处理播放
     */
    private fun handlePlay(context: Context) {
        val serviceIntent = Intent(context, MediaService::class.java).apply {
            action = MediaService.ACTION_PLAY
        }
        context.startService(serviceIntent)
        LogUtil.d(TAG, "执行播放")
    }
    
    /**
     * 处理暂停
     */
    private fun handlePause(context: Context) {
        val serviceIntent = Intent(context, MediaService::class.java).apply {
            action = MediaService.ACTION_PAUSE
        }
        context.startService(serviceIntent)
        LogUtil.d(TAG, "执行暂停")
    }
    
    /**
     * 处理播放/暂停切换
     */
    private fun handlePlayPause(context: Context) {
        val serviceIntent = Intent(context, MediaService::class.java).apply {
            action = MediaService.ACTION_PLAY
        }
        context.startService(serviceIntent)
        LogUtil.d(TAG, "执行播放/暂停")
    }
    
    /**
     * 处理停止
     */
    private fun handleStop(context: Context) {
        val serviceIntent = Intent(context, MediaService::class.java).apply {
            action = MediaService.ACTION_STOP
        }
        context.startService(serviceIntent)
        LogUtil.d(TAG, "执行停止")
    }
    
    /**
     * 处理下一首
     */
    private fun handleNext(context: Context) {
        val serviceIntent = Intent(context, MediaService::class.java).apply {
            action = MediaService.ACTION_NEXT
        }
        context.startService(serviceIntent)
        LogUtil.d(TAG, "执行下一首")
    }
    
    /**
     * 处理上一首
     */
    private fun handlePrevious(context: Context) {
        val serviceIntent = Intent(context, MediaService::class.java).apply {
            action = MediaService.ACTION_PREVIOUS
        }
        context.startService(serviceIntent)
        LogUtil.d(TAG, "执行上一首")
    }
    
    /**
     * 处理快退
     */
    private fun handleRewind(context: Context) {
        LogUtil.d(TAG, "执行快退")
        // TODO: 实现快退逻辑
    }
    
    /**
     * 处理快进
     */
    private fun handleFastForward(context: Context) {
        LogUtil.d(TAG, "执行快进")
        // TODO: 实现快进逻辑
    }
}
