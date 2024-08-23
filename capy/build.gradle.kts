plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version libs.versions.ksp
    id("app.cash.sqldelight") version libs.versions.sqldelight
    kotlin("plugin.serialization") version libs.versions.kotlin
}

android {
    namespace = "com.jocmp.capy"
    compileSdk = 34

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
                dialect("app.cash.sqldelight:sqlite-3-38-dialect:$sqldelightVersion")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        resources {
            excludes.add("lib/x86/libsqlite3x.so")
            excludes.add("lib/x86_64/libsqlite3x.so")
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.preferences)
    implementation(libs.jsoup)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.readability4j)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.sqldelight.androidx.paging.extensions)
    implementation(libs.sqldelight.coroutines.extensions)
    implementation(project(":feedbinclient"))
    implementation(project(":feedfinder"))
    implementation(project(":rssparser"))
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
