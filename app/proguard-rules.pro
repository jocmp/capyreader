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

# Internal
-dontwarn com.jocmp.capy.Account
-dontwarn com.jocmp.capy.AccountBuildArticlePagerExtKt
-dontwarn com.jocmp.capy.AccountCountExtKt
-dontwarn com.jocmp.capy.AccountManager
-dontwarn com.jocmp.capy.AccountPreferences
-dontwarn com.jocmp.capy.Article$FullContentState
-dontwarn com.jocmp.capy.Article
-dontwarn com.jocmp.capy.ArticleFilter$Articles
-dontwarn com.jocmp.capy.ArticleFilter$Companion
-dontwarn com.jocmp.capy.ArticleFilter$Feeds
-dontwarn com.jocmp.capy.ArticleFilter$Folders
-dontwarn com.jocmp.capy.ArticleFilter
-dontwarn com.jocmp.capy.ArticleStatus
-dontwarn com.jocmp.capy.Countable
-dontwarn com.jocmp.capy.DatabaseProvider
-dontwarn com.jocmp.capy.EditFeedFormEntry
-dontwarn com.jocmp.capy.Feed
-dontwarn com.jocmp.capy.Folder
-dontwarn com.jocmp.capy.MarkRead$After
-dontwarn com.jocmp.capy.MarkRead$All
-dontwarn com.jocmp.capy.MarkRead$Before
-dontwarn com.jocmp.capy.MarkRead
-dontwarn com.jocmp.capy.PreferenceStoreProvider
-dontwarn com.jocmp.capy.accounts.AddFeedResult$AddFeedError$FeedNotFound
-dontwarn com.jocmp.capy.accounts.AddFeedResult$AddFeedError$NetworkError
-dontwarn com.jocmp.capy.accounts.AddFeedResult$AddFeedError$SaveFailure
-dontwarn com.jocmp.capy.accounts.AddFeedResult$AddFeedError
-dontwarn com.jocmp.capy.accounts.AddFeedResult$Failure
-dontwarn com.jocmp.capy.accounts.AddFeedResult$MultipleChoices
-dontwarn com.jocmp.capy.accounts.AddFeedResult$Success
-dontwarn com.jocmp.capy.accounts.AddFeedResult
-dontwarn com.jocmp.capy.accounts.AutoDelete
-dontwarn com.jocmp.capy.accounts.Credentials$Companion
-dontwarn com.jocmp.capy.accounts.Credentials
-dontwarn com.jocmp.capy.accounts.FeedOption
-dontwarn com.jocmp.capy.accounts.Source$Companion
-dontwarn com.jocmp.capy.accounts.Source
-dontwarn com.jocmp.capy.articles.ArticleRenderer
-dontwarn com.jocmp.capy.articles.FontOption$Companion
-dontwarn com.jocmp.capy.articles.FontOption
-dontwarn com.jocmp.capy.articles.ParseHTMLKt
-dontwarn com.jocmp.capy.articles.TemplateColors
-dontwarn com.jocmp.capy.articles.TextSize$Companion
-dontwarn com.jocmp.capy.articles.TextSize
-dontwarn com.jocmp.capy.articles.UnreadSortOrder$Companion
-dontwarn com.jocmp.capy.articles.UnreadSortOrder
-dontwarn com.jocmp.capy.common.Async$Failure
-dontwarn com.jocmp.capy.common.Async$Loading
-dontwarn com.jocmp.capy.common.Async$Uninitialized
-dontwarn com.jocmp.capy.common.Async
-dontwarn com.jocmp.capy.common.CoroutineExtKt
-dontwarn com.jocmp.capy.common.LocalDateTimeExtKt
-dontwarn com.jocmp.capy.common.NetworkErrorExtKt
-dontwarn com.jocmp.capy.common.OptionalURLKt
-dontwarn com.jocmp.capy.common.StringProtocolExtKt
-dontwarn com.jocmp.capy.common.TimeHelpers
-dontwarn com.jocmp.capy.common.UnauthorizedError
-dontwarn com.jocmp.capy.db.Database
-dontwarn com.jocmp.capy.notifications.ArticleNotification
-dontwarn com.jocmp.capy.opml.ImportProgress
-dontwarn com.jocmp.capy.preferences.AndroidPreferenceStore
-dontwarn com.jocmp.capy.preferences.Preference
-dontwarn com.jocmp.capy.preferences.PreferenceStore$DefaultImpls
-dontwarn com.jocmp.capy.preferences.PreferenceStore
