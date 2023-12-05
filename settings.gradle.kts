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
    }

    versionCatalogs {
        create("libs") {
            library("moshi-kotlin", "com.squareup.moshi:moshi-kotlin:1.14.0")
            library("moshi-kotlin-codegen", "com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            library("kotlinx-coroutines-test", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        }
    }
}

rootProject.name = "Basil Reader"
include(":app")
include(":feedbinclient")
include(":basil")
include(":feedfinder")
