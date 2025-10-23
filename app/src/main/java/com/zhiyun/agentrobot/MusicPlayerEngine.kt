package com.zhiyun.agentrobot

import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

/**
 * 媒体播放引擎 (v2.0 - 最终重塑版)
 *
 * 这是一个真正的、状态可控的单例播放引擎。
 * 它内部维护一个唯一的、稳定的MediaPlayer实例，确保了在任何时候，
 * 应用内只有一个声音在播放，并且可以被精确地控制。
 */
object MusicPlayerEngine {

    // ▼▼▼【核心改造 1：将mediaPlayer实例设为私有且稳定】▼▼▼
    // 这个实例在整个Engine的生命周期中只有一个，不会被随意置为null。
    // 我们只替换它的数据源，而不是销毁它。
    private var mediaPlayer: MediaPlayer? = null
    private const val TAG = "MusicPlayerEngine_V2"

    // 数据库保持不变，但已不再是主要数据源
    val musicDatabase: Map<String, String> = mapOf(
        "七里香" to "https://www.bubuyony.com/music/qilixiang.mp3"
    )

    // ▼▼▼【核心改造 2：提供一个公开、安全的播放状态查询方法】▼▼▼
    /**
     * 安全地检查当前是否正在播放音乐。
     * @return 如果MediaPlayer实例存在且正在播放，则为true。
     */
    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: IllegalStateException) {
            Log.w(TAG, "isPlaying check failed with IllegalStateException, considering as not playing.")
            false
        }
    }

    // ▼▼▼【核心改造 3：重构 playMusic 方法，逻辑更健壮】▼▼▼
    /**
     * 播放指定URL的音乐。
     * 此方法会重用或创建唯一的MediaPlayer实例。
     *
     * @param url 要播放的音乐的URL。
     * @param onCompletion 播放完成时的回调。
     * @param onError 发生错误时的回调。
     */
    fun playMusic(url: String, onCompletion: () -> Unit, onError: (String) -> Unit) {
        Log.i(TAG, "Received play command for URL: $url")

        // 确保在主线程（或有Looper的线程）中操作MediaPlayer
        // 如果在协程中调用，确保上下文是正确的

        try {
            // 如果实例不存在，则创建它。
            if (mediaPlayer == null) {
                Log.d(TAG, "MediaPlayer instance is null, creating a new one.")
                mediaPlayer = MediaPlayer()
            }

            // 如果正在播放，先停止并重置，以播放新的音乐。
            if (mediaPlayer!!.isPlaying) {
                Log.w(TAG, "Player is currently playing. Stopping it before starting new track.")
                mediaPlayer!!.stop()
            }

            Log.d(TAG, "Resetting MediaPlayer to a clean state.")
            mediaPlayer!!.reset() // 重置到空闲状态

            Log.d(TAG, "Setting data source: $url")
            mediaPlayer!!.setDataSource(url)

            // 设置监听器
            mediaPlayer!!.setOnCompletionListener {
                Log.i(TAG, "Playback completed.")
                onCompletion()
            }
            mediaPlayer!!.setOnErrorListener { _, what, extra ->
                val errorMsg = "MediaPlayer Error! What: $what, Extra: $extra"
                Log.e(TAG, errorMsg)
                // 发生错误后，尝试重置播放器以备下次使用
                try {
                    mediaPlayer?.reset() // ✅ 正确：我们应该重置的是 mediaPlayer 这个实例，并且使用安全调用?.

                } catch (resetException: Exception) {
                    Log.e(TAG, "Failed to reset player after error.", resetException)
                }
                onError(errorMsg)
                true // 返回true表示我们已经处理了错误
            }

            Log.d(TAG, "Preparing player asynchronously...")
            mediaPlayer!!.setOnPreparedListener { player ->
                Log.i(TAG, "Player prepared. Starting playback.")
                player.start()
            }
            mediaPlayer!!.prepareAsync()

        } catch (e: IOException) {
            val errorMsg = "Prepare FAILED (IOException): ${e.message}"
            Log.e(TAG, errorMsg, e)
            onError(errorMsg)
        } catch (e: IllegalStateException) {
            val errorMsg = "Operation FAILED (IllegalStateException): ${e.message}"
            Log.e(TAG, errorMsg, e)
            onError(errorMsg)
            // 发生严重状态错误时，可能需要重建播放器
            release() // 释放坏掉的实例
        }
    }

    // ▼▼▼【核心改造 4：重构 stopMusic 方法，控制唯一实例】▼▼▼
    /**
     * 停止当前正在播放的音乐。
     * 此方法会操作那个唯一的MediaPlayer实例。
     */
    fun stopMusic() {
        Log.i(TAG, "Received stop command.")
        if (mediaPlayer != null && isPlaying()) {
            Log.d(TAG, "Player is playing, stopping now.")
            mediaPlayer!!.stop()
            // stop()之后会自动进入Stopped状态，下次播放前需要prepare
            Log.i(TAG, "Playback stopped successfully.")
        } else {
            Log.w(TAG, "Stop command ignored: Player was not running or was null.")
        }
    }

    /**
     * 在应用退出时，释放MediaPlayer资源。
     * 可以在Activity的onDestroy中调用。
     */
    fun release() {
        Log.w(TAG, "Releasing MediaPlayer resources.")
        mediaPlayer?.release()
        mediaPlayer = null
    }
}