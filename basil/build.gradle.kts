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
    val sqldelightVersion = libs.versions.sqldelight.get()
    val pagingVersion = libs.versions.androidx.paging.get()

    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("androidx.datastore:datastore-core:1.0.0")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.rssparser)
    implementation(project(":feedbinclient"))
    implementation(project(":feedfinder"))
    implementation("app.cash.sqldelight:androidx-paging3-extensions:$sqldelightVersion")
    implementation(libs.kotlinx.serialization.json)
    testImplementation("app.cash.sqldelight:sqlite-driver:$sqldelightVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
    testImplementation(testLibs.mockk.agent)
    testImplementation(testLibs.mockk.android)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
