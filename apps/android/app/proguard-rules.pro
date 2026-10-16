# Proguard rules for SAHASRA Android App

# Keep Retrofit & Gson models
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.sahasra.app.data.remote.dto.** { *; }
-keep class com.google.gson.** { *; }

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**

# Media3 ExoPlayer
-keep class androidx.media3.** { *; }

# Coil
-dontwarn coil.**
