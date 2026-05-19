# Filament rules
-keep class com.google.android.filament.** { *; }
-keep class com.google.android.filament.gltfio.** { *; }
-dontwarn com.google.android.filament.**

# Coroutines
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory {
    *** create(...);
}

-keepclassmembernames class kotlinx.coroutines.scheduling.DefaultScheduler {
    *** accessors(...);
}

-keepclassmembernames class kotlinx.coroutines.scheduling.DefaultIoScheduler {
    *** accessors(...);
}

# Serialization
-keep @kotlinx.serialization.Serializable class com.vrm.avatarrenderer.** { *; }
-keepclassmembers class com.vrm.avatarrenderer.** {
    static <fields>;
}

# Timber logging
-dontwarn timber.**

# Keep main activity and application classes
-keep class com.vrm.avatarrenderer.MainActivity { *; }
-keep class com.vrm.avatarrenderer.App { *; }
-keep class com.vrm.avatarrenderer.rendering.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep constructors and enum methods
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep View constructors for inflation
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep ViewBinding
-keep class * extends androidx.viewbinding.ViewBinding {
    public static *** bind(...);
    public static *** inflate(...);
}
