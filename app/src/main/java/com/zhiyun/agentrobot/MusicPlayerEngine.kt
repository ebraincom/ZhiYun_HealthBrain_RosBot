// 在 MusicPlayerEngine.kt 中

package com.zhiyun.agentrobot

import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

object MusicPlayerEngine {
    // 使用一个可空的MediaPlayer实例，以便每次都创建新的
    private var mediaPlayer: MediaPlayer? = null

    val musicDatabase = mapOf(
        "七里香" to "http://117.50.85.132/music/qilixiang.mp3"
    )

    /**
     * 播放音乐，并在完成或失败时通过回调通知
     * @param url 音乐地址
     * @param onCompletion 成功播放完成时的回调
     * @param onError 播放失败时的回调, 会返回具体的错误原因
     */
    fun playMusic(url: String, onCompletion: () -> Unit, onError: (String) -> Unit) {
        // 先释放之前的实例，确保干净的状态
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer?.setDataSource(url)

            // 监听完成事件
            mediaPlayer?.setOnCompletionListener {
                Log.i("MusicPlayerEngine", "Playback COMPLETED.")
                onCompletion()
                mediaPlayer?.release() // 释放资源
                mediaPlayer = null
            }

            // 监听错误事件
            mediaPlayer?.setOnErrorListener { _, what, extra ->
                val errorMsg = "Playback FAILED. Error code (what): $what, extra code: $extra"
                Log.e("MusicPlayerEngine", errorMsg)
                onError(errorMsg) // 播放出错时调用回调，并传递错误信息
                mediaPlayer?.release() // 释放资源
                mediaPlayer = null
                true // 返回true表示我们已经处理了错误
            }

            // 异步准备，准备好后开始播放
            mediaPlayer?.setOnPreparedListener { player ->
                Log.i("MusicPlayerEngine", "MediaPlayer Prepared. Starting playback.")
                player.start()
            }
            mediaPlayer?.prepareAsync()

        } catch (e: IOException) {
            val errorMsg = "Prepare FAILED (IOException): ${e.message}"
            Log.e("MusicPlayerEngine", errorMsg, e)
            onError(errorMsg) // 准备阶段就失败了，也要调用回调
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    fun stopMusic() {
        if (mediaPlayer?.isPlaying == true) {
            Log.i("MusicPlayerEngine", "Stopping playback and releasing MediaPlayer.")
            // 按照MediaPlayer的生命周期，先stop()再release()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } else if (mediaPlayer != null) {
            // 如果 MediaPlayer 存在但没有在播放（例如正在准备中），也直接释放
            Log.i("MusicPlayerEngine", "MediaPlayer was not playing, but releasing it.")
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    // ▲▲▲【新增结束】▲▲▲
}
