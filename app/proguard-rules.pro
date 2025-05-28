# --- Default Android Rules (often included automatically by AGP, but good to be aware of) ---

# Keep attributes that are used by reflection.
-keepattributes Signature
-keepattributes *Annotation* # Keep annotations, important for libraries like Dagger, Retrofit, Room, etc.
-keepattributes InnerClasses # Preserve inner class information if needed

# Keep public classes and members that are entry points to your app or libraries.
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# Keep all public static final fields that are part of the R class,
# as they might be accessed via reflection.
-keep public class **.R$* {
    public static final int *;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom Views, their constructors, and set/get methods.
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep Parcelable implementations.
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
  # If you have custom Parcelable fields, you might need to keep them too.
  # <fields>;
}

# Keep Serializable implementations (if you use Java Serialization, less common now)
-keep class * implements java.io.Serializable {
    <fields>;
    <methods>;
}

# Keep Enum classes and their values() and valueOf() methods if used via reflection
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep an Application class that is referenced in AndroidManifest.xml.
-keep public class * extends android.app.Application

# --- Rules for Kotlin ---

# Keep Kotlin metadata. This is crucial for reflection and other Kotlin-specific features.
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; } # For Kotlin reflection library
-keep class kotlin.coroutines.** { *; } # For Kotlin coroutines

# Keep data classes' componentN() and copy() methods if they are used.
# R8 is generally good at this, but explicit rules can be a safeguard.
-keepclassmembers class * extends kotlin.Any { # Or specify your data class package
    public fun copy(...);
}
# If you are using data classes with libraries that use reflection (like Moshi, Gson, Jackson)
# you often need to keep the data class itself and its properties.
# Example: -keep class com.example.yourpackage.data.** { *; }

# Keep default_schema.json for Room (if exportSchema = true)
-keep class androidx.room.RoomDatabase_Impl {
    public static java.util.List<java.lang.String> getRequiredMigrations();
}


# --- Rules for Jetpack Libraries (Many are handled by consumer rules, but good to know) ---

# ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keep class androidx.lifecycle.ViewModelProvider$Factory { *; } # If you use custom factories

# LiveData / StateFlow / SharedFlow Observers (often handled well by R8)

# Room (Room has its own consumer rules, but if you have issues):
 -keep class androidx.room.** { *; }
 -keepclassmembers class * {
 @androidx.room.Dao <methods>;
 @androidx.room.Entity <fields>;
 @androidx.room.Database <methods>;
 }
# Keep generated Impl classes if needed (usually handled by consumer rules)
# -keep class *MyDatabase_Impl { *; }
# -keep class *MyDao_Impl { *; }

# Navigation Component
# -keep class * extends androidx.navigation.NavType { *; } # If you have custom NavTypes
# -keep public class * extends androidx.navigation.Navigator { *; } # If you have custom Navigators
# Keep classes used with Safe Args if accessed by reflection (usually handled)
# -keep class com.example.yourpackage.fragment.**Args
# -keep class com.example.yourpackage.fragment.**Args$Builder


# --- Rules for Common Third-Party Libraries (Check their specific documentation!) ---

# Example: Retrofit & OkHttp (These usually have consumer rules)
# -dontwarn okio.**
# -dontwarn retrofit2.**
# -dontwarn okhttp3.**
# -keep interface retrofit2.** { *; }
# -keep class retrofit2.Response { *; }
# -keep class com.squareup.okhttp.** { *; } # If using older OkHttp
# -keep class okhttp3.** { *; }
# -keep class okio.** { *; }
# If using Retrofit with data classes for requests/responses (e.g., with Moshi/Gson):
# -keep class com.example.yourpackage.network.model.** { *; } # Keep your model classes

# Example: Gson (if you use it for JSON serialization)
# -keep class com.google.gson.annotations.** { *; }
# -keep class * {
#     @com.google.gson.annotations.SerializedName <fields>;
# }
# Keep data classes that are serialized/deserialized.
# -keep class com.example.yourpackage.model.** { *; }

# Example: Moshi (if you use it for JSON serialization)
# -keep class com.squareup.moshi.JsonQualifier { *; }
# -keep @com.squareup.moshi.JsonQualifier @interface *
# -keepclassmembers class * {
#     @com.squareup.moshi.JsonQualifier <fields>;
#     @com.squareup.moshi.JsonQualifier <methods>;
# }
# Keep data classes that are serialized/deserialized.
# -keep class com.example.yourpackage.model.** { *; }
# If using Moshi with codegen:
# -keep class com.example.yourpackage.model.*JsonAdapter { *; }

# Example: Koin (Dependency Injection)
 -keep class org.koin.** { *; }
 -keep class * implements org.koin.core.module.Module # If using modules directly
 -keep class * { @org.koin.core.annotation.Module *; } # For Koin annotations
 -keep class * { @org.koin.core.annotation.Single *; }
 -keep class * { @org.koin.core.annotation.Factory *; }
 -keepclassmembers class * {
     @org.koin.core.component.KoinComponent <methods>;
 }
# Keep your Koin modules and injectables if not inferred correctly
# -keep class com.example.yourpackage.di.** { *; }

# Example: Coil / Glide / Picasso (Image Loading)
# These usually have good consumer rules.

# --- Your Project Specific Rules ---

# Keep any classes or members accessed via reflection that R8 cannot detect.
# For example, if you use Class.forName("com.example.MyClass")
# -keep class com.example.MyClass

# If you use JNI and the C/C++ code calls Java methods by name.
# -keepclasseswithmembernames class com.example.yourpackage.MyJniClass {
#     <methods>; # List specific

# --- Rules for kotlinx.serialization ---

# Keep the @Serializable annotation itself
-keep class kotlinx.serialization.Serializable { *; }

# Keep classes annotated with @Serializable and their members.
# This is the most important rule for general @Serializable classes.
-keepnames class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Serializable <init>(...); # Keep constructors
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <methods>; # Might be needed for custom serializers or certain library interactions
}

# Keep companion objects of @Serializable classes and their generated serializer() method.
# This is vital for the library to find and use the generated serializers.
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>; # Keep companion object fields if any special ones

}
# Alternative, more general rule for companion object serializers:
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    public static final fun serializer(...); # Catches serializer in companion
}


# Keep generated serializer classes (if they are not inner classes).
# Their names usually end with "$serializer".
# This might be overly broad, consider scoping to your specific packages if you know them.
# -keep class *.$serializer { *; }
# More specific, if your serializable classes are in a specific package:
# e.g., -keep class com.example.yourpackage.data.*.$serializer { *; }

# Keep the $CREATOR field in Parcelable KSerializer (if you use @Serializable with @Parcelize)
# for `kotlinx-serialization-parcelize`
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    public static final android.os.Parcelable$Creator *;
}

# Keep specific methods that kotlinx.serialization might use reflectively in some edge cases
# or if you are using features like polymorphism extensively with custom class discriminators.
# These are often not needed with the primary rules above but can be useful for troubleshooting.
# -keepclassmembers class * {
#     @kotlinx.serialization.Serializable <fields>;
#     public static final fun write$Self(...);
#     public static final fun read$Self(...);
# }

# Don't warn about kotlinx.serialization internal classes if R8 complains,
# though usually, the -keep rules above are sufficient.
-dontwarn kotlinx.serialization.**

-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**


# Specifically for DataStore's internal protobuf messages, though consumer rules *should* cover this.
# This is more of a diagnostic/just-in-case rule if you're desperate.
-keep class androidx.datastore.preferences.PreferencesProto** { *; }
-keepclassmembers class androidx.datastore.preferences.PreferencesProto** { *; }
