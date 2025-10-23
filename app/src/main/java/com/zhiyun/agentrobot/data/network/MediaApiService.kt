package com.zhiyun.agentrobot.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 【极简最终版】专用于媒体服务的API接口。
 * 职责：定义一个最简单的、基于关键字的搜索方法。
 */
interface MediaApiService {

    /**
     * 【回归本源】
     * 从服务器搜索媒体资源。这个定义与您之前能工作的版本完全一致。
     * Retrofit会自动将请求拼接为: .../api/v1/media?keyword=七里香
     */
    @GET("/api/v1/media")
    suspend fun searchMedia(
        @Query("keyword") keyword: String
    ): FinalMediaResponse
}
