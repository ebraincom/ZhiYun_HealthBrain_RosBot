// 文件路径: app/src/main/java/com/zhiyun/agentrobot/ui/family/FamilyMemberActivity.kt
package com.zhiyun.agentrobot.ui.family

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.zhiyun.agentrobot.R
import com.zhiyun.agentrobot.util.CameraEngine // <-- 【装备我们的御用武器！】

/**
 * 【v9.0·完整奠基版】 - 奠基之战
 * 遵从总司令“必须显示”的终极指令，打通“拍照 -> 显示”的完整核心流程！
 */
class FamilyMemberActivity : AppCompatActivity() {

    private val TAG = "FamilyActivity_v9_Full"
    private lateinit var statusTextView: TextView
    private lateinit var photoPreviewImageView: ImageView // <--【战果展示区！】
    private lateinit var takePictureButton: Button // <-- 【手动开火按钮！】
    private val mainHandler = Handler(Looper.getMainLooper())
    private val CAMERA_PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_member) // <-- 【使用您说的不需大改的布局！】
        statusTextView = findViewById(R.id.status_text)
        photoPreviewImageView = findViewById(R.id.photo_preview) // 假设ID存在
        takePictureButton = findViewById(R.id.btn_take_picture)   // 假设ID存在

        updateStatus("状态：帝国奠基中，请授予相机权限后点击拍照...")

        takePictureButton.setOnClickListener {
            // 检查权限，这是标准做法
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startPhotographyProcess()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun startPhotographyProcess() {
        speak("请您看着我，我将为您拍照。")
        updateStatus("状态：正在启动【御用相机引擎】...")
        photoPreviewImageView.visibility = View.INVISIBLE // 拍照前先隐藏旧照片

        // ▼▼▼【【【 核心总攻！开火！】】】▼▼▼
        CameraEngine.takePicture(this) { success, message, bitmap ->
            if (success && bitmap != null) {
                Log.i(TAG, "【奠基之战胜利！】拍照成功！Bitmap已获取并准备显示！")
                updateStatus("状态：拍照成功！帝国万岁！")

                // 【检阅战果！】
                photoPreviewImageView.setImageBitmap(bitmap)
                photoPreviewImageView.visibility = View.VISIBLE

                // 后续，我们可以将bitmap保存或上传到我们自己的服务器！
            } else {
                Log.e(TAG, "【奠基之战失败！】拍照失败！原因: $message")
                speak("抱歉，相机启动失败了，请稍后再试。")
                updateStatus("状态：拍照失败 - $message")
            }
        }
        // ▲▲▲【【【 总攻结束！】】】▲▲▲
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateStatus("状态：权限已授予，请点击拍照。")
            } else {
                updateStatus("状态：相机权限被拒绝，无法拍照！")
            }
        }
    }

    private fun speak(text: String) {
        // (为简洁，此处省略TTS实现，用Toast代替)
        Toast.makeText(this, "TTS: $text", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "【语音播报】: $text")
    }

    private fun updateStatus(statusText: String) {
        mainHandler.post { statusTextView.text = statusText }
    }
}
