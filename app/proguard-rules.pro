# Project-specific Proguard / R8 rules.
#
# AGP already applies `proguard-android-optimize.txt`. Library consumer rules
# (Retrofit, OkHttp, Firebase, Hilt, AndroidX) ship inside each artifact and
# are merged automatically — we only add what's project-specific here.

# --- kotlinx.serialization ----------------------------------------------------
# Keep generated serializers + companion objects for every @Serializable class.
# Without this R8 strips the .Companion / .serializer() lookups Retrofit's
# kotlinx-serialization converter relies on at runtime.

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.powerlifting.assistant.**$$serializer { *; }
-keepclassmembers class com.powerlifting.assistant.** {
    *** Companion;
}
-keepclasseswithmembers class com.powerlifting.assistant.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- App models reachable only via reflection (Retrofit @Body / @GET) ---------
-keep class com.powerlifting.assistant.data.api.** { *; }
-keep class com.powerlifting.assistant.domain.model.** { *; }

# --- Coroutines: keep debug metadata off, but preserve continuation classes ---
-keepclassmembers class kotlin.coroutines.jvm.internal.** { *; }

# --- Compose / Hilt rely on reflection over generated classes — covered by
#     consumer rules in their artifacts, no extra config needed.
