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

// Composite build for local mercury-parser-kt iteration. Uncomment to test
// against the working tree at ../mercury-parser-kt instead of Maven Central.
// includeBuild("../mercury-parser-kt") {
//     dependencySubstitution {
//         substitute(module("com.jocmp:mercury-parser")).using(project(":mercury-parser"))
//     }
// }

include(":app")
include(":feedbinclient")
include(":feedfinder")
include(":capy")
include(":minifluxclient")
include(":rssparser")
include(":readerclient")
include(":bench")
