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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit/Gson models — snake_case field names must survive R8
-keepclassmembers class com.tokendad.nesventory.data.remote.** { *; }
-keepclassmembers class com.tokendad.nesventory.data.model.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.** { *; }

# DataStore keys
-keep class androidx.datastore.** { *; }

# Retrofit
-keepattributes Signature, RuntimeVisibleAnnotations, AnnotationDefault
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Niimbot printer — byte-level BLE protocol must not be renamed
-keep class com.tokendad.nesventory.ui.printer.** { *; }