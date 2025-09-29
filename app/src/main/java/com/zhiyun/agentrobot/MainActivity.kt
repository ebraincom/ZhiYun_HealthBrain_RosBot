package com.zhiyun.agentrobot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zhiyun.agentrobot.ui.screens.HomeScreen
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
// import com.ainirobot.agent.AgentCore // 如果您的 HomeScreen 或其他地方确实需要，再取消注释

class MainActivity : ComponentActivity() {

    // --- 状态变量声明区 ---
    private var isRecordAudioPermissionGranted by mutableStateOf(false)
    private var isCameraPermissionGranted by mutableStateOf(false)
    private var isAgentSdkInitialized by mutableStateOf(false)
    private var isLoadingPermissions by mutableStateOf(true) // 用于初始权限检查和SDK加载

    // --- ActivityResultLaunchers 声明区 ---
    private val requestRecordAudioLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "RECORD_AUDIO permission GRANTED by launcher")
                isRecordAudioPermissionGranted = true
                // 麦克风权限通过后，检查并请求摄像头权限
                checkAndRequestCameraPermission()
            } else {
                Log.w("MainActivity", "RECORD_AUDIO permission DENIED by launcher")
                isLoadingPermissions = false // 结束加载状态
                // 您可以在这里添加UI提示，告知用户为何需要此权限
            }
        }

    private val requestCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "CAMERA permission GRANTED by launcher")
                isCameraPermissionGranted = true
                // 摄像头权限也通过后（此时麦克风应该已经授权），初始化 SDK
                attemptInitializeAgentSDK()
            } else {
                Log.w("MainActivity", "CAMERA permission DENIED by launcher")
                isLoadingPermissions = false // 结束加载状态
                // 您可以在这里添加UI提示
            }
        }

    // --- onCreate 方法 ---
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "Activity onCreate: START")
        super.onCreate(savedInstanceState)

        // --- 您可以把获取屏幕信息的代码放在这里 START ---
        val displayMetrics = resources.displayMetrics
        val densityDpi = displayMetrics.densityDpi
        val density = displayMetrics.density // density factor
        val screenWidthPx = displayMetrics.widthPixels
        val screenHeightPx = displayMetrics.heightPixels
        val screenWidthDp = screenWidthPx / density
        val screenHeightDp = screenHeightPx / density

        Log.i("MainActivityScreenInfo", "Density DPI: $densityDpi")
        Log.i("MainActivityScreenInfo", "Density (factor): $density")
        Log.i("MainActivityScreenInfo", "Screen Width (px): $screenWidthPx")
        Log.i("MainActivityScreenInfo", "Screen Height (px): $screenHeightPx")
        Log.i("MainActivityScreenInfo", "Screen Width (dp): $screenWidthDp")
        Log.i("MainActivityScreenInfo", "Screen Height (dp): $screenHeightDp")
        // --- 获取屏幕信息的代码放在这里 END ---




        setContent {
            Log.d("MainActivity", "setContent Composable lambda: ENTER")
            ZhiyunAgentRobotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAgentSdkInitialized) {
                        Log.d("MainActivity", "UI: AgentSDK is initialized, rendering HomeScreen")
                        HomeScreen()
                    } else if (isLoadingPermissions) {
                        Log.d("MainActivity", "UI: Loading permissions or SDK...")
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(8.dp))
                                Text("正在准备应用...")
                            }
                        }
                    } else {
                        Log.d("MainActivity", "UI: Permissions NOT fully granted or SDK NOT initialized, showing PermissionsScreen")
                        // PermissionsScreen 现在直接接收成员函数作为 lambda
                        PermissionsScreen(
                            onGrantRecordAudio = { launchRecordAudioPermissionRequest() },
                            onGrantCamera = { launchCameraPermissionRequest() },
                            recordAudioGranted = isRecordAudioPermissionGranted,
                            cameraGranted = isCameraPermissionGranted
                        )
                    }
                }
            }
            Log.d("MainActivity", "setContent Composable lambda: EXIT")
        }
        Log.d("MainActivity", "setContent: FINISHED")

        // 应用启动时开始检查权限状态并按需请求
        checkInitialPermissions()
        Log.d("MainActivity", "Activity onCreate: FINISHED")
    }

    // --- 成员函数声明区 (确保这些函数在 MainActivity 类的花括号内，与 onCreate 并列) ---

    private fun checkInitialPermissions() {
        isLoadingPermissions = true // 开始时总是标记为加载
        val recordAudioHasPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val cameraHasPermission = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        isRecordAudioPermissionGranted = recordAudioHasPermission
        isCameraPermissionGranted = cameraHasPermission

        if (recordAudioHasPermission && cameraHasPermission) {
            Log.d("MainActivity", "InitialCheck: Both permissions already GRANTED.")
            attemptInitializeAgentSDK() // 如果权限都有，直接尝试初始化
        } else if (!recordAudioHasPermission) {
            Log.d("MainActivity", "InitialCheck: RECORD_AUDIO not granted. Will show PermissionsScreen or await user action.")
            isLoadingPermissions = false // 如果需要用户操作，结束加载状态以显示按钮
        } else { // 麦克风已授权，但摄像头未授权
            Log.d("MainActivity", "InitialCheck: CAMERA not granted. Will show PermissionsScreen or await user action.")
            isLoadingPermissions = false // 如果需要用户操作，结束加载状态以显示按钮
        }
        // 如果有任何一个权限未授予，PermissionsScreen 会显示，用户需要点击按钮触发下面的 launchXxxPermissionRequest
    }

    private fun launchRecordAudioPermissionRequest() {
        isLoadingPermissions = true // 开始请求时标记为加载
        Log.d("MainActivity", "User Action: Launching RECORD_AUDIO permission request...")
        requestRecordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun checkAndRequestCameraPermission() {
        // 这个函数在麦克风权限授予后被调用
        val cameraHasPermission = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        isCameraPermissionGranted = cameraHasPermission
        if (cameraHasPermission) {
            Log.d("MainActivity", "CheckAfterAudio: CAMERA permission already GRANTED.")
            attemptInitializeAgentSDK()
        } else {
            Log.d("MainActivity", "CheckAfterAudio: CAMERA permission NOT granted. Launching request...")
            launchCameraPermissionRequest() // 触发摄像头权限请求
        }
    }

    private fun launchCameraPermissionRequest() {
        isLoadingPermissions = true // 开始请求时标记为加载
        Log.d("MainActivity", "User Action: Launching CAMERA permission request...")
        requestCameraLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun attemptInitializeAgentSDK() {
        // 确保两个权限都已授予才初始化
        if (isRecordAudioPermissionGranted && isCameraPermissionGranted) {
            if (!isAgentSdkInitialized) {
                Log.d("MainActivity", "Attempting to initialize AgentSDK (all permissions granted)...")
                try {
                    (applicationContext as MyApplication).initializeAgentSDK()
                    isAgentSdkInitialized = true // 标记SDK已初始化
                    Log.d("MainActivity", "AgentSDK initialization process COMPLETED.")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error initializing AgentSDK", e)
                    isAgentSdkInitialized = false // 初始化失败
                }
            } else {
                Log.d("MainActivity", "AgentSDK was already marked as initialized.")
            }
        } else {
            Log.w("MainActivity", "Attempted to initialize SDK without all permissions. RecordAudio: $isRecordAudioPermissionGranted, Camera: $isCameraPermissionGranted")
        }
        isLoadingPermissions = false // SDK 初始化尝试完成后，结束加载状态
    }

} // <--- MainActivity 类的结束花括号


// --- 顶层 Composable 函数 (在 MainActivity 类外部) ---
@Composable
fun PermissionsScreen(
    onGrantRecordAudio: () -> Unit,
    onGrantCamera: () -> Unit,
    recordAudioGranted: Boolean,
    cameraGranted: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!recordAudioGranted) {
            Text("需要麦克风权限以启用语音助手功能。")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGrantRecordAudio) {
                Text("授予麦克风权限")
            }
            Spacer(Modifier.height(16.dp))
        }
        if (!cameraGranted) {
            Text("应用可能还需要摄像头权限以提供完整功能。")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGrantCamera) {
                Text("授予摄像头权限")
            }
        }
        // 如果两个权限都已授予，但 SDK 仍在初始化 (isLoadingPermissions 可能为 true)，
        // 那么应该显示加载指示器，而不是这里的文本。
        // 不过，如果进入 PermissionsScreen，通常是至少一个权限未授予。
        if (recordAudioGranted && cameraGranted) {
            Text("所有权限已授予，请稍候...") // 或不显示任何内容，因为应该切换到加载或 HomeScreen
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZhiyunAgentRobotTheme {
        PermissionsScreen(
            onGrantRecordAudio = {},
            onGrantCamera = {},
            recordAudioGranted = false,
            cameraGranted = false
        )
    }
}

