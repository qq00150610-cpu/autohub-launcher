package com.autocar.launcher.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.os.Result
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.autocar.launcher.AutoHubApplication
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseService
import com.autocar.launcher.ui.activity.MainActivity
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * 语音助手服务
 * 提供语音识别和语音助手功能
 * 
 * 功能：
 * - 语音唤醒
 * - 语音识别
 * - 命令词解析
 * - TTS 语音播报
 */
class VoiceAssistantService : BaseService() {

    companion object {
        private const val TAG = "VoiceAssistantService"
        private const val NOTIFICATION_ID = 1007
        
        const val ACTION_START_LISTENING = "com.autocar.launcher.action.START_VOICE_LISTENING"
        const val ACTION_STOP_LISTENING = "com.autocar.launcher.action.STOP_VOICE_LISTENING"
        const val ACTION_SPEAK = "com.autocar.launcher.action.SPEAK"
    }
    
    // SpeechRecognizer
    private var speechRecognizer: SpeechRecognizer? = null
    
    // Handler
    private val handler = Handler(Looper.getMainLooper())
    
    // 监听状态
    private val _listeningState = MutableStateFlow(ListeningState.IDLE)
    val listeningState: StateFlow<ListeningState> = _listeningState.asStateFlow()
    
    // 识别结果
    private val _recognitionResult = MutableStateFlow("")
    val recognitionResult: StateFlow<String> = _recognitionResult.asStateFlow()
    
    // 命令回调
    private var commandCallback: VoiceCommandCallback? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化语音识别器
        initSpeechRecognizer()
        
        LogUtil.d(TAG, "语音助手服务已创建")
    }
    
    override fun onHandleStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_LISTENING -> startListening()
            ACTION_STOP_LISTENING -> stopListening()
            ACTION_SPEAK -> {
                val text = intent.getStringExtra("text")
                if (!text.isNullOrEmpty()) {
                    speak(text)
                }
            }
        }
        return START_STICKY
    }
    
    override fun onHandleBind(intent: Intent?): android.os.IBinder? = null
    
    /**
     * 初始化语音识别器
     */
    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(recognitionListener)
            }
        } else {
            LogUtil.e(TAG, "设备不支持语音识别")
        }
    }
    
    /**
     * 开始监听
     */
    private fun startListening() {
        if (speechRecognizer == null) {
            LogUtil.e(TAG, "语音识别器未初始化")
            return
        }
        
        if (_listeningState.value == ListeningState.LISTENING) {
            LogUtil.d(TAG, "已经在监听中")
            return
        }
        
        _listeningState.value = ListeningState.LISTENING
        startForeground(NOTIFICATION_ID, createNotification())
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            LogUtil.d(TAG, "开始语音监听")
        } catch (e: Exception) {
            LogUtil.e(TAG, "启动语音监听失败", e)
            _listeningState.value = ListeningState.ERROR
        }
    }
    
    /**
     * 停止监听
     */
    private fun stopListening() {
        speechRecognizer?.stopListening()
        _listeningState.value = ListeningState.IDLE
        LogUtil.d(TAG, "停止语音监听")
    }
    
    /**
     * 语音播报
     */
    private fun speak(text: String) {
        // TODO: 实现 TTS 语音播报
        // 可以使用 android.speech.tts.TextToSpeech
        LogUtil.d(TAG, "语音播报: $text")
    }
    
    /**
     * 处理识别结果
     */
    private fun processRecognition(text: String) {
        _recognitionResult.value = text
        LogUtil.d(TAG, "识别结果: $text")
        
        // 解析命令
        parseAndExecuteCommand(text)
    }
    
    /**
     * 解析并执行命令
     */
    private fun parseAndExecuteCommand(text: String) {
        val lowerText = text.lowercase(Locale.getDefault())
        
        // 简单的命令词匹配
        when {
            lowerText.contains("导航") || lowerText.contains("去") -> {
                commandCallback?.onNavigationCommand(extractDestination(text))
            }
            lowerText.contains("播放") || lowerText.contains("音乐") -> {
                commandCallback?.onMusicCommand(extractSongName(text))
            }
            lowerText.contains("打电话") || lowerText.contains("拨号") -> {
                commandCallback?.onPhoneCommand(extractPhoneNumber(text))
            }
            lowerText.contains("打开") || lowerText.contains("启动") -> {
                commandCallback?.onAppCommand(extractAppName(text))
            }
            lowerText.contains("关闭") || lowerText.contains("退出") -> {
                commandCallback?.onCloseCommand(extractTarget(text))
            }
            else -> {
                // 默认处理
                commandCallback?.onUnknownCommand(text)
            }
        }
    }
    
    // 辅助方法 - 提取目的地
    private fun extractDestination(text: String): String {
        return text.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "")
    }
    
    // 辅助方法 - 提取歌曲名
    private fun extractSongName(text: String): String {
        return text.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "")
    }
    
    // 辅助方法 - 提取电话号码
    private fun extractPhoneNumber(text: String): String {
        return text.replace(Regex("[^0-9]"), "")
    }
    
    // 辅助方法 - 提取应用名
    private fun extractAppName(text: String): String {
        return text.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "")
    }
    
    // 辅助方法 - 提取目标
    private fun extractTarget(text: String): String {
        return text.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "")
    }
    
    /**
     * 设置命令回调
     */
    fun setCommandCallback(callback: VoiceCommandCallback) {
        commandCallback = callback
    }
    
    /**
     * 语音识别监听器
     */
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            LogUtil.d(TAG, "准备就绪，等待语音输入")
        }
        
        override fun onBeginningOfSpeech() {
            LogUtil.d(TAG, "开始说话")
        }
        
        override fun onRmsChanged(rmsdB: Float) {
            // 音量变化
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {
            // 音频缓冲区
        }
        
        override fun onEndOfSpeech() {
            LogUtil.d(TAG, "说话结束")
            _listeningState.value = ListeningState.PROCESSING
        }
        
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "音频录制错误"
                SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                SpeechRecognizer.ERROR_NO_MATCH -> "没有匹配结果"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙"
                SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有语音输入"
                else -> "未知错误"
            }
            
            LogUtil.e(TAG, "语音识别错误: $errorMessage (code: $error)")
            _listeningState.value = ListeningState.ERROR
            
            // 延迟重试
            handler.postDelayed({
                _listeningState.value = ListeningState.IDLE
            }, 1000)
        }
        
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                processRecognition(matches[0])
            }
            
            // 继续监听
            handler.postDelayed({
                _listeningState.value = ListeningState.IDLE
            }, 500)
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _recognitionResult.value = matches[0]
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {
            // 事件回调
        }
    }
    
    /**
     * 创建通知
     */
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, VoiceAssistantService::class.java).apply { action = ACTION_STOP_LISTENING },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stateText = when (_listeningState.value) {
            ListeningState.IDLE -> "语音助手就绪"
            ListeningState.LISTENING -> "正在聆听..."
            ListeningState.PROCESSING -> "正在处理..."
            ListeningState.SPEAKING -> "正在播报..."
            ListeningState.ERROR -> "发生错误"
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, AutoHubApplication.CHANNEL_VOICE)
                .setContentTitle("语音助手")
                .setContentText(stateText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_stop, "停止", stopIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("语音助手")
                .setContentText(stateText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_stop, "停止", stopIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build()
        }
    }
    
    override fun onDestroy() {
        stopListening()
        speechRecognizer?.destroy()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
        LogUtil.d(TAG, "语音助手服务已销毁")
    }
    
    /**
     * 监听状态枚举
     */
    enum class ListeningState {
        IDLE,
        LISTENING,
        PROCESSING,
        SPEAKING,
        ERROR
    }
    
    /**
     * 语音命令回调接口
     */
    interface VoiceCommandCallback {
        fun onNavigationCommand(destination: String)
        fun onMusicCommand(songName: String)
        fun onPhoneCommand(phoneNumber: String)
        fun onAppCommand(appName: String)
        fun onCloseCommand(target: String)
        fun onUnknownCommand(text: String)
    }
}
