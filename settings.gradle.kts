pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

rootProject.name = "Capy Reader"
include(":app")
include(":feedbinclient")
include(":feedfinder")
include(":capy")
include(":minifluxclient")
include(":rssparser")
include(":readerclient")
