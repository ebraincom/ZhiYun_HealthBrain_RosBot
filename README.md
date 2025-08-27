# 智芸康养机器人 Android APP

## 项目简介

智芸康养机器人是基于机器人载体终端的AI综合智能体产品，其目标为康养社区、康养机构和康养服务单位提供一款智能化终端产品。是基于智芸康养大脑平台提供的智能化能力的具体表现。

## 技术栈

- **编程语言**: Kotlin
- **构建工具**: Gradle
- **开发平台**: Android Studio
- **目标平台**: Android (API 26+)
- **UI框架**: Jetpack Compose
- **SDK版本**: AgentOS SDK v0.3.5, RobotOS SDK v11.3C

## 功能模块

### 1. 购物功能
- 商品搜索和浏览
- 购物车管理
- 订单查看
- 商品推荐

### 2. 记事本功能
- 添加、编辑、删除笔记
- 搜索笔记
- 语音输入支持

### 3. 定位功能
- 当前位置显示
- 保存常用位置
- 导航服务

### 4. 回忆录功能
- 记录美好回忆
- 编辑和查看回忆
- 语音记录支持

### 5. 用药提醒功能
- 设置用药提醒
- 管理药品信息
- 用药记录查看

### 6. 紧急求助功能
- 一键紧急求助
- 紧急联系人管理
- 快速拨号和短信

### 7. 计划提醒功能
- 添加日常计划
- 设置提醒时间
- 计划管理

## 项目结构

```
app/
├── src/main/
│   ├── java/com/zhiyun/care/
│   │   ├── MainActivity.kt                 # 主界面
│   │   ├── ZhiyunCareApplication.kt       # 应用类
│   │   └── ui/                            # UI页面
│   │       ├── shopping/                  # 购物页面
│   │       ├── note/                      # 记事本页面
│   │       ├── placement/                 # 定位页面
│   │       ├── memoirs/                   # 回忆录页面
│   │       ├── medication/                # 用药提醒页面
│   │       ├── sos/                       # 紧急求助页面
│   │       └── planreminder/              # 计划提醒页面
│   ├── res/                               # 资源文件
│   └── assets/
│       └── actionRegistry.json           # AgentOS配置
├── libs/
│   └── robotservice_11.3.jar             # RobotOS SDK
└── build.gradle                          # 构建配置
```

## 开发环境要求

- Android Studio Arctic Fox 或更高版本
- JDK 11
- Android SDK API 26+
- Gradle 7.0+

## 安装和运行

1. 克隆项目到本地
2. 在Android Studio中打开项目
3. 同步Gradle依赖
4. 连接Android设备或启动模拟器
5. 点击运行按钮

## AgentOS SDK集成

项目集成了AgentOS SDK v0.3.5，提供以下功能：

- **智能对话**: 基于大语言模型的自然语言交互
- **语音识别**: ASR语音转文字
- **语音合成**: TTS文字转语音
- **Action系统**: 自定义功能动作
- **页面信息管理**: 动态更新页面上下文

### 配置说明

1. **actionRegistry.json**: 配置Action注册表
2. **AppAgent**: 应用级智能体，处理全局功能
3. **PageAgent**: 页面级智能体，处理页面特定功能

## RobotOS SDK集成

项目集成了RobotOS SDK v11.3C，提供机器人底层控制功能：

- 运动控制
- 导航定位
- 传感器数据
- 视觉识别
- 系统服务

## 语音交互

用户可以通过语音指令使用各种功能：

- "打开购物功能"
- "添加用药提醒"
- "记录回忆"
- "紧急求助"
- "设置计划提醒"

## 界面设计

- 采用Material Design 3设计语言
- 使用Jetpack Compose构建现代化UI
- 适配横屏显示（1920×1080px）
- 适合老年人使用的简洁界面

## 权限说明

应用需要以下权限：

- `RECORD_AUDIO`: 语音识别
- `ACCESS_FINE_LOCATION`: 定位服务
- `CALL_PHONE`: 拨打电话
- `SEND_SMS`: 发送短信
- `INTERNET`: 网络连接

## 开发规范

- 遵循Kotlin编码规范
- 使用Jetpack Compose构建UI
- 集成AgentOS SDK进行智能交互
- 集成RobotOS SDK进行机器人控制
- 异步操作使用协程处理

## 版本信息

- **当前版本**: 1.0.0
- **AgentOS SDK**: v0.3.5
- **RobotOS SDK**: v11.3C
- **目标设备**: 猎户星空豹小秘MINI机器人

## 技术支持

如有问题，请参考：
- [AgentOS SDK文档](Agent/v0.3.5/AgentOS_SDK_Doc_v0.3.5.md)
- [RobotOS API文档](Robot/v11.3C/RobotAPI.md)
- [常见问题FAQ](FAQ.md)
