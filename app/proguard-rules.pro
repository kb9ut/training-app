# ── Room ──
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# ── kotlinx-serialization ──
# Keep serialization infrastructure
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep serializer companion objects
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}

# Keep all @Serializable classes and their generated serializers
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep the backup data classes explicitly
-keep class com.kb9ut.pror.backup.** { *; }

# ── Hilt ──
# Hilt is mostly handled by its Gradle plugin, but keep generated components
-dontwarn dagger.hilt.**

# ── Enums (used by Room type converters) ──
-keepclassmembers enum com.kb9ut.pror.local.entity.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Navigation ──
# Keep Screen route classes for navigation arguments
-keep class com.kb9ut.pror.navigation.Screen { *; }
-keep class com.kb9ut.pror.navigation.Screen$* { *; }

# ── General ──
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
