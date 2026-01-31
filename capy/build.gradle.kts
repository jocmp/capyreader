plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version libs.versions.ksp
    id("app.cash.sqldelight") version libs.versions.sqldelight
    kotlin("plugin.serialization") version libs.versions.kotlin
}

android {
    namespace = "com.jocmp.capy"
    compileSdk = 36

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    sqldelight {
        databases {
            create("Database") {
                val sqldelightVersion = libs.versions.sqldelight.get()

                packageName.set("com.jocmp.capy.db")
                verifyMigrations.set(true)
                deriveSchemaFromMigrations.set(true)
                // Explicitly set API 30's version
                // https://developer.android.com/reference/android/database/sqlite/package-summary
                dialect("app.cash.sqldelight:sqlite-3-25-dialect:$sqldelightVersion")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            consumerProguardFiles("consumer-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes.add("lib/x86/libsqlite3x.so")
            excludes.add("lib/x86_64/libsqlite3x.so")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.preferences)
    implementation(libs.jsoup)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.sqldelight.androidx.paging.extensions)
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
    testImplementation(libs.tests.mockk.android)
    testImplementation(libs.tests.okhttp.mock)
    androidTestImplementation(libs.tests.androidx.espresso.core)
    androidTestImplementation(libs.tests.androidx.test.ext)
}
