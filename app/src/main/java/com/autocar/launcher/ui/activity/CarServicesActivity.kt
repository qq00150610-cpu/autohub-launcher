/**
 * 车主服务Activity
 */
package com.autocar.launcher.ui.activity

import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.databinding.ActivityCarServicesBinding
import com.autocar.launcher.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CarServicesActivity : BaseActivity<ActivityCarServicesBinding>() {

    companion object {
        private const val TAG = "CarServicesActivity"
    }

    private val handler = Handler(Looper.getMainLooper())
    
    // 当前功能
    private var currentFunction = Function.NONE
    
    enum class Function {
        NONE, FILE_MANAGER, CLEAN_TRASH, DATA_USAGE, APP_BACKUP, REMOTE_TRANSFER
    }

    override fun createBinding(): ActivityCarServicesBinding {
        return ActivityCarServicesBinding.inflate(layoutInflater)
    }

    override fun initView() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // 默认显示文件管理
        showFileManager()
    }

    override fun initListener() {
        // 返回
        binding.btnBack?.setOnClickListener {
            finish()
        }
        
        // 文件管理
        binding.cardFileManager?.setOnClickListener {
            showFileManager()
        }
        
        // 垃圾清理
        binding.cardCleanTrash?.setOnClickListener {
            showCleanTrash()
        }
        
        // 流量监控
        binding.cardDataUsage?.setOnClickListener {
            showDataUsage()
        }
        
        // 应用备份
        binding.cardAppBackup?.setOnClickListener {
            showAppBackup()
        }
        
        // 远程传输
        binding.cardRemoteTransfer?.setOnClickListener {
            showRemoteTransfer()
        }
        
        // 一键清理按钮
        binding.btnCleanAll?.setOnClickListener {
            performCleanAll()
        }
        
        // 开始扫描按钮
        binding.btnStartScan?.setOnClickListener {
            startScan()
        }
    }

    private fun showFileManager() {
        currentFunction = Function.FILE_MANAGER
        updateFunctionIndicator()
        
        binding.layoutFileManager?.visibility = View.VISIBLE
        binding.layoutCleanTrash?.visibility = View.GONE
        binding.layoutDataUsage?.visibility = View.GONE
        binding.layoutAppBackup?.visibility = View.GONE
        binding.layoutRemoteTransfer?.visibility = View.GONE
        
        loadFileSystem()
    }

    private fun showCleanTrash() {
        currentFunction = Function.CLEAN_TRASH
        updateFunctionIndicator()
        
        binding.layoutFileManager?.visibility = View.GONE
        binding.layoutCleanTrash?.visibility = View.VISIBLE
        binding.layoutDataUsage?.visibility = View.GONE
        binding.layoutAppBackup?.visibility = View.GONE
        binding.layoutRemoteTransfer?.visibility = View.GONE
    }

    private fun showDataUsage() {
        currentFunction = Function.DATA_USAGE
        updateFunctionIndicator()
        
        binding.layoutFileManager?.visibility = View.GONE
        binding.layoutCleanTrash?.visibility = View.GONE
        binding.layoutDataUsage?.visibility = View.VISIBLE
        binding.layoutAppBackup?.visibility = View.GONE
        binding.layoutRemoteTransfer?.visibility = View.GONE
        
        loadDataUsage()
    }

    private fun showAppBackup() {
        currentFunction = Function.APP_BACKUP
        updateFunctionIndicator()
        
        binding.layoutFileManager?.visibility = View.GONE
        binding.layoutCleanTrash?.visibility = View.GONE
        binding.layoutDataUsage?.visibility = View.GONE
        binding.layoutAppBackup?.visibility = View.VISIBLE
        binding.layoutRemoteTransfer?.visibility = View.GONE
        
        loadInstalledApps()
    }

    private fun showRemoteTransfer() {
        currentFunction = Function.REMOTE_TRANSFER
        updateFunctionIndicator()
        
        binding.layoutFileManager?.visibility = View.GONE
        binding.layoutCleanTrash?.visibility = View.GONE
        binding.layoutDataUsage?.visibility = View.GONE
        binding.layoutAppBackup?.visibility = View.GONE
        binding.layoutRemoteTransfer?.visibility = View.VISIBLE
    }

    private fun updateFunctionIndicator() {
        val cards = mapOf(
            binding.cardFileManager to (currentFunction == Function.FILE_MANAGER),
            binding.cardCleanTrash to (currentFunction == Function.CLEAN_TRASH),
            binding.cardDataUsage to (currentFunction == Function.DATA_USAGE),
            binding.cardAppBackup to (currentFunction == Function.APP_BACKUP),
            binding.cardRemoteTransfer to (currentFunction == Function.REMOTE_TRANSFER)
        )
        
        cards.forEach { (card, selected) ->
            card?.alpha = if (selected) 1f else 0.6f
        }
    }

    private fun loadFileSystem() {
        lifecycleScope.launch {
            try {
                val storageInfo = withContext(Dispatchers.IO) {
                    getStorageInfo()
                }
                
                binding.tvInternalStorage?.text = "内部存储: ${storageInfo.internal}"
                binding.tvExternalStorage?.text = "外部存储: ${storageInfo.external}"
            } catch (e: Exception) {
                LogUtil.e(TAG, "加载存储信息失败", e)
            }
        }
    }

    private data class StorageInfo(val internal: String, val external: String)

    private fun getStorageInfo(): StorageInfo {
        val internalPath = Environment.getDataDirectory()
        val externalPath = Environment.getExternalStorageDirectory()
        
        val internal = formatFileSize(getFolderSize(internalPath))
        val external = if (externalPath.exists()) {
            formatFileSize(getFolderSize(externalPath))
        } else {
            "无可用存储"
        }
        
        return StorageInfo(internal, external)
    }

    private fun getFolderSize(path: File): Long {
        var size = 0L
        if (path.exists()) {
            path.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getFolderSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)}MB"
            else -> String.format("%.2fGB", size.toDouble() / (1024 * 1024 * 1024))
        }
    }

    private fun loadDataUsage() {
        lifecycleScope.launch {
            try {
                val usageInfo = withContext(Dispatchers.IO) {
                    getDataUsage()
                }
                
                binding.tvTodayUsage?.text = "今日: ${usageInfo.today}"
                binding.tvMonthUsage?.text = "本月: ${usageInfo.month}"
            } catch (e: Exception) {
                LogUtil.e(TAG, "加载流量信息失败", e)
            }
        }
    }

    private data class UsageInfo(val today: String, val month: String)

    private fun getDataUsage(): UsageInfo {
        // 这里可以调用系统API获取真实的流量使用情况
        // 由于权限限制，这里返回模拟数据
        return UsageInfo(
            today = "${(Math.random() * 500).toInt()}MB",
            month = "${(Math.random() * 5000).toInt()}MB"
        )
    }

    private fun loadInstalledApps() {
        lifecycleScope.launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                    getUserApps()
                }
                
                binding.tvInstalledApps?.text = "已安装应用: ${apps.size}个"
            } catch (e: Exception) {
                LogUtil.e(TAG, "加载应用列表失败", e)
            }
        }
    }

    private fun getUserApps(): List<String> {
        val pm = packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
        intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        
        return pm.queryIntentActivities(intent, 0)
            .filter { !it.activityInfo.packageName.startsWith("com.android.") }
            .map { it.activityInfo.packageName }
    }

    private fun startScan() {
        binding.progressScan?.visibility = View.VISIBLE
        binding.btnStartScan?.isEnabled = false
        binding.tvScanStatus?.text = "扫描中..."
        
        lifecycleScope.launch {
            try {
                // 模拟扫描
                withContext(Dispatchers.IO) {
                    for (i in 1..10) {
                        kotlinx.coroutines.delay(300)
                        binding.progressScan?.progress = i * 10
                    }
                }
                
                binding.tvScanStatus?.text = "扫描完成"
                binding.btnStartScan?.text = "重新扫描"
            } catch (e: Exception) {
                LogUtil.e(TAG, "扫描失败", e)
                binding.tvScanStatus?.text = "扫描失败"
            } finally {
                binding.progressScan?.visibility = View.GONE
                binding.btnStartScan?.isEnabled = true
            }
        }
    }

    private fun performCleanAll() {
        AlertDialog.Builder(this)
            .setTitle("确认清理")
            .setMessage("确定要清理选中的缓存文件吗？")
            .setPositiveButton("确定") { _, _ ->
                doClean()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun doClean() {
        binding.progressClean?.visibility = View.VISIBLE
        binding.btnCleanAll?.isEnabled = false
        binding.tvCleanStatus?.text = "清理中..."
        
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 清理缓存
                    cacheDir.deleteRecursively()
                    externalCacheDir?.deleteRecursively()
                }
                
                binding.tvCleanStatus?.text = "清理完成"
                Toast.makeText(this@CarServicesActivity, "清理完成", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                LogUtil.e(TAG, "清理失败", e)
                binding.tvCleanStatus?.text = "清理失败"
                Toast.makeText(this@CarServicesActivity, "清理失败", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressClean?.visibility = View.GONE
                binding.btnCleanAll?.isEnabled = true
            }
        }
    }
}
