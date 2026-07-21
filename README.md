# 鹿录 · AI 制作的 Android 应用

一款完全离线的 Android 撸管次数记录应用，使用 Kotlin、Jetpack Compose、Room 和 Material 3 构建。

> **AI 制作声明：** 本项目在用户需求指导下，产品设计、界面设计、代码实现、自动化测试、问题修复和 APK 构建均由 **OpenAI Codex AI** 完成。

## 功能

- 今日快速记录与撤销，数据即时保存
- 月历查看，允许修改今天及之前 6 天
- 累计总次数、本周、本月、近 30 天日均与单日最高统计
- 可点击的近 30 天趋势图
- 系统深浅色模式
- 无账号、无网络权限、无广告与分析服务

## 构建要求

- JDK 17
- Android SDK 37
- Android Studio 或 Gradle 9.5

在 Android Studio 中打开项目并同步依赖，随后运行 `assembleDebug`。调试 APK 生成在：

`app/build/outputs/apk/debug/app-debug.apk`

## 技术栈

- Kotlin 与 Jetpack Compose
- Material 3 与 Navigation Compose
- Room 本地数据库
- ViewModel、Repository 与 StateFlow
- Android API 26–37

## AI 开发说明

这是一个 AI 驱动开发示例：用户负责提出真实需求和确认产品方向，OpenAI Codex AI 负责将需求转化为可运行的 Android 工程，并完成实现、测试和构建验证。仓库中的应用代码不是人工逐行编写的。

## 隐私

记录保存在应用私有 Room 数据库中。项目没有声明网络权限，并关闭 Android 云备份和设备迁移备份；卸载应用会删除记录。
