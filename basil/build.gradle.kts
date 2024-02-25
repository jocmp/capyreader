plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version libs.versions.ksp
    id("app.cash.sqldelight") version libs.versions.sqldelight
    kotlin("plugin.serialization") version libs.versions.kotlin
}

android {
    namespace = "com.jocmp.basil"
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

                packageName.set("com.jocmp.basil.db")
                verifyMigrations.set(true)
                deriveSchemaFromMigrations.set(true)
                dialect("app.cash.sqldelight:sqlite-3-30-dialect:$sqldelightVersion")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    val coreVersion = "1.12.0"
    val sqldelightVersion = libs.versions.sqldelight.get()
    val pagingVersion = libs.versions.androidx.paging.get()

    implementation("androidx.core:core-ktx:$coreVersion")
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("app.cash.sqldelight:androidx-paging3-extensions:$sqldelightVersion")
    implementation(libs.androidx.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.rssparser)
    implementation(project(":feedbinclient"))
    implementation(project(":feedfinder"))
    testImplementation("app.cash.sqldelight:sqlite-driver:$sqldelightVersion")
    implementation("app.cash.sqldelight:coroutines-extensions:$sqldelightVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
    testImplementation(testLibs.mockk.agent)
    testImplementation(testLibs.mockk.android)
    testImplementation(testLibs.kotlinx.coroutines.test)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
