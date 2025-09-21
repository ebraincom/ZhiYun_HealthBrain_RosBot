// settings.gradle.kts
pluginManagement {
    repositories {
        google() // 暂时移除 content 块，让 google() 仓库可以提供任何它有的插件
        mavenCentral()
        gradlePluginPortal()
    }
}
// ... 后续的 dependencyResolutionManagement 等内容保持不变 ...
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // START: AgentOS SDK Maven Repository Configuration (来自您提供的文档)
        maven {
            url = uri("https://npm.ainirobot.com/repository/maven-public/")
            credentials {
                username = "agentMaven"
                password = "agentMaven"
            }
        }
    }
}

rootProject.name = "ZhiYun_AgentRobot"
include(":app")