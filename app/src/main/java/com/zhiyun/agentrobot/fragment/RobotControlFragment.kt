// 文件路径: com/zhiyun/agentrobot/fragment/RobotControlFragment.kt

package com.zhiyun.agentrobot.fragment

import android.os.Bundle
import android.util.Log
import com.ainirobot.agent.PageAgent
import com.zhiyun.agentrobot.RobotActionFactory // ✅ 【关键】引入我们唯一的“兵工厂”
import kotlinx.coroutines.CoroutineScope

/**
 * 机器人控制前线指挥所
 *
 * 职责：
 * 1. 接收来自MainActivity的部署命令。
 * 2. 调用唯一的兵工厂 (RobotActionFactory) 来创建所有机器人动作。
 * 3. 将这些动作部署到PageAgent上。
 * 4. 为PageAgent设定最终的作战纲领 (Objective)。
 *
 * 它不再关心任何Action的创建细节或API回调处理。
 */
class RobotControlFragment(
    // 它运行所必需的“两大资源”

    private val pageAgent: PageAgent,
    private val coroutineScope: CoroutineScope
) {
    private val TAG = "RobotControlFragment"

    /**
     * 【核心方法】
     * 执行部署任务。该方法由MainActivity在机器人准备就绪时调用。
     */
    fun setupRobotActions() {
        Log.d(TAG, "指挥所收到部署命令，开始联络兵工厂并部署机器人动作...")

        // 1. 调用唯一的“兵工厂”，获取所有规范化生产的Action。
        //    【关键】将构造函数中传入的 coroutineScope 作为“能源”传递给兵工厂！
        val allActions = RobotActionFactory.createAllRobotActions(coroutineScope)

        // 2. 将所有Action一次性注册到PageAgent上。
        //    【关键】使用构造函数中传入的 pageAgent 进行部署！
        pageAgent.registerActions(allActions)


        Log.d(TAG, "不再读取旧Objective，直接设定包含所有功能的最终版本！")

        // 2. 定义我们硬件控制部分的Objective描述
        pageAgent.setObjective(
            "这是一个应用主页。你的首要任务是与用户进行友好、有帮助的对话。" +
                    "当用户的意图符合以下工具的功能时，你必须优先调用对应的工具：\n" +

                    // 【通用功能部分】 - 我们必须在这里“预知”并包含MainActivity可能定义的功能
                    "1. '查询天气': 调用 'com.zhiyun.action.QUERY_WEATHER'。\n" +
                    "2. '查询限行': 调用 'com.zhiyun.action.QUERY_TRAFFIC_RESTRICTION'。\n" +
                    "3. '播放音乐': 调用 'com.zhiyun.action.PLAY_MUSIC'，并提取'song_name'参数。\n" +
                    "4. '停止播放': 调用 'com.zhiyun.action.STOP_MUSIC'。\n" +

                    // 【硬件控制部分】
                    "5. '前进': 调用 'com.zhiyun.agentrobot.action.MOTION_GO_FORWARD'。\n" +
                    "6. '后退': 调用 'com.zhiyun.agentrobot.action.MOTION_GO_BACKWARD'。\n" +
                    "7. '左转': 调用 'com.zhiyun.agentrobot.action.MOTION_TURN_LEFT'。\n" +
                    "8. '右转': 调用 'com.zhiyun.agentrobot.action.MOTION_TURN_RIGHT'。\n" +
                    "9. '停止': 调用 'com.zhiyun.agentrobot.action.MOTION_STOP_MOVE'。\n" +
                    "10. '移动头部': 当用户说'抬头'或'低头'时, 调用 'com.zhiyun.agentrobot.action.HEAD_MOVE'。并设置参数 'vAngle' 为 '30'。当用户说'低头'时，调用相同的工具，但设置参数 'vAngle' 为 '-20'。\n" +
                    "11. '头部回正': 调用 'com.zhiyun.agentrobot.action.HEAD_RESET'。\n" +

                    "如果用户的意图与工具无关，你就以当前的全局角色身份与他自由闲聊。"
        )

        Log.i(TAG, "【最终剧本】已设定。系统完全就绪！")
    }

    // 注意：
    // 此处不再有任何 registerHeadActions(), registerMotionActions(),
    // createHeadListener(), createMotionListener(), createMotionExecutor() 等方法。
    // 所有的“制造”细节已被完美地封装在 RobotActionFactory.kt 中。
    // 这个类变得非常“薄”且职责单一。
}
