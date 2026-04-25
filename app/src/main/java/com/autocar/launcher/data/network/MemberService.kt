/**
 * 会员服务接口
 */
package com.autocar.launcher.data.network

import com.autocar.launcher.data.model.*
import retrofit2.Call
import retrofit2.http.*

interface MemberService {

    /**
     * 获取会员信息
     */
    @GET("member/info")
    fun getMemberInfo(): Call<ApiResponse<MemberInfo>>

    /**
     * 获取会员产品列表
     */
    @GET("member/products")
    fun getMemberProducts(): Call<ApiResponse<List<MemberProduct>>>

    /**
     * 购买会员
     */
    @POST("member/purchase")
    fun purchaseMember(
        @Field("productId") productId: String
    ): Call<ApiResponse<MemberInfo>>

    /**
     * 获取会员订单列表
     */
    @GET("member/orders")
    fun getMemberOrders(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Call<ApiResponse<List<MemberOrder>>>
}

/**
 * 会员订单
 */
data class MemberOrder(
    val orderId: String,
    val productId: String,
    val productName: String,
    val amount: Double,
    val status: Int,
    val createdAt: String,
    val paidAt: String?
)
