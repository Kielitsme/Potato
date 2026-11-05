# Keep data models
-keep class com.israadev.nuxlauncher.core.models.** { *; }
-keep class com.oracle.dalvik.** { *; }
-keep class com.movtery.zalithlauncher.game.input.CriticalNativeTest { *; }
-keep class org.lwjgl.glfw.CallbackBridge { *; }

# SDL Keep rules (JNI native methods and callbacks)
-keep class org.libsdl.app.** { *; }
-keep class com.movtery.zalithlauncher.game.sdl.** { *; }

# ---- R8 safety: seluruh kode aplikasi dijaga utuh (dipanggil dari JNI/refleksi/Gson);
# ---- yang dipangkas hanya library pihak ketiga yang tidak terpakai.
-keep class com.israadev.nuxlauncher.** { *; }
-keep class com.movtery.** { *; }
-keep class org.lwjgl.** { *; }
-keep class org.libsdl.** { *; }
-keepclasseswithmembernativemethods class * { native <methods>; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
