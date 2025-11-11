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
    @SerializedName("id")
    val id: Int,

    @SerializedName("type")
    val type: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("artist")
    val artist: String?,

    @SerializedName("url")
    val url: String,

    @SerializedName("cover") // <-- 【【【关键修正！】】】与 music.py 的 'cover' 字段对齐
    val cover: String?,

    @SerializedName("lyrics")
    val lyrics: String?
    // 移除了所有与服务器不匹配或冗余的字段：mediaId, album, coverUrl, format, duration, category
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

