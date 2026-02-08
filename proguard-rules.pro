# Правила для OpenVPN библиотеки
-keep class de.blinkt.openvpn.** { *; }
-keep interface de.blinkt.openvpn.** { *; }
-keep class org.spongycastle.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class android.net.VpnService { *; }

# Правила для нашего приложения
-keep class com.freevpn.app.** { *; }
-keep class * extends androidx.appcompat.app.AppCompatActivity { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Android Support библиотеки
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class org.jetbrains.** { *; }

# Сохраняем View Binding
-keep class * extends androidx.viewbinding.ViewBinding { *; }

# Сохраняем ресурсы
-keep class **.R$* { *; }

# Сохраняем нативные методы
-keepclasseswithmembernames class * {
    native <methods>;
}

# Сохраняем Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Не обфусцировать лямбда-выражения
-keepclassmembers class * {
    private static synthetic *** lambda$*(...);
}

# Для дебага
-keepattributes SourceFile,LineNumberTable
