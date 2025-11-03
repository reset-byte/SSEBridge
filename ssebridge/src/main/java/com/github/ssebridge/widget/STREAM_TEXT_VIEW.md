# StreamTextView - 渐变流式文本展示组件

## 简介

StreamTextView 是一个自定义的 Android TextView 组件，专门用于实现流式文本展示效果。它支持逐字显示、文字渐变色和光标闪烁动画，非常适合用于 AI 对话、SSE 流式数据展示等场景。

## 功能特性

### 三个展示状态

1. **IDLE（初始等待）**
   - 展示闪烁的光标
   - 等待开始流式展示

2. **STREAMING（展示中）**
   - 逐字展示文案
   - 文字带有渐变色效果
   - 末尾显示闪烁光标

3. **COMPLETED（展示结束）**
   - 展示完整文案
   - 隐藏光标

### 核心功能

- ✅ **逐字显示**：使用 Handler 定时器实现周期性文字更新
- ✅ **文字渐变**：自定义 LinearGradientSpan 实现渐变色效果
- ✅ **光标闪烁**：自定义 CursorSpan + ValueAnimator 实现光标动画
- ✅ **可自定义**：支持自定义颜色、速度、光标样式
- ✅ **回调监听**：提供开始、进度、完成事件回调

## 技术实现

### 1. LinearGradientSpan - 文字渐变

```kotlin
class LinearGradientSpan(
    private val containingText: String,
    private val gradientStart: Int,
    private val gradientEnd: Int,
    @ColorInt private val startColorInt: Int,
    @ColorInt private val endColorInt: Int
) : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint?) {
        // 使用 LinearGradient 为画笔设置渐变色
        val lineGradient = LinearGradient(...)
        tp.shader = lineGradient
    }
}
```

通过继承 `CharacterStyle` 并实现 `UpdateAppearance` 接口，重写 `updateDrawState()` 方法来设置文字的渐变效果。

### 2. CursorSpan - 光标绘制

```kotlin
class CursorSpan(
    private val context: Context,
    @ColorInt private val cursorColor: Int
) : ReplacementSpan() {
    var alpha: Float = 1f
    
    override fun getSize(...): Int {
        // 返回光标宽度
        return cursorWidth
    }
    
    override fun draw(...) {
        // 绘制圆角矩形作为光标
        canvas.drawRoundRect(...)
    }
}
```

通过继承 `ReplacementSpan` 实现自定义绘制，使用 `ValueAnimator` 动态更新 alpha 值实现闪烁效果。

### 3. StreamTextView - 流式展示控制

```kotlin
class StreamTextView : AppCompatTextView {
    private val streamRunnable: Runnable = object : Runnable {
        override fun run() {
            if (currentIndex < fullText.length) {
                currentIndex++
                updateDisplayText()
                streamHandler.postDelayed(this, streamDelayMillis)
            } else {
                finishStreaming()
            }
        }
    }
}
```

使用 Handler 的 `postDelayed()` 方法周期性更新 TextView 内容，结合 SpannableStringBuilder 实现渐变和光标效果。

## 使用方法

### 基本使用

```xml
<com.github.ssebridge.widget.StreamTextView
    android:id="@+id/streamTextView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textSize="16sp"
    android:padding="16dp" />
```

```kotlin
val streamTextView = findViewById<StreamTextView>(R.id.streamTextView)

// 设置要展示的文本和速度（50ms/字符）
streamTextView.setStreamText("这是一段流式展示的文本", delayMillis = 50L)

// 开始流式展示
streamTextView.startStreaming()
```

### 自定义颜色

```kotlin
// 设置渐变色
streamTextView.setGradientColors(
    Color.parseColor("#FF6B6B"),  // 起始颜色
    Color.parseColor("#4ECDC4")   // 结束颜色
)

// 设置光标颜色
streamTextView.setCursorColor(Color.parseColor("#4E6EF2"))
```

### 事件监听

```kotlin
// 监听流式展示开始
streamTextView.setOnStreamStartListener {
    Log.d("Stream", "开始展示")
}

// 监听展示进度
streamTextView.setOnStreamProgressListener { progress ->
    Log.d("Stream", "进度: ${(progress * 100).toInt()}%")
}

// 监听展示完成
streamTextView.setOnStreamCompleteListener {
    Log.d("Stream", "展示完成")
}
```

### 控制方法

```kotlin
// 停止流式展示
streamTextView.stopStreaming()

// 重置组件
streamTextView.reset()

// 检查是否正在展示
val isStreaming: Boolean = streamTextView.isStreaming()

// 获取当前状态
val state: StreamState = streamTextView.getStreamState()
```

## 演示示例

项目包含完整的演示 Activity：`StreamTextDemoActivity`

### 运行演示

1. 运行应用
2. 在主界面点击右上角的"演示"按钮
3. 尝试不同的演示内容：
   - 短文本演示
   - 长文本演示
   - SSE流式文本演示
4. 调整速度滑块控制展示速度
5. 尝试不同的配色方案

### 演示功能

- ✅ 三种不同长度的文本演示
- ✅ 实时进度显示
- ✅ 速度调节（10ms - 200ms/字符）
- ✅ 三种预设配色方案
- ✅ 开始/停止/重置控制

## 适用场景

1. **AI 对话界面**
   - ChatGPT 风格的打字效果
   - 智能助手回复展示

2. **SSE 流式数据**
   - Server-Sent Events 数据展示
   - 实时数据流可视化

3. **加载提示**
   - 优雅的加载文案展示
   - 提升用户体验

4. **教育应用**
   - 逐字朗读效果
   - 文字动画教学

## 性能优化

- ✅ 使用 Handler 而非 Timer，避免创建新线程
- ✅ 在 onDetachedFromWindow 时自动清理资源
- ✅ 使用 SpannableStringBuilder 减少对象创建
- ✅ ValueAnimator 实现流畅的光标动画

## 兼容性

- **最低 SDK 版本**：API 21 (Android 5.0)
- **推荐 SDK 版本**：API 33+ (Android 13+)
- **依赖库**：AndroidX AppCompat

## 项目结构

```
ssebridge/src/main/java/com/github/ssebridge/widget/
├── StreamState.kt           # 展示状态枚举
├── LinearGradientSpan.kt    # 渐变色 Span
├── CursorSpan.kt            # 光标 Span
└── StreamTextView.kt        # 主组件类

app/src/main/java/com/github/ssebridge/app/
└── StreamTextDemoActivity.kt  # 演示 Activity
```

## 注意事项

1. **内存管理**：长文本展示时注意及时停止和重置
2. **生命周期**：组件会自动在 onDetachedFromWindow 时清理资源
3. **线程安全**：所有更新都在主线程执行，无需担心线程问题
4. **性能考虑**：建议单屏不要同时展示过多 StreamTextView

---

**享受流畅的流式文本展示体验！** 🚀

