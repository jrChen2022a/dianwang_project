# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.

# 保留 OkHttp 的类和方法
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-keep class com.purui.service.IPuruiService {*;}
-keep class com.purui.service.IPuruiService$** {*;}
-keep class com.purui.service.result.** { *;}
-keep class com.purui.service.cam.NativeCamManager{*;}

-dontshrink
-keep class org.bytedeco.ffmpeg.**{ *; }
-keep class org.bytedeco.javacpp.**{*;}
-keep class org.bytedeco.javacv.**{*; }
-keep class org.bytedeco.opencv.**{*; }
-keep class org.opencv.**{*; }
-dontwarn org.bytedeco.ffmpeg.**
-dontwarn org.bytedeco.javacpp.**
-dontwarn org.bytedeco.javacv.**
-dontwarn org.bytedeco.opencv.**
-dontwarn org.opencv**

-keep class com.purui.service.PuruiServiceManager {
    public <fields>;
    public <methods>;
}
-keep class com.purui.service.ynmodule.YNNative {
    native <methods>;
}
-keep class com.purui.service.ynmodule.YNNative$Obj{
    public <fields>;
    public <methods>;
}
-keep class com.purui.service.ocrmodule.OCRPredictorNative {}
-keep class com.purui.service.facemodule.Face {}


# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile