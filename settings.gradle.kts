pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.22")
            version("sqldelight", "2.0.1")
            version("ksp", "1.9.22-1.0.16")
            version("androidx-paging", "3.2.1")
            library("moshi-kotlin", "com.squareup.moshi:moshi-kotlin:1.14.0")
            library("okhttp-client", "com.squareup.okhttp3:okhttp:4.12.0")
            library("moshi-kotlin-codegen", "com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
            library("androidx-preferences", "androidx.preference:preference-ktx:1.2.1")
            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            library("kotlinx-coroutines-test", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            library("rssparser", "com.prof18.rssparser:rssparser:6.0.4")
        }
        create("testLibs") {
            library("mockk-mockk", "io.mockk:mockk:1.13.7")
            library("mockk-android", "io.mockk:mockk-android:1.13.7")
            library("mockk-agent", "io.mockk:mockk-agent:1.13.7")
        }
    }
}

rootProject.name = "Basil Reader"
include(":app")
include(":feedbinclient")
include(":basil")
include(":feedfinder")
