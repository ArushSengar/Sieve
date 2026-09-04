# Sieve ProGuard rules

# --------------- Room Database ---------------
# Keep Room entities and DAOs (Room generates code that reflection won't find after R8)
-keep class com.sieve.filter.data.local.entity.** { *; }
-keep class com.sieve.filter.data.local.dao.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static <fields>;
    !private <methods>;
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# --------------- Kotlin ---------------
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Lazy {
    <methods>;
}

# --------------- Kotlin Coroutines ---------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --------------- Jetpack Compose ---------------
-keepclassmembers class androidx.compose.runtime.** { *; }
-keep class androidx.compose.material3.** { *; }
-keepclassmembernames class ** {
    @androidx.compose.runtime.Composable *;
}

# --------------- AndroidX ---------------
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class com.sieve.filter.ui.viewmodel.** { *; }

# --------------- Notification Listener Service ---------------
-keep class com.sieve.filter.service.SieveNotificationListenerService { *; }
-keep class com.sieve.filter.SieveApplication { *; }

# --------------- Models ---------------
-keep class com.sieve.filter.model.** { *; }

# --------------- General Android ---------------
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# --------------- Suppress warnings ---------------
-dontwarn kotlinx.serialization.**
-dontwarn kotlin.reflect.jvm.internal.**
