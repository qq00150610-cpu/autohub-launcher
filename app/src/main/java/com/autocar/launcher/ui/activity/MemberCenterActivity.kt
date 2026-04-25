/**
 * 会员中心Activity
 */
package com.autocar.launcher.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.data.network.ApiClient
import com.autocar.launcher.databinding.ActivityMemberCenterBinding
import com.autocar.launcher.ui.viewmodel.MemberCenterViewModel
import com.autocar.launcher.util.LogUtil
import com.autocar.launcher.util.PreferencesManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MemberCenterActivity : BaseActivity<ActivityMemberCenterBinding>() {

    companion object {
        private const val TAG = "MemberCenterActivity"
    }

    private val viewModel by lazy { MemberCenterViewModel() }
    
    // 会员等级名称
    private val levelNames = arrayOf("普通用户", "月度会员", "季度会员", "年度会员")

    override fun createBinding(): ActivityMemberCenterBinding {
        return ActivityMemberCenterBinding.inflate(layoutInflater)
    }

    override fun initView() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // 设置会员卡片
        setupMemberCard()
    }

    override fun initData() {
        loadMemberInfo()
        loadMemberBenefits()
    }

    override fun initListener() {
        // 返回
        binding.btnBack?.setOnClickListener {
            finish()
        }
        
        // 续费按钮
        binding.btnRenew?.setOnClickListener {
            showRenewDialog()
        }
        
        // 会员权益项点击
        binding.layoutBenefits?.setOnClickListener {
            showBenefitsDetail()
        }
        
        // 退出登录
        binding.btnLogout?.setOnClickListener {
            logout()
        }
        
        // 云同步设置
        binding.switchCloudSync?.setOnCheckedChangeListener { _, isChecked ->
            updateCloudSyncSetting(isChecked)
        }
        
        // 自动续费设置
        binding.switchAutoRenew?.setOnCheckedChangeListener { _, isChecked ->
            updateAutoRenewSetting(isChecked)
        }
    }

    private fun setupMemberCard() {
        // 初始化会员等级选择器
        binding.cardMemberLevel?.setOnClickListener {
            showMemberProducts()
        }
    }

    private fun loadMemberInfo() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.memberService.getMemberInfo()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    
                    // 更新会员等级
                    val level = data?.memberLevel ?: 0
                    binding.tvMemberLevel?.text = levelNames.getOrElse(level) { "普通用户" }
                    
                    // 更新到期时间
                    val expireAt = data?.memberExpireAt
                    if (expireAt != null) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        binding.tvExpireDate?.text = "到期时间: ${dateFormat.format(Date(expireAt))}"
                        binding.tvExpireDate?.visibility = View.VISIBLE
                        
                        // 计算剩余天数
                        val remaining = data.remainingDays ?: 0
                        binding.tvRemainingDays?.text = "剩余 $remaining 天"
                        binding.tvRemainingDays?.visibility = View.VISIBLE
                    } else {
                        binding.tvExpireDate?.visibility = View.GONE
                        binding.tvRemainingDays?.visibility = View.GONE
                    }
                    
                    // 更新存储信息
                    val totalStorage = data?.totalStorage ?: 0L
                    val usedStorage = data?.usedStorage ?: 0L
                    val percent = data?.storagePercent ?: 0
                    
                    binding.progressStorage?.progress = percent
                    binding.tvStorageInfo?.text = "${formatFileSize(usedStorage)} / ${formatFileSize(totalStorage)}"
                    
                    // 更新VIP标识
                    if (level > 0) {
                        binding.ivVipBadge?.visibility = View.VISIBLE
                        binding.btnRenew?.text = "续费会员"
                    } else {
                        binding.ivVipBadge?.visibility = View.GONE
                        binding.btnRenew?.text = "开通会员"
                    }
                    
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "加载会员信息失败", e)
            }
        }
    }

    private fun loadMemberBenefits() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.memberService.getMemberBenefits()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val benefits = response.body()?.data
                    
                    // 显示权益列表
                    val benefitsText = benefits?.take(5)?.joinToString("\n") { 
                        "✓ ${it.benefitName}" 
                    } ?: ""
                    
                    binding.tvBenefitsList?.text = benefitsText
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "加载会员权益失败", e)
            }
        }
    }

    private fun showRenewDialog() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.memberService.getMemberProducts()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val products = response.body()?.data
                    
                    val items = products?.mapIndexed { index, product ->
                        "${product.name} - ¥${product.price}/月"
                    }?.toTypedArray()
                    
                    if (items != null) {
                        AlertDialog.Builder(this@MemberCenterActivity)
                            .setTitle("选择会员套餐")
                            .setItems(items) { _, which ->
                                val selectedProduct = products[which]
                                createOrder(selectedProduct.type)
                            }
                            .setNegativeButton("取消", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "获取产品列表失败", e)
                Toast.makeText(this@MemberCenterActivity, "获取产品列表失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createOrder(productType: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.memberService.createOrder(productType, "alipay")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val orderNo = response.body()?.data?.orderNo
                    Toast.makeText(this@MemberCenterActivity, "订单创建成功: $orderNo", Toast.LENGTH_SHORT).show()
                    
                    // TODO: 调用支付
                    // 模拟支付
                    simulatePayment(orderNo!!)
                } else {
                    Toast.makeText(this@MemberCenterActivity, "订单创建失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "创建订单失败", e)
                Toast.makeText(this@MemberCenterActivity, "创建订单失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun simulatePayment(orderNo: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.memberService.payOrder(orderNo)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MemberCenterActivity, "支付成功！", Toast.LENGTH_LONG).show()
                    loadMemberInfo()
                    loadMemberBenefits()
                } else {
                    Toast.makeText(this@MemberCenterActivity, "支付失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "支付失败", e)
                Toast.makeText(this@MemberCenterActivity, "支付失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMemberProducts() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.memberService.getMemberProducts()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val products = response.body()?.data
                    
                    val message = products?.joinToString("\n\n") { product ->
                        "${product.name}\n" +
                        "价格: ¥${product.price}\n" +
                        "时长: ${product.duration}天\n" +
                        "权益: ${product.benefits.joinToString("、")}"
                    }
                    
                    AlertDialog.Builder(this@MemberCenterActivity)
                        .setTitle("会员套餐")
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show()
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "获取产品列表失败", e)
            }
        }
    }

    private fun showBenefitsDetail() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.memberService.getMemberBenefits()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val benefits = response.body()?.data
                    
                    val message = benefits?.joinToString("\n\n") { level ->
                        "${level.name}:\n" +
                        level.benefits.joinToString("\n") { "  • $it" }
                    }
                    
                    AlertDialog.Builder(this@MemberCenterActivity)
                        .setTitle("会员权益")
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show()
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "获取权益列表失败", e)
            }
        }
    }

    private fun updateCloudSyncSetting(enabled: Boolean) {
        PreferencesManager.getInstance().putBoolean("cloud_sync_enabled", enabled)
        Toast.makeText(this, if (enabled) "云同步已开启" else "云同步已关闭", Toast.LENGTH_SHORT).show()
    }

    private fun updateAutoRenewSetting(enabled: Boolean) {
        PreferencesManager.getInstance().putBoolean("auto_renew_enabled", enabled)
        Toast.makeText(this, if (enabled) "自动续费已开启" else "自动续费已关闭", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("确定") { _, _ ->
                PreferencesManager.getInstance().setLoggedIn(false)
                PreferencesManager.getInstance().clearToken()
                
                // 跳转到登录页面
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)}MB"
            else -> String.format("%.2fGB", size.toDouble() / (1024 * 1024 * 1024))
        }
    }
}
