/**
 * 音乐播放器Activity
 */
package com.autocar.launcher.ui.activity

import android.animation.ObjectAnimator
import android.content.*
import android.graphics.BitmapFactory
import android.os.*
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.databinding.ActivityMusicPlayerBinding
import com.autocar.launcher.service.MediaService
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPlayerActivity : BaseActivity<ActivityMusicPlayerBinding>() {

    companion object {
        private const val TAG = "MusicPlayerActivity"
    }

    // 播放状态
    private var isPlaying = false
    private var isLyricsMode = false
    
    // 当前播放进度
    private var currentProgress = 0
    private var totalDuration = 100
    
    // 旋转动画
    private var discAnimator: ObjectAnimator? = null

    // 广播接收器
    private val mediaReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MediaService.ACTION_PLAY -> updatePlayState(true)
                MediaService.ACTION_PAUSE -> updatePlayState(false)
                MediaService.ACTION_NEXT -> loadNextTrack()
                MediaService.ACTION_PREVIOUS -> loadPreviousTrack()
                MediaService.ACTION_UPDATE_PROGRESS -> updateProgress()
            }
        }
    }

    override fun createBinding(): ActivityMusicPlayerBinding {
        return ActivityMusicPlayerBinding.inflate(layoutInflater)
    }

    override fun initView() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // 初始化播放控制
        setupControls()
        
        // 初始化进度条
        setupSeekBar()
        
        // 模拟加载音乐
        loadMockData()
    }

    override fun initData() {
        // 注册广播
        val filter = IntentFilter().apply {
            addAction(MediaService.ACTION_PLAY)
            addAction(MediaService.ACTION_PAUSE)
            addAction(MediaService.ACTION_NEXT)
            addAction(MediaService.ACTION_PREVIOUS)
            addAction(MediaService.ACTION_UPDATE_PROGRESS)
        }
        registerReceiver(mediaReceiver, filter)
    }

    override fun initListener() {
        // 返回
        binding.btnBack?.setOnClickListener {
            finish()
        }
        
        // 播放/暂停
        binding.btnPlayPause?.setOnClickListener {
            togglePlayPause()
        }
        
        // 上一首
        binding.btnPrevious?.setOnClickListener {
            playPrevious()
        }
        
        // 下一首
        binding.btnNext?.setOnClickListener {
            playNext()
        }
        
        // 播放模式
        binding.btnPlayMode?.setOnClickListener {
            cyclePlayMode()
        }
        
        // 收藏
        binding.btnFavorite?.setOnClickListener {
            toggleFavorite()
        }
        
        // 歌词模式
        binding.btnLyrics?.setOnClickListener {
            toggleLyricsMode()
        }
        
        // 播放列表
        binding.btnPlaylist?.setOnClickListener {
            showPlaylist()
        }
        
        // 浮窗模式
        binding.btnFloatMode?.setOnClickListener {
            enterFloatMode()
        }
    }

    private fun setupControls() {
        // 设置专辑封面点击
        binding.ivAlbumCover?.setOnClickListener {
            toggleDiscAnimation()
        }
        
        // 设置封面为圆形
        binding.ivAlbumCover?.clipToOutline = true
    }

    private fun setupSeekBar() {
        binding.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 停止自动播放
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 恢复自动播放
            }
        })
    }

    private fun loadMockData() {
        // 模拟音乐数据
        binding.tvSongTitle?.text = "Sample Song"
        binding.tvArtist?.text = "Artist Name"
        binding.tvAlbum?.text = "Album Name"
        
        // 模拟时长
        totalDuration = 240 // 4分钟
        binding.seekBar?.max = totalDuration
        binding.tvTotalDuration?.text = formatTime(totalDuration)
        
        // 模拟歌词
        binding.tvLyrics?.text = """
            [00:00] 欢迎使用凹凸桌面
            [00:05] 音乐播放器
            [00:10] 享受驾驶的乐趣
            [00:15] 让音乐陪伴您的旅途
        """.trimIndent()
    }

    private fun togglePlayPause() {
        isPlaying = !isPlaying
        
        if (isPlaying) {
            startDiscAnimation()
            startProgressUpdate()
            sendMediaCommand(MediaService.ACTION_PLAY)
        } else {
            pauseDiscAnimation()
            stopProgressUpdate()
            sendMediaCommand(MediaService.ACTION_PAUSE)
        }
        
        updatePlayButton()
    }

    private fun playNext() {
        sendMediaCommand(MediaService.ACTION_NEXT)
        loadNextTrack()
    }

    private fun playPrevious() {
        sendMediaCommand(MediaService.ACTION_PREVIOUS)
        loadPreviousTrack()
    }

    private fun seekTo(position: Int) {
        currentProgress = position
        sendMediaCommand(MediaService.ACTION_SEEK_TO, position)
    }

    private fun updatePlayButton() {
        binding.btnPlayPause?.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun updatePlayState(playing: Boolean) {
        isPlaying = playing
        updatePlayButton()
        
        if (playing) {
            startDiscAnimation()
        } else {
            pauseDiscAnimation()
        }
    }

    private fun loadNextTrack() {
        Toast.makeText(this, "加载下一首", Toast.LENGTH_SHORT).show()
    }

    private fun loadPreviousTrack() {
        Toast.makeText(this, "加载上一首", Toast.LENGTH_SHORT).show()
    }

    private fun cyclePlayMode() {
        // 循环播放模式: 列表循环 -> 单曲循环 -> 随机播放 -> 列表循环
    }

    private fun toggleFavorite() {
        binding.btnFavorite?.isSelected = !binding.btnFavorite?.isSelected!!
        
        if (binding.btnFavorite?.isSelected == true) {
            Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleLyricsMode() {
        isLyricsMode = !isLyricsMode
        
        if (isLyricsMode) {
            binding.layoutAlbum?.visibility = View.GONE
            binding.layoutLyrics?.visibility = View.VISIBLE
        } else {
            binding.layoutAlbum?.visibility = View.VISIBLE
            binding.layoutLyrics?.visibility = View.GONE
        }
    }

    private fun showPlaylist() {
        // TODO: 显示播放列表对话框
        Toast.makeText(this, "播放列表", Toast.LENGTH_SHORT).show()
    }

    private fun enterFloatMode() {
        // 发送广播进入浮窗模式
        val intent = Intent(MediaService.ACTION_ENTER_FLOAT_MODE)
        sendBroadcast(intent)
        
        finish()
    }

    private fun startDiscAnimation() {
        discAnimator?.cancel()
        
        discAnimator = ObjectAnimator.ofFloat(binding.ivAlbumCover, "rotation", 0f, 360f).apply {
            duration = 20000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun pauseDiscAnimation() {
        discAnimator?.pause()
    }

    private fun toggleDiscAnimation() {
        if (discAnimator?.isRunning == true) {
            discAnimator?.pause()
        } else if (discAnimator?.isPaused == true) {
            discAnimator?.resume()
        } else {
            startDiscAnimation()
        }
    }

    private var progressUpdateJob: kotlinx.coroutines.Job? = null

    private fun startProgressUpdate() {
        progressUpdateJob = lifecycleScope.launch {
            while (true) {
                delay(1000)
                if (isPlaying && currentProgress < totalDuration) {
                    currentProgress++
                    updateProgressUI()
                }
            }
        }
    }

    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
    }

    private fun updateProgress() {
        // 从服务获取真实进度
    }

    private fun updateProgressUI() {
        binding.seekBar?.progress = currentProgress
        binding.tvCurrentTime?.text = formatTime(currentProgress)
    }

    private fun sendMediaCommand(action: String, extra: Int = 0) {
        val intent = Intent(this, MediaService::class.java).apply {
            this.action = action
            if (extra > 0) {
                putExtra(MediaService.EXTRA_POSITION, extra)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    override fun onResume() {
        super.onResume()
        if (isPlaying) {
            startDiscAnimation()
            startProgressUpdate()
        }
    }

    override fun onPause() {
        super.onPause()
        pauseDiscAnimation()
        stopProgressUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(mediaReceiver)
        } catch (e: Exception) {
            LogUtil.e(TAG, "注销广播失败", e)
        }
        discAnimator?.cancel()
    }
}
