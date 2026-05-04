# Keep line numbers for crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Moshi
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers @com.squareup.moshi.JsonClass class * { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Retrofit API interfaces - must NOT be obfuscated or renamed.
-keep interface com.rootilabs.wmeCardiac.data.api.** { *; }

# Retrofit + Kotlin suspend: Signature attribute cross-reference fix.
# R8 renames kotlin.coroutines.Continuation (→ x8.c) and retrofit2.Response (→ ic.s0)
# but does NOT update Signature attributes inside explicitly kept classes.
# At runtime JVM can't resolve the original names → getGenericParameterTypes() returns raw
# Class instead of ParameterizedType → ClassCastException in HttpServiceMethod.parseAnnotations.
-keep class kotlin.coroutines.Continuation
-keep class retrofit2.Response

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# App data models (Moshi needs these intact)
-keep class com.rootilabs.wmeCardiac.data.model.** { *; }
-keep class com.rootilabs.wmeCardiac.data.auth.** { *; }

# WorkManager
# Workers are instantiated by class name string stored in WorkManager's SQLite DB.
# R8 renaming the class breaks the lookup even if WorkManager's consumer rules run first.
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ML Kit Barcode Scanning
# BarcodeScanning was being removed by R8 (R8$$REMOVED$$CLASS$$711), losing its static
# initializer which triggers MlKitContext setup. Keeping the class prevents the NPE.
-keep class com.google.mlkit.vision.barcode.BarcodeScanning { *; }
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
