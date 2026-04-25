/**
 * 登录/注册Activity
 */
package com.autocar.launcher.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.autocar.launcher.R
import com.autocar.launcher.base.BaseActivity
import com.autocar.launcher.data.network.ApiClient
import com.autocar.launcher.data.repository.UserRepository
import com.autocar.launcher.databinding.ActivityLoginBinding
import com.autocar.launcher.ui.viewmodel.LoginViewModel
import com.autocar.launcher.util.LogUtil
import com.autocar.launcher.util.PreferencesManager
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val SMS_COUNTDOWN_SECONDS = 60L
    }

    private val viewModel by lazy { LoginViewModel() }
    private val userRepository by lazy { UserRepository() }
    
    // 当前模式: login 或 register
    private var currentMode = Mode.LOGIN
    
    // 登录方式: phone, email, password, wechat
    private var loginType = LoginType.PHONE
    
    private var countDownTimer: CountDownTimer? = null

    enum class Mode { LOGIN, REGISTER }
    enum class LoginType { PHONE, EMAIL, PASSWORD, WECHAT }

    override fun createBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun initView() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // 默认显示手机登录
        showPhoneLogin()
    }

    override fun initData() {
        checkAutoLogin()
    }

    override fun initListener() {
        // 返回
        binding.btnBack?.setOnClickListener {
            finish()
        }
        
        // 切换登录/注册模式
        binding.tvSwitchMode?.setOnClickListener {
            toggleMode()
        }
        
        // 发送验证码
        binding.btnSendCode?.setOnClickListener {
            sendVerifyCode()
        }
        
        // 登录/注册按钮
        binding.btnLogin?.setOnClickListener {
            performLoginOrRegister()
        }
        
        // 切换登录方式
        binding.layoutPhoneLogin?.setOnClickListener { showPhoneLogin() }
        binding.layoutEmailLogin?.setOnClickListener { showEmailLogin() }
        binding.layoutPasswordLogin?.setOnClickListener { showPasswordLogin() }
        binding.layoutWechatLogin?.setOnClickListener { showWechatLogin() }
        
        // 用户协议
        binding.cbAgreement?.setOnCheckedChangeListener { _, isChecked ->
            binding.btnLogin?.isEnabled = isChecked
        }
        
        // 忘记密码
        binding.tvForgotPassword?.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun checkAutoLogin() {
        if (PreferencesManager.getInstance().isLoggedIn()) {
            // 已登录，直接跳转
            navigateToMain()
        }
    }

    private fun toggleMode() {
        currentMode = if (currentMode == Mode.LOGIN) Mode.REGISTER else Mode.LOGIN
        
        if (currentMode == Mode.LOGIN) {
            binding.tvTitle?.text = "登录"
            binding.tvSwitchMode?.text = "没有账号？立即注册"
            binding.btnLogin?.text = "登录"
            binding.layoutPassword?.visibility = View.GONE
        } else {
            binding.tvTitle?.text = "注册"
            binding.tvSwitchMode?.text = "已有账号？立即登录"
            binding.btnLogin?.text = "注册"
            binding.layoutPassword?.visibility = View.VISIBLE
        }
    }

    private fun showPhoneLogin() {
        loginType = LoginType.PHONE
        updateLoginTypeIndicator()
        
        binding.layoutInput?.visibility = View.VISIBLE
        binding.etPhone?.visibility = View.VISIBLE
        binding.etEmail?.visibility = View.GONE
        binding.etPassword?.visibility = View.GONE
        binding.layoutCode?.visibility = View.VISIBLE
        binding.tvForgotPassword?.visibility = View.GONE
        
        binding.etPhone?.hint = "请输入手机号"
    }

    private fun showEmailLogin() {
        loginType = LoginType.EMAIL
        updateLoginTypeIndicator()
        
        binding.layoutInput?.visibility = View.VISIBLE
        binding.etPhone?.visibility = View.GONE
        binding.etEmail?.visibility = View.VISIBLE
        binding.etPassword?.visibility = View.GONE
        binding.layoutCode?.visibility = View.VISIBLE
        binding.tvForgotPassword?.visibility = View.GONE
        
        binding.etEmail?.hint = "请输入邮箱"
    }

    private fun showPasswordLogin() {
        loginType = LoginType.PASSWORD
        updateLoginTypeIndicator()
        
        binding.layoutInput?.visibility = View.VISIBLE
        binding.etPhone?.visibility = View.GONE
        binding.etEmail?.visibility = View.GONE
        binding.etPassword?.visibility = View.VISIBLE
        binding.layoutCode?.visibility = View.GONE
        binding.tvForgotPassword?.visibility = View.VISIBLE
        
        binding.etAccount?.hint = "手机号或邮箱"
        binding.etPassword?.hint = "请输入密码"
    }

    private fun showWechatLogin() {
        loginType = LoginType.WECHAT
        updateLoginTypeIndicator()
        
        // TODO: 实现微信登录
        showToast("微信登录开发中")
    }

    private fun updateLoginTypeIndicator() {
        // 更新登录方式指示器
    }

    private fun sendVerifyCode() {
        val target = when (loginType) {
            LoginType.PHONE -> binding.etPhone?.text?.toString()
            LoginType.EMAIL -> binding.etEmail?.text?.toString()
            else -> null
        }
        
        if (target.isNullOrBlank()) {
            showToast("请输入${if (loginType == LoginType.PHONE) "手机号" else "邮箱"}")
            return
        }
        
        val purpose = if (currentMode == Mode.LOGIN) "login" else "register"
        
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                val response = if (loginType == LoginType.PHONE) {
                    ApiClient.authService.sendSms(target, purpose)
                } else {
                    ApiClient.authService.sendEmail(target, purpose)
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    startCountdown()
                    showToast("验证码已发送")
                } else {
                    showToast(response.body()?.message ?: "发送失败")
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "发送验证码失败", e)
                showToast("网络错误，请重试")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun startCountdown() {
        binding.btnSendCode?.isEnabled = false
        
        countDownTimer = object : CountDownTimer(SMS_COUNTDOWN_SECONDS * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.btnSendCode?.text = "${seconds}秒后重发"
            }

            override fun onFinish() {
                binding.btnSendCode?.isEnabled = true
                binding.btnSendCode?.text = "发送验证码"
            }
        }.start()
    }

    private fun performLoginOrRegister() {
        if (!binding.cbAgreement?.isChecked!!) {
            showToast("请阅读并同意用户协议")
            return
        }
        
        when (currentMode) {
            Mode.LOGIN -> performLogin()
            Mode.REGISTER -> performRegister()
        }
    }

    private fun performLogin() {
        val code = binding.etCode?.text?.toString()
        
        if (code.isNullOrBlank()) {
            showToast("请输入验证码")
            return
        }
        
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                val response = when (loginType) {
                    LoginType.PHONE -> {
                        val phone = binding.etPhone?.text?.toString()
                        ApiClient.authService.loginByPhone(phone!!, code)
                    }
                    LoginType.EMAIL -> {
                        val email = binding.etEmail?.text?.toString()
                        ApiClient.authService.loginByEmail(email!!, code)
                    }
                    LoginType.PASSWORD -> {
                        val account = binding.etAccount?.text?.toString()
                        val password = binding.etPassword?.text?.toString()
                        ApiClient.authService.loginByPassword(account!!, password!!)
                    }
                    LoginType.WECHAT -> null
                }
                
                if (response?.isSuccessful == true && response.body()?.success == true) {
                    val data = response.body()?.data
                    data?.accessToken?.let { token ->
                        PreferencesManager.getInstance().saveToken(token)
                        PreferencesManager.getInstance().setLoggedIn(true)
                    }
                    data?.userId?.let { userId ->
                        PreferencesManager.getInstance().saveUserId(userId)
                    }
                    
                    showToast("登录成功")
                    navigateToMain()
                } else {
                    showToast(response?.body()?.message ?: "登录失败")
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "登录失败", e)
                showToast("网络错误，请重试")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun performRegister() {
        val target = when (loginType) {
            LoginType.PHONE -> binding.etPhone?.text?.toString()
            LoginType.EMAIL -> binding.etEmail?.text?.toString()
            else -> null
        }
        
        val code = binding.etCode?.text?.toString()
        val password = binding.etRegisterPassword?.text?.toString()
        
        if (target.isNullOrBlank()) {
            showToast("请输入${if (loginType == LoginType.PHONE) "手机号" else "邮箱"}")
            return
        }
        
        if (code.isNullOrBlank()) {
            showToast("请输入验证码")
            return
        }
        
        if (password.isNullOrBlank() || password.length < 6) {
            showToast("密码至少6位")
            return
        }
        
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                val response = if (loginType == LoginType.PHONE) {
                    ApiClient.authService.registerByPhone(target, code, password)
                } else {
                    ApiClient.authService.registerByEmail(target, code, password)
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    data?.accessToken?.let { token ->
                        PreferencesManager.getInstance().saveToken(token)
                        PreferencesManager.getInstance().setLoggedIn(true)
                    }
                    data?.userId?.let { userId ->
                        PreferencesManager.getInstance().saveUserId(userId)
                    }
                    
                    showToast("注册成功")
                    navigateToMain()
                } else {
                    showToast(response?.body()?.message ?: "注册失败")
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "注册失败", e)
                showToast("网络错误，请重试")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showForgotPasswordDialog() {
        // TODO: 实现忘记密码功能
        showToast("忘记密码功能开发中")
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar?.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin?.isEnabled = !show
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
