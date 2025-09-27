plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinCompose)
    // 用这个替换：
    // id("org.jetbrains.kotlin.plugin.compose") version libs.versions.composeCompiler.get() // 或者直接写 "1.5.11"
    // id("org.jetbrains.kotlin.plugin.compose") version "1.5.8" // <--- 直接在这里应用并指定版本
}

android {
    namespace = "com.zhiyun.agentrobot" // <--- 修改这里！！！
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zhiyun.agentrobot" // <--- 包名确保修改这里
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // kotlinCompilerExtensionVersion = "1.5.8" // 确保与上面插件版本一致
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("io.coil-kt:coil-compose:2.6.0") //  请使用 Coil 的最新稳定版本

    // 1. AgentOS SDK 依赖 (来自文档)
    implementation("com.orionstar.agent:sdk:0.3.5-SNAPSHOT")
    // 2. RobotOS SDK 依赖 (您实际拥有的本地JAR)
    implementation(files("libs/robotservice_11.3.jar")) // 确保此文件名与您libs目录下的文件名完全一致
}