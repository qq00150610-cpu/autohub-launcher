/**
 * 会员中心ViewModel
 */
package com.autocar.launcher.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autocar.launcher.data.model.MemberInfo
import com.autocar.launcher.data.model.MemberProduct
import com.autocar.launcher.data.model.MemberBenefit
import com.autocar.launcher.data.network.ApiClient
import kotlinx.coroutines.launch

class MemberCenterViewModel : ViewModel() {

    // 会员信息
    private val _memberInfo = MutableLiveData<MemberInfo?>()
    val memberInfo: LiveData<MemberInfo?> = _memberInfo
    
    // 会员产品列表
    private val _products = MutableLiveData<List<MemberProduct>>()
    val products: LiveData<List<MemberProduct>> = _products
    
    // 会员权益列表
    private val _benefits = MutableLiveData<List<MemberBenefit>>()
    val benefits: LiveData<List<MemberBenefit>> = _benefits
    
    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 错误信息
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // 订单创建结果
    private val _orderCreated = MutableLiveData<OrderResult?>()
    val orderCreated: LiveData<OrderResult?> = _orderCreated
    
    data class OrderResult(
        val orderNo: String,
        val amount: Double
    )
    
    init {
        loadMemberInfo()
    }
    
    fun loadMemberInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.memberService.getMemberInfo()
                if (response.isSuccessful && response.body()?.success == true) {
                    _memberInfo.value = response.body()?.data
                } else {
                    _error.value = response.body()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            try {
                val response = ApiClient.memberService.getMemberProducts()
                if (response.isSuccessful && response.body()?.success == true) {
                    _products.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun loadBenefits() {
        viewModelScope.launch {
            try {
                val response = ApiClient.memberService.getMemberBenefits()
                if (response.isSuccessful && response.body()?.success == true) {
                    _benefits.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun createOrder(productType: String, paymentMethod: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.memberService.createOrder(productType, paymentMethod)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    _orderCreated.value = OrderResult(
                        orderNo = data?.orderNo ?: "",
                        amount = data?.amount ?: 0.0
                    )
                } else {
                    _error.value = response.body()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun payOrder(orderNo: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.memberService.payOrder(mapOf("order_no" to orderNo))
                if (response.isSuccessful && response.body()?.success == true) {
                    // 支付成功，重新加载会员信息
                    loadMemberInfo()
                    loadBenefits()
                } else {
                    _error.value = response.body()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearOrderResult() {
        _orderCreated.value = null
    }
}
