plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("com.google.devtools.ksp") version libs.versions.ksp
    id("app.cash.sqldelight") version libs.versions.sqldelight
    kotlin("plugin.serialization") version libs.versions.kotlin
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

sqldelight {
    databases {
        create("Database") {
            val sqldelightVersion = libs.versions.sqldelight.get()

            packageName.set("com.jocmp.capy.db")
            verifyMigrations.set(true)
            deriveSchemaFromMigrations.set(true)
            dialect("app.cash.sqldelight:sqlite-3-35-dialect:$sqldelightVersion")
        }
    }
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.sqldelight.coroutines.extensions)
    implementation(libs.okhttp.brotli)
    implementation(project(":feedbinclient"))
    implementation(project(":feedfinder"))
    implementation(project(":minifluxclient"))
    implementation(project(":rssparser"))
    implementation(project(":readerclient"))
    testImplementation(kotlin("test"))
    testImplementation(libs.sqldelight.sqlite.driver)
    testImplementation(libs.tests.junit)
    testImplementation(libs.tests.kotlinx.coroutines)
    testImplementation(libs.tests.mockk.agent)
    testImplementation(libs.tests.mockk.mockk)
    testImplementation(libs.tests.okhttp.mock)
}
