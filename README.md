# SSEBridge

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2024+-green.svg)](https://developer.android.com)

一个功能强大、易于使用的 Android SSE (Server-Sent Events) 客户端 SDK，支持 GET 和 POST 请求，提供完整的生命周期管理和事件监听功能。

## ✨ 功能特性

- 🚀 **简单易用** - Builder 模式设计，流畅的 API 调用
- 🔄 **支持 GET/POST** - 灵活支持多种 HTTP 请求方法
- 🎯 **类型安全** - 使用 ViewBinding，完全类型安全
- ⚡ **生命周期感知** - 自动管理连接，防止内存泄漏
- 🔧 **高度可配置** - 支持超时、日志、自定义拦截器等配置
- 📊 **实时状态监控** - 完整的连接状态回调
- 🎨 **Material Design 3** - 示例应用采用最新 Material Design 规范
- 🏗️ **Clean Architecture** - 遵循 SOLID 原则，代码结构清晰

## 📋 技术栈

### SDK 核心
- **Kotlin** - 100% Kotlin 编写
- **OkHttp** - 网络请求和 SSE 连接
- **Coroutines** - 异步处理
- **Lifecycle** - Android 生命周期感知

### 示例应用
- **ViewBinding** - 类型安全的视图绑定
- **Material Design 3** - 现代化的 UI 设计
- **RecyclerView** - 高效的列表展示
- **ConstraintLayout** - 灵活的布局管理

## 🏗️ 项目结构

```
SSEBridge/
├── ssebridge/                    # SDK 核心模块
│   └── src/main/java/
│       └── com/github/ssebridge/
│           ├── SSEBridge.kt           # 主入口类
│           ├── core/
│           │   └── SSEClient.kt       # SSE 客户端核心
│           ├── data/
│           │   ├── SSEConfig.kt       # 配置类
│           │   ├── SSEEvent.kt        # 事件数据类
│           │   ├── SSERequest.kt      # 请求配置
│           │   └── SSEState.kt        # 连接状态枚举
│           ├── listener/
│           │   └── SSEEventListener.kt # 事件监听器接口
│           ├── interceptor/
│           │   ├── HeaderInterceptor.kt   # 请求头拦截器
│           │   ├── LoggingInterceptor.kt  # 日志拦截器
│           │   └── SSEInterceptor.kt      # SSE 拦截器接口
│           └── util/
│               └── SSEParser.kt       # SSE 数据解析器
│
└── app/                          # 示例应用
    └── src/main/java/
        └── com/github/ssebridge/app/
            ├── MainActivity.kt            # 主界面（使用 ViewBinding）
            └── EventLogAdapter.kt         # 日志适配器（使用 ViewBinding）
```

## 🚀 快速开始

### 1. 添加依赖

在项目的 `settings.gradle.kts` 中添加模块：

```kotlin
include(":ssebridge")
```

在应用的 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation(project(":ssebridge"))
}
```

### 2. 启用 ViewBinding（可选）

如果你的应用使用 ViewBinding：

```kotlin
android {
    buildFeatures {
        viewBinding = true
    }
}
```

### 3. 基本使用

#### 创建 SSEBridge 实例

```kotlin
// 创建配置
val config = SSEConfig.Builder()
    .setConnectTimeout(30)
    .setReadTimeout(5)
    .setWriteTimeout(30)
    .setTimeUnit(TimeUnit.SECONDS)
    .setEnableLogging(true)
    .build()

// 创建事件监听器
val listener = object : SSEEventListener {
    override fun onStateChanged(state: SSEState) {
        // 处理状态变化
    }
    
    override fun onConnected() {
        // 连接成功
    }
    
    override fun onEvent(event: SSEEvent) {
        // 接收到 SSE 事件
        Log.d("SSE", "Received: ${event.data}")
    }
    
    override fun onClosed() {
        // 连接关闭
    }
    
    override fun onFailure(throwable: Throwable?) {
        // 连接失败
    }
    
    override fun onCancelled() {
        // 连接取消
    }
}

// 构建 SSEBridge
val sseBridge = SSEBridge.Builder()
    .setConfig(config)
    .setLifecycle(lifecycle)  // 自动生命周期管理
    .setEventListener(listener)
    .build()
```

#### GET 请求

```kotlin
sseBridge.connectGet(
    url = "https://example.com/sse",
    headers = mapOf("Authorization" to "Bearer your_token")
)
```

#### POST 请求

```kotlin
val requestBody = """
{
  "model": "gpt-4",
  "messages": [
    {"role": "user", "content": "Hello"}
  ],
  "stream": true
}
"""

sseBridge.connectPost(
    url = "https://api.example.com/chat",
    body = requestBody,
    headers = mapOf(
        "Authorization" to "Bearer your_token",
        "Content-Type" to "application/json"
    )
)
```

#### 断开连接

```kotlin
sseBridge.disconnect()
```

## 📱 示例应用

示例应用提供了完整的 SSE 连接测试功能，展示了如何使用 ViewBinding 和 Material Design 3：

### 主要功能

1. **连接测试**
   - GET 请求连接
   - POST 请求连接（支持自定义请求体）
   - 实时状态显示

2. **事件日志**
   - 实时显示接收到的 SSE 事件
   - 时间戳记录
   - 颜色编码状态（成功/失败/警告）

3. **配置管理**
   - 自定义 URL
   - 自定义请求体（JSON 格式）
   - 自定义超时设置

### 使用 ViewBinding 的示例

#### Activity

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 直接访问视图，无需 findViewById
        binding.connectButton.setOnClickListener {
            val url = binding.urlEditText.text.toString()
            sseBridge.connectGet(url, emptyMap())
        }
    }
}
```

#### RecyclerView Adapter

```kotlin
class EventLogAdapter : RecyclerView.Adapter<EventLogAdapter.EventLogViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventLogViewHolder {
        val binding = ItemEventLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventLogViewHolder(binding)
    }
    
    class EventLogViewHolder(
        private val binding: ItemEventLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(log: EventLog) {
            binding.logTypeText.text = log.type
            binding.logMessageText.text = log.message
        }
    }
}
```

## ⚙️ 配置选项

### SSEConfig

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `connectTimeout` | Long | 1 | 连接超时时间 |
| `readTimeout` | Long | 2 | 读取超时时间 |
| `writeTimeout` | Long | 1 | 写入超时时间 |
| `timeUnit` | TimeUnit | MINUTES | 时间单位 |
| `enableLogging` | Boolean | false | 是否启用日志 |

### SSEState

连接状态枚举：

- `IDLE` - 空闲状态
- `CONNECTING` - 连接中
- `CONNECTED` - 已连接
- `CLOSED` - 已关闭
- `FAILED` - 连接失败
- `CANCELLED` - 已取消

### SSEEvent

SSE 事件数据：

```kotlin
data class SSEEvent(
    val id: String?,      // 事件 ID
    val type: String?,    // 事件类型
    val data: String,     // 事件数据
    val retry: Long?      // 重试间隔
)
```

## 🔧 高级用法

### 自定义拦截器

```kotlin
class CustomInterceptor : SSEInterceptor {
    override fun intercept(chain: SSEInterceptor.Chain): Response {
        val request = chain.request()
        // 自定义请求处理
        return chain.proceed(request)
    }
}

val sseBridge = SSEBridge.Builder()
    .setConfig(config)
    .addInterceptor(CustomInterceptor())
    .build()
```

### 生命周期管理

通过设置 Lifecycle，SSEBridge 会自动：
- 在 Activity/Fragment onDestroy 时断开连接
- 防止内存泄漏
- 管理协程作用域

```kotlin
val sseBridge = SSEBridge.Builder()
    .setLifecycle(lifecycle)  // 传入 Activity/Fragment 的 lifecycle
    .build()
```
