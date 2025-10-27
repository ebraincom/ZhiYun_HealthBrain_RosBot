package com.zhiyun.agentrobot

import android.Manifest
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.OnTranscribeListener
import com.ainirobot.agent.PageAgent
import com.ainirobot.agent.action.Action
import com.ainirobot.agent.action.ActionExecutor
import com.ainirobot.agent.base.Parameter
import com.ainirobot.agent.base.ParameterType
import com.ainirobot.agent.base.Transcription
import com.zhiyun.agentrobot.data.TrafficRepository
import com.zhiyun.agentrobot.ui.guide.GuideActivity
import com.zhiyun.agentrobot.ui.screens.HomeScreen
import com.zhiyun.agentrobot.ui.screens.UserProfile
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import kotlinx.coroutines.launch
import com.ainirobot.agent.coroutine.AOCoroutineScope
import com.zhiyun.agentrobot.data.network.MediaApiClient
import androidx.compose.ui.platform.LocalContext
import com.zhiyun.agentrobot.ui.family.FamilyMemberActivity


/**
 * 主页Activity
 * 在新战略下，它将顺从并正确响应全局角色的变化。
 */
class MainActivity : ComponentActivity() {

    // --- 状态变量声明区 ---
    private var isRecordAudioPermissionGranted by mutableStateOf(false)
    private var isCameraPermissionGranted by mutableStateOf(false)
    private var isAgentSdkInitialized by mutableStateOf(false)
    private var isLoadingPermissions by mutableStateOf(true)
    private lateinit var pageAgent: PageAgent
    private val trafficRepository = TrafficRepository()
    private val weatherDataState: MutableState<String> = mutableStateOf("天气获取中...")

    private val apiService by lazy { MediaApiClient.mediaApiService }
    val mediaApiService = MediaApiClient.mediaApiService


    // --- ActivityResultLaunchers 声明区 ---
    private val requestRecordAudioLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            Log.d("MainActivity", "RECORD_AUDIO permission result: $isGranted")
            isRecordAudioPermissionGranted = isGranted
            if (isGranted) {
                // 麦克风权限通过后，检查并请求摄像头权限
                checkAndRequestCameraPermission()
            } else {
                isLoadingPermissions = false // 结束加载状态
            }
        }

    private val requestCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            Log.d("MainActivity", "CAMERA permission result: $isGranted")
            isCameraPermissionGranted = isGranted
            if (isGranted) {
                // 摄像头权限也通过后，初始化 SDK
                attemptInitializeAgentSDK()
            } else {
                isLoadingPermissions = false // 结束加载状态
            }
        }

    // --- onCreate 方法 ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MainActivity_Final", "onCreate: Initializing.")


        initDependencies()
        setupListeners()

        setContent {
            ZhiyunAgentRobotTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // --- 关键情报：获取Compose世界里的上下文，这是启动新Activity最安全、最标准的方式！ ---
                    val context = LocalContext.current

                    when {
                        isAgentSdkInitialized -> HomeScreen(
                            weatherDataState = weatherDataState,
                            onMoreConsultClick = {
                                startActivity(Intent(this@MainActivity, GuideActivity::class.java))
                                Log.i("MainActivity_Final", "onMoreConsultClick: Starting GuideActivity.")
                            },
                            // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
                            // ★★★【跃迁信标已嵌入】：这里是唯一的、需要您添加/修改的核心代码！★★★
                            // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
                            onMemoryShowcaseClick = {
                                // 【设计哲学】：点击事件的唯一职责，就是用最经典的方式启动一个新Activity！
                                Log.d("MainActivity_Final", "onMemoryShowcaseClick: 即将启动 FamilyMemberActivity。")

                                // 【跃迁指令】：创建指向我们新世界“FamilyMemberActivity”的意图(Intent)。
                                val intent = Intent(context, FamilyMemberActivity::class.java)

                                // 【执行跃迁】：启动Activity，完成从主世界到新世界的空间跳跃！
                                context.startActivity(intent)
                            },
                            // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
                            // ★★★【嵌入点结束】：以上是您需要添加的全部内容。                 ★★★
                            // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

                            userProfile = UserProfile(name = "王阿姨")
                        )
                        isLoadingPermissions -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(8.dp))
                                Text("正在准备应用...")
                            }
                        }
                        else -> PermissionsScreen(
                            onGrantRecordAudio = { launchRecordAudioPermissionRequest() },
                            onGrantCamera = { launchCameraPermissionRequest() },
                            recordAudioGranted = isRecordAudioPermissionGranted,
                            cameraGranted = isCameraPermissionGranted
                        )
                    }
                }
            }
        }

        checkInitialPermissions()
        updateHomepageWeather()
        Log.i("MainActivity_Final", "onCreate: FINISHED.")
    }

    // ▼▼▼【最终、也是唯一的改造核心】▼▼▼
    override fun onResume() {
        super.onResume()
        if (!isAgentSdkInitialized) return

        Log.i("MainActivity_Final", "onResume: Home page is visible.")

        // 1. 【移除“思想钢印”】
        // 我们不再强制恢复默认角色。主页将“顺从”由GuideActivity设定的全局角色。
        Log.i("MainActivity_Final", "Strategy Update: No longer forcing default role. Accepting global role.")

        // 2. 【保留必要职责】为当前页面注册工具并设置Objective
        defineAndRegisterActions()

        // 3. 【保留激活状态】确保主页的语音交互可用
        AgentCore.isDisablePlan = false
        Log.i("MainActivity_Final", "Agent mode is ACTIVE. Ready for commands on home page.")
    }
    // ▲▲▲【改造结束】▲▲▲

    override fun onStart() {
        super.onStart()
        Log.i("MainActivity_Final", "onStart: Lifecycle event.")
    }

    // --- defineAndRegisterActions 方法 ---
    private fun defineAndRegisterActions() {

        val cityParameter = Parameter("city", ParameterType.STRING, "用户想要查询的城市名称，例如'北京'、'上海'", true)

        val queryWeatherAction = Action(
            "com.zhiyun.action.QUERY_WEATHER", "查询天气", "查询指定城市当天的天气信息。",
            listOf(cityParameter),
            object : ActionExecutor {
                override fun onExecute(action: Action, params: Bundle?): Boolean {
                    lifecycleScope.launch {
                        try {
                            val city = params?.getString("city") ?: "北京"
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
        val queryRestrictionAction = Action(
            "com.zhiyun.action.QUERY_TRAFFIC_RESTRICTION", "查询限行", "查询指定城市当天的机动车尾号限行信息，如果用户没有说城市，就默认查询北京。",
            emptyList(),
            object : ActionExecutor {
                override fun onExecute(action: Action, params: Bundle?): Boolean {
                    lifecycleScope.launch {
                        try {
                            val restrictionInfo = trafficRepository.getRestrictionInfo("beijing")
                            AgentCore.tts(restrictionInfo)
                        } catch (e: Exception) {
                            AgentCore.tts("抱歉，查询限行信息失败，请稍后再试。")
                            e.printStackTrace()
                        } finally {
                            action.notify()
                        }
                    }
                    return true
                }
            }
        )
        // --- 音乐播放Action（最终回溯正确版本Final Ultimatum） ---
        val playMusicAction = Action(
            "com.zhiyun.action.PLAY_MUSIC",
            "播放音乐",
            "根据用户指定的歌名，从我们自己的服务器搜索并播放音乐。",
            listOf(
                Parameter("song_name", ParameterType.STRING, "歌曲名称，例如'七里香'、'晴天'等", true)
            ),
            object : ActionExecutor {
                override fun onExecute(action: Action, params: Bundle?): Boolean {
                    val songName = params?.getString("song_name")
                    if (songName.isNullOrEmpty()) {
                        AgentCore.tts("请告诉我您想听什么歌呢？")
                        // 【失败路径1】: 只做TTS提示，不调用notify()！
                        return true
                    }

                    lifecycleScope.launch {
                        try {
                            Log.d("playMusicAction", "正在为歌曲'$songName'请求网络...")
                            val response = mediaApiService.searchMedia(keyword = songName)
                            if (response.code == 0 && response.data?.items?.isNotEmpty() == true) {
                                val firstSong = response.data.items[0]
                                val songUrl = firstSong.url
                                val artist = firstSong.artist ?: "未知艺术家"

                                AgentCore.tts("好的，为您播放${artist}的${firstSong.title}")
                                Log.i("playMusicAction", "歌曲找到: ${firstSong.title}, URL: $songUrl")

                                MusicPlayerEngine.playMusic(
                                    url = songUrl,
                                    onCompletion = {
                                        Log.i("playMusicAction", "歌曲播放完成。")
                                        // ✅【唯一成功路径】: 在播放完成的回调中，调用无参数的 notify()！
                                        action.notify()
                                    },
                                    onError = { errorMsg ->
                                        Log.e("playMusicAction", "播放器错误: $errorMsg")
                                        AgentCore.tts("哎呀，这首歌好像播放失败了，换一首试试？")
                                        // 【失败路径2】: 只做TTS提示，不调用notify()！
                                    }
                                )
                            } else {
                                Log.w("playMusicAction", "服务器未找到歌曲 '$songName'。响应: ${response.message}")
                                AgentCore.tts("抱歉，我的曲库里暂时还没有这首歌。")
                                // 【失败路径3】: 只做TTS提示，不调用notify()！
                            }
                        } catch (e: Exception) {
                            Log.e("playMusicAction", "网络请求异常", e)
                            AgentCore.tts("网络好像开小差了，请稍后再试试吧。")
                            // 【失败路径4】: 只做TTS提示，不调用notify()！
                        }
                    }
                    return true
                }
            }
        )

        // ▼▼▼【“停止播放”Action】▼▼▼
        val stopMusicAction = Action(    name = "com.zhiyun.agentrobot.action.STOP_MUSIC",
            displayName = "停止播放音乐",
            desc = "当用户想要停止当前正在播放的音乐时，调用此工具。例如用户说'停止播放'、'别唱了'、'安静'等。",
            parameters = emptyList(),
            executor = object : ActionExecutor {
                override fun onExecute(action: Action, params: Bundle?): Boolean {
                    // 【修正！】将所有操作放入协程中，以正确调用挂起函数 ttsSync
                    AOCoroutineScope.launch {
                        Log.i("MainActivity", "STOP_MUSIC Action triggered.")

                        // 在协程环境中调用 ttsSync，编译器将不再报错
                        AgentCore.ttsSync("好的")

                        MusicPlayerEngine.stopMusic()

                        // 在所有操作完成后调用 notify()
                        action.notify()
                    }
                    // 立即返回 true
                    return true
                }
            }
        )
// ▲▲▲【修改结束】▲▲▲



        pageAgent.registerAction(queryWeatherAction)
        pageAgent.registerAction(queryRestrictionAction)
        pageAgent.registerAction(playMusicAction) // <-- 改为静态注册，注释保留
        pageAgent.registerAction(stopMusicAction) // <-- 改为静态注册，注释保留

        // Objective的描述变得更通用，不再强行绑定“小助手”
        pageAgent.setObjective(
            "这是一个应用主页。你的首要任务是与用户进行友好、有帮助的对话。" +
                    "当用户的意图符合以下工具的功能时，你必须优先调用对应的工具：\n" +
                    "1. '查询天气'：调用'com.zhiyun.action.QUERY_WEATHER'工具。\n" +
                    "2. '查询限行'：调用'com.zhiyun.action.QUERY_TRAFFIC_RESTRICTION'工具。\n" +
                    "3. '播放音乐'：调用'com.zhiyun.action.PLAY_MUSIC'工具，并从用户的话语中里提取'song_name'参数,不局限七里香。\n" +
                    "4. '停止播放'：调用'com.zhiyun.action.STOP_MUSIC'工具。\n" +
                    "如果用户的意图与工具无关，你就以当前的全局角色身份与他自由闲聊。"
        )
        Log.i("MainActivity_Final", "Home page actions and a more GENERIC objective have been set.")
    }

    // --- 所有辅助函数，逐行补全 ---
    private fun setupListeners() {
        pageAgent.setOnTranscribeListener(object : OnTranscribeListener {
            override fun onASRResult(transcription: Transcription): Boolean {
                if (transcription.final) Log.i("MainActivity_Final", "ASR: ${transcription.text}")
                return false
            }
            override fun onTTSResult(transcription: Transcription): Boolean = false
        })
        Log.i("MainActivity_Final", "OnTranscribeListener has been set for home page.")
    }

    private fun initDependencies() {
        pageAgent = PageAgent(this)
        Log.i("MainActivity_Final", "initDependencies: pageAgent initialized.")
    }

    private fun updateHomepageWeather() {
        lifecycleScope.launch {
            Log.d("MainActivity_Final", "updateHomepageWeather: Starting weather update.")
            try {
                val weatherInfo = trafficRepository.getWeatherInfoForHomepage("北京")
                weatherDataState.value = weatherInfo?.let { "${it.weatherText} ${it.temperature}℃" } ?: "天气获取失败"
                Log.d("MainActivity_Final", "Weather updated: ${weatherDataState.value}")
            } catch (e: Exception) {
                weatherDataState.value = "天气获取异常"
                Log.e("MainActivity_Final", "Error updating weather", e)
            }
        }
    }

    private fun checkInitialPermissions() {
        isLoadingPermissions = true
        val recordAudioHasPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val cameraHasPermission = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        isRecordAudioPermissionGranted = recordAudioHasPermission
        isCameraPermissionGranted = cameraHasPermission

        if (recordAudioHasPermission && cameraHasPermission) {
            Log.d("MainActivity_Final", "InitialCheck: Both permissions already GRANTED.")
            attemptInitializeAgentSDK()
        } else {
            Log.d("MainActivity_Final", "InitialCheck: Permissions not fully granted. Will show PermissionsScreen.")
            isLoadingPermissions = false
        }
    }

    private fun launchRecordAudioPermissionRequest() {
        isLoadingPermissions = true
        Log.d("MainActivity_Final", "Launching RECORD_AUDIO permission request...")
        requestRecordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun checkAndRequestCameraPermission() {
        val cameraHasPermission = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (cameraHasPermission) {
            Log.d("MainActivity_Final", "CheckAfterAudio: CAMERA permission already GRANTED.")
            isCameraPermissionGranted = true
            attemptInitializeAgentSDK()
        } else {
            launchCameraPermissionRequest()
        }
    }

    private fun launchCameraPermissionRequest() {
        isLoadingPermissions = true
        Log.d("MainActivity_Final", "Launching CAMERA permission request...")
        requestCameraLauncher.launch(Manifest.permission.CAMERA)
    }
    // ▼▼▼【核心改造 2/2：创建“包容性录入”对话框的完整实现】▼▼▼




    private fun attemptInitializeAgentSDK() {
        if (isRecordAudioPermissionGranted && isCameraPermissionGranted) {
            if (!isAgentSdkInitialized) {
                Log.d("MainActivity_Final", "Attempting to initialize AgentSDK...")
                try {
                    (applicationContext as MyApplication).initializeAgentSDK()
                    isAgentSdkInitialized = true
                    Log.i("MainActivity_Final", "AgentSDK initialization process COMPLETED.")
                } catch (e: Exception) {
                    Log.e("MainActivity_Final", "Error initializing AgentSDK", e)
                    isAgentSdkInitialized = false
                }
            }
        }
        isLoadingPermissions = false
    }
}

// --- Composable 函数 (无修改) ---
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
        }
        if (!cameraGranted && recordAudioGranted) { // 只有在麦克风授权后才提示摄像头
            Spacer(Modifier.height(16.dp))
            Text("应用可能还需要摄像头权限以提供完整功能。")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGrantCamera) {
                Text("授予摄像头权限")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZhiyunAgentRobotTheme {
        PermissionsScreen(onGrantRecordAudio = {}, onGrantCamera = {}, recordAudioGranted = false, cameraGranted = false)
    }
}
