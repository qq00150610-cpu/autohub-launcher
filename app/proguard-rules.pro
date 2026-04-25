# ==============================================
# 凹凸桌面 ProGuard 混淆规则
# ==============================================

# 包名: com.autocar.launcher

# 基本混淆规则
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# 保持自定义 Application 类
-keep class com.autocar.launcher.AutoHubApplication { *; }

# 保持所有 Service
-keep class com.autocar.launcher.service.** { *; }

# 保持所有 BroadcastReceiver
-keep class com.autocar.launcher.receiver.** { *; }

# 保持所有 Activity
-keep class com.autocar.launcher.ui.activity.** { *; }

# 保持所有 Fragment
-keep class com.autocar.launcher.ui.fragment.** { *; }

# 保持所有 ViewModel
-keep class com.autocar.launcher.viewmodel.** { *; }

# 保持数据模型
-keep class com.autocar.launcher.data.model.** { *; }

# Kotlin 相关
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin 协程
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Room 数据库
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Lifecycle
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# DataStore
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# Timber 日志
-keep class com.jakewharton.timber.** { *; }
-dontwarn org.jetbrains.annotations.**

# Coil 图片加载
-keep class coil.** { *; }
-dontwarn coil.**

# 保持 Parcelable 实现
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# 保持枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# R 类
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 移除日志（在 release 构建中）
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# 保持本地方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保持资源引用
-keepclassmembers class **.R$* {
    public static <fields>;
}

# WebView 处理
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}

# 混淆比例优化
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''

# 不混淆内部类
-keepclassmembers class $InnerClass {
    java.lang.String this$0;
}

# 保持 JNI 调用
-keepclasseswithmembernames class * {
    native <methods>;
}
