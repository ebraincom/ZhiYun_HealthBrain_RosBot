package com.zhiyun.agentrobot

import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import android.util.Log
import android.widget.TextView
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
import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.action.Action
import com.ainirobot.agent.action.ActionExecutor
import androidx.lifecycle.lifecycleScope // 非常重要，用于执行协程任务
import com.zhiyun.agentrobot.data.TrafficRepository // 引入我们的“后勤部门”
import kotlinx.coroutines.launch // 引入协程启动器
import com.ainirobot.agent.PageAgent
import com.ainirobot.agent.base.Parameter
import com.ainirobot.agent.base.ParameterType
import android.content.Intent
import com.zhiyun.agentrobot.data.selectableRoles
import com.zhiyun.agentrobot.ui.guide.GuideActivity
import com.zhiyun.agentrobot.ui.screens.UserProfile



// import com.ainirobot.agent.AgentCore // 如果您的 HomeScreen 或其他地方确实需要，再取消注释

class MainActivity : ComponentActivity() {


    // --- 状态变量声明区 ---
    private var isRecordAudioPermissionGranted by mutableStateOf(false)
    private var isCameraPermissionGranted by mutableStateOf(false)
    private var isAgentSdkInitialized by mutableStateOf(false)
    private var isLoadingPermissions by mutableStateOf(true) // 用于初始权限检查和SDK加载
    private lateinit var pageAgent: PageAgent // 改为 lateinit 初始化
    private val trafficRepository = TrafficRepository()
    private lateinit var tvWeather: TextView // 【确保这个变量已经定义】
    private val weatherDataState: MutableState<String> = mutableStateOf("天气获取中...")


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
            // setContentView(R.layout.activity_main)

            // 2. 严格按照“视图 -> 依赖 -> 配置 -> 运行”的顺序执行,调用视图初始化方法
            // initViews()
            initDependencies()

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

            // --- 获取屏幕信息的代码放在这里 END -
            // ▼▼▼ 在这里定义和注册我们的Action ▼▼▼
            // defineAndRegisterActions()

            setContent {
                Log.d("MainActivity", "setContent Composable lambda: ENTER")
                ZhiyunAgentRobotTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (isAgentSdkInitialized) {
                            Log.d(
                                "MainActivity",
                                "UI: AgentSDK is initialized, rendering HomeScreen"
                            )
                            HomeScreen(
                                weatherDataState = weatherDataState,
                                onMoreConsultClick = {
                                    // 创建一个意图，明确指定要启动 GuideActivity
                                    val intent = Intent(this@MainActivity, GuideActivity::class.java)
                                    // 执行启动！
                                    startActivity(intent)
                                    Log.i("MainActivity_Action", "onMoreConsultClick: Starting GuideActivity.")
                                },
                                userProfile = UserProfile(name = "王阿姨") // 或者 UserProfile() 如果它有默认值

                            )
                        } else if (isLoadingPermissions) {
                            Log.d("MainActivity", "UI: Loading permissions or SDK...")
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(Modifier.height(8.dp))
                                    Text("正在准备应用...")
                                }
                            }
                        } else {
                            Log.d(
                                "MainActivity",
                                "UI: Permissions NOT fully granted or SDK NOT initialized, showing PermissionsScreen"
                            )
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
            updateHomepageWeather()
        }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "Activity onResume: Registering Actions and setting Objective.")
        // 在 onResume 中调用，确保 PageAgent 处于活跃状态
        defineAndRegisterActions()
    }

    override fun onStart() {
        super.onStart()
        // 从 applicationContext 中获取 MyApplication 实例
        val myApp = applicationContext as MyApplication
        // 从全局角色列表中找出我们的默认角色 "智芸康养小助手"
        val defaultRole = selectableRoles.find { it.name == "智芸康养小助手" }

        if (defaultRole != null) {
            // 【核心】: 每次回到主页，都无条件地、强制地用默认角色覆盖当前角色！
            myApp.switchAgentRole(defaultRole)
            Log.i("MainActivity_LifeCycle", "onStart: Agent role FORCE-RESTORED to default '智芸康养小助手'.")
        } else {
            Log.w("MainActivity_LifeCycle", "onStart: Could not find default role to restore!")
        }
    }

        // --- 成员函数声明区 (确保这些函数在 MainActivity 类的花括号内，与 onCreate 并列) ---

     private fun defineAndRegisterActions() {
         // === 【升级】作战单位1：天气查询Action ===

         // 1. 定义Action执行时需要的参数
         val cityParameter = Parameter(
             name = "city", // 参数名叫 'city'
             type = ParameterType.STRING, // 参数类型是字符串
             desc = "用户想要查询的城市名称，例如'北京'、'上海'",
             required = true // 这个参数是必须的！
         )

         // === 作战单位2：限行查询Action (我们暂时保持不变，作为对比) ===
         val queryWeatherAction = Action(
             name = "com.zhiyun.action.QUERY_WEATHER",
             displayName = "查询天气",
             desc = "查询指定城市当天的天气信息。",
             // 2. 【核心升级】将参数描述注册到Action中
             parameters = listOf(cityParameter),
             executor = object : ActionExecutor {
                 override fun onExecute(action: Action, params: Bundle?): Boolean {
                     lifecycleScope.launch {
                         try {
                             // 3. 【核心升级】从params中获取大模型传来的城市名
                             // 如果大模型没提供，我们就给一个默认值“北京”
                             val city = params?.getString("city") ?: "北京"

                             // 4. 将动态获取的城市名用于网络请求！
                             val resultInfo = trafficRepository.getWeatherInfo(city)

                             AgentCore.tts(resultInfo)
                         } catch (e: Exception) {
                             AgentCore.tts("抱歉，在执行天气查询时出错了。")
                             e.printStackTrace()
                         } finally {
                             action.notify()
                         }
                     }
                     return true
                 }
             }
         )

         // 2. 定义一个用于查询限行的Action
         val queryRestrictionAction = Action(
             name = "com.zhiyun.action.QUERY_TRAFFIC_RESTRICTION", // Action的唯一名称
             displayName = "查询限行", // 显示名称
             desc = "查询指定城市当天的机动车尾号限行信息，如果用户没有说城市，就默认查询北京。", // 给大模型看的描述，告诉它这个Action能干什么
             parameters = emptyList(), // 我们暂时不需要参数，后面可以扩展，给个为空，以便通过编译
             executor = object : ActionExecutor {
                 override fun onExecute(action: Action, params: Bundle?): Boolean {
                     // 2. 在协程中执行耗时任务（网络请求）
                     lifecycleScope.launch {
                         try {
                             // 3. 调用“后勤部门”获取数据
                             val restrictionInfo = trafficRepository.getRestrictionInfo("beijing")

                             // 4. 使用TTS播报结果
                             AgentCore.tts(restrictionInfo)

                         } catch (e: Exception) {
                             // 异常处理，如果获取失败，也给用户一个反馈
                             AgentCore.tts("抱歉，查询限行信息失败，请稍后再试。")
                             e.printStackTrace() // 在后台打印错误日志，方便调试
                         } finally {
                             // 5. 无论成功还是失败，都必须通知Agent任务已结束
                             action.notify()
                         }
                     }
                     // 返回true，表示我们已经接管了这个任务
                     return true
                 }
             }
         )

        // 将我们定义好的Action统一注册到PageAgent中
         pageAgent.registerAction(queryWeatherAction)
         pageAgent.registerAction(queryRestrictionAction)
         // === 【升级】作战目标 ===
         pageAgent.setObjective(
             "你是一个生活助手。" +
                     "当用户询问天气时，你应该从用户问题中提取'city'参数，并调用工具 'com.zhiyun.action.QUERY_WEATHER'。" +
                     "如果用户没有明确说出城市，你应该默认使用'北京'。" +
                     "当用户询问限行时，你应该调用工具 'com.zhiyun.action.QUERY_RESTRICTION'。"
         )
     }

    // 这是新增的2个方法，专门用于异步获取天气并更新主页上的 TextView。
    // 它使用 lifecycleScope 来确保协程的生命周期安全。
    // */第一个新增如下
    private fun initDependencies() {
        pageAgent = PageAgent(this)
        Log.d("MainActivity", "initDependencies: pageAgent initialized.")
    }

    //第二个新增如下
    private fun updateHomepageWeather() {
        // 使用 lifecycleScope 启动协程，它会与 Activity 的生命周期绑定
        lifecycleScope.launch {
            Log.d("MainActivity", "updateHomepageWeather: Starting weather update coroutine.")
            // 1. 在后台线程中调用我们之前在 Repository 中新增的方法
            val weatherInfo = trafficRepository.getWeatherInfoForHomepage("北京")

            // 2. 切换回主线程来安全地更新UI组件
            //  withContext(Dispatchers.Main) {
            if (weatherInfo != null) {
                // 3. 如果成功获取到数据，就更新 TextView 的文本
                weatherDataState.value = "${weatherInfo.weatherText} ${weatherInfo.temperature}℃"
                Log.d(
                    "MainActivity",
                    "updateHomepageWeather: State updated with: ${weatherDataState.value}"
                )
            } else {
                weatherDataState.value = "天气获取失败"
                Log.w(
                    "MainActivity",
                    "updateHomepageWeather: Failed to get weather info, State updated to error state.")
            }
        }
    }


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

