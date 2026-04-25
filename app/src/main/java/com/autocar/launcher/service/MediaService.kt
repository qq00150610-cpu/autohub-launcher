package com.autocar.launcher.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.autocar.launcher.AutoHubApplication
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseService
import com.autocar.launcher.ui.activity.MusicPlayerActivity
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 媒体播放服务
 * 控制音乐和视频的播放，提供媒体控制功能
 */
class MediaService : BaseService() {

    companion object {
        private const val TAG = "MediaService"
        private const val NOTIFICATION_ID = 1003
        
        // Action
        const val ACTION_PLAY = "com.autocar.launcher.action.PLAY"
        const val ACTION_PAUSE = "com.autocar.launcher.action.PAUSE"
        const val ACTION_STOP = "com.autocar.launcher.action.STOP"
        const val ACTION_NEXT = "com.autocar.launcher.action.NEXT"
        const val ACTION_PREVIOUS = "com.autocar.launcher.action.PREVIOUS"
        const val ACTION_SEEK_TO = "com.autocar.launcher.action.SEEK_TO"
        const val ACTION_ENTER_FLOAT_MODE = "com.autocar.launcher.action.ENTER_FLOAT_MODE"
        const val ACTION_UPDATE_PROGRESS = "com.autocar.launcher.action.UPDATE_PROGRESS"
        
        // Extra Keys
        const val EXTRA_POSITION = "extra_position"
        
        // MediaSession Tag
        private const val MEDIA_SESSION_TAG = "AutoHubMediaSession"
    }
    
    // MediaPlayer
    private var mediaPlayer: MediaPlayer? = null
    
    // MediaSession
    private lateinit var mediaSession: MediaSession
    
    // WakeLock
    private lateinit var wakeLock: PowerManager.WakeLock
    
    // Handler
    private val handler = Handler(Looper.getMainLooper())
    
    // 播放状态
    private val _playbackState = MutableStateFlow(PlaybackState.Builder().build())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    // 当前播放信息
    private var currentTitle = ""
    private var currentArtist = ""
    private var currentAlbumArt: Bitmap? = null
    
    // 播放列表
    private val playQueue = mutableListOf<String>()
    private var currentIndex = 0
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 MediaSession
        mediaSession = MediaSession(this, MEDIA_SESSION_TAG).apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }
        
        // 获取 WakeLock
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AutoHubLauncher::MediaWakeLock"
        )
        
        LogUtil.d(TAG, "媒体服务已创建")
    }
    
    override fun onHandleStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
        }
        return START_STICKY
    }
    
    override fun onHandleBind(intent: Intent?): android.os.IBinder? = null
    
    private fun play() {
        try {
            if (mediaPlayer == null) {
                prepareMediaPlayer()
            }
            
            mediaPlayer?.start()
            updatePlaybackState(PlaybackState.STATE_PLAYING)
            startForeground(NOTIFICATION_ID, createNotification())
            
            LogUtil.d(TAG, "开始播放")
        } catch (e: Exception) {
            LogUtil.e(TAG, "播放失败", e)
        }
    }
    
    private fun pause() {
        try {
            mediaPlayer?.pause()
            updatePlaybackState(PlaybackState.STATE_PAUSED)
            updateNotification()
            
            LogUtil.d(TAG, "暂停播放")
        } catch (e: Exception) {
            LogUtil.e(TAG, "暂停失败", e)
        }
    }
    
    private fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            updatePlaybackState(PlaybackState.STATE_STOPPED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            
            LogUtil.d(TAG, "停止播放")
        } catch (e: Exception) {
            LogUtil.e(TAG, "停止失败", e)
        }
    }
    
    private fun playNext() {
        if (playQueue.isEmpty()) return
        
        currentIndex = (currentIndex + 1) % playQueue.size
        playMedia(playQueue[currentIndex])
    }
    
    private fun playPrevious() {
        if (playQueue.isEmpty()) return
        
        currentIndex = if (currentIndex > 0) currentIndex - 1 else playQueue.size - 1
        playMedia(playQueue[currentIndex])
    }
    
    private fun playMedia(path: String) {
        try {
            stop()
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setOnPreparedListener {
                    start()
                    updatePlaybackState(PlaybackState.STATE_PLAYING)
                    updateNotification()
                }
                setOnCompletionListener {
                    playNext()
                }
                setOnErrorListener { _, what, extra ->
                    LogUtil.e(TAG, "播放错误: what=$what, extra=$extra")
                    true
                }
                prepareAsync()
            }
            
            LogUtil.d(TAG, "准备播放: $path")
        } catch (e: Exception) {
            LogUtil.e(TAG, "播放媒体失败", e)
        }
    }
    
    private fun prepareMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }
    }
    
    private fun updatePlaybackState(state: Int) {
        val playbackState = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_STOP or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_SEEK_TO
            )
            .setState(state, 0, 1f)
            .build()
        
        mediaSession.setPlaybackState(playbackState)
        _playbackState.value = playbackState
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MusicPlayerActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val playPauseAction = if (_playbackState.value.state == PlaybackState.STATE_PLAYING) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "暂停",
                createPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "播放",
                createPendingIntent(ACTION_PLAY)
            )
        }
        
        return NotificationCompat.Builder(this, AutoHubApplication.CHANNEL_MEDIA)
            .setContentTitle(currentTitle.ifEmpty { "未知标题" })
            .setContentText(currentArtist.ifEmpty { "未知艺术家" })
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(currentAlbumArt)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_previous, "上一首", createPendingIntent(ACTION_PREVIOUS))
            .addAction(playPauseAction)
            .addAction(R.drawable.ic_next, "下一首", createPendingIntent(ACTION_NEXT))
            .setOngoing(_playbackState.value.state == PlaybackState.STATE_PLAYING)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
    
    private fun createPendingIntent(action: String): PendingIntent {
        return PendingIntent.getService(
            this,
            action.hashCode(),
            Intent(this, MediaService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private val mediaSessionCallback = object : MediaSession.Callback() {
        override fun onPlay() {
            play()
        }
        
        override fun onPause() {
            pause()
        }
        
        override fun onStop() {
            stop()
        }
        
        override fun onSkipToNext() {
            playNext()
        }
        
        override fun onSkipToPrevious() {
            playPrevious()
        }
    }
    
    override fun onDestroy() {
        stop()
        mediaSession.release()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
        LogUtil.d(TAG, "媒体服务已销毁")
    }
}
