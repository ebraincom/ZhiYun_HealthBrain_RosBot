package com.zhiyun.agentrobot.data.network

import com.google.gson.annotations.SerializedName

/**
 * 最终的API响应体模型，与FastAPI的响应结构完全对应。
 */
data class FinalMediaResponse(
    val code: Int,
    val message: String,
    val data: MediaData? // 使用可空类型以增加健壮性
)

/**
 * 媒体数据的包装类。
 */
data class MediaData(
    val items: List<MediaItem>,
    val pagination: Pagination
)

/**
 * 单个媒体项的模型，定义了歌曲或视频的详细信息。
 */
data class MediaItem(
    val id: Int,
    @SerializedName("media_id")
    val mediaId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val url: String,
    @SerializedName("cover_url")
    val coverUrl: String?,
    val type: String,
    val format: String?,
    val duration: Int?,
    val category: String?
)

/**
 * 分页信息模型，用于支持加载更多数据。
 */
data class Pagination(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
    @SerializedName("totalItems")
    val totalItems: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)

