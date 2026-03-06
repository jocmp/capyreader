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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

# https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md
-keepattributes Signature
-keep class kotlin.coroutines.Continuation

-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Readability4J
-dontwarn org.slf4j.impl.StaticLoggerBinder

# JSoup optional Re2j support
-dontwarn com.google.re2j.**

# Internal
-keep class com.jocmp.** { *; }

-dontwarn com.jocmp.**
