// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin apply false
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin
}
