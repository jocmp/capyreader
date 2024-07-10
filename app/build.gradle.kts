import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version libs.versions.kotlin
}

val secrets = Properties()

if (rootProject.file("secrets.properties").exists()) {
    secrets.load(rootProject.file("secrets.properties").inputStream())
}

android {
    namespace = "com.capyreader.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.capyreader.app"
        minSdk = 30
        targetSdk = 34
        versionCode = 1003
        versionName = "2024.07.1003"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += listOf("license")

    productFlavors {
        create("free") {
            dimension = "license"
            afterEvaluate {
                tasks
                    .matching {
                        !it.name.contains("Gplay") &&
                                (it.name.contains("GoogleServices") || it.name.contains("Crashlytics"))
                    }
                    .forEach { it.enabled = false }
            }
        }
        create("gplay") {
            dimension = "license"
            isDefault = true
            apply(plugin = "com.google.gms.google-services")
            apply(plugin = "com.google.firebase.crashlytics")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("${project.rootDir}/release.keystore")
            storePassword = secrets.getProperty("store_password")
            keyAlias = secrets.getProperty("key_alias")
            keyPassword = secrets.getProperty("key_password")
        }

        getByName("debug") {
            storeFile = file("${project.rootDir}/debug.keystore")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {
    implementation("androidx.compose.material3:material3-window-size-class-android:1.2.1")
    val sqldelightVersion = libs.versions.sqldelight.get()
    val pagingVersion = libs.versions.androidx.paging.get()

    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3.adaptive:adaptive:1.0.0-beta04")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.0.0-beta04")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.0.0-beta04")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.paging:paging-compose:3.3.0")
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("app.cash.sqldelight:android-driver:$sqldelightVersion")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.insert-koin:koin-android")
    implementation("io.insert-koin:koin-androidx-compose")
    implementation("io.insert-koin:koin-androidx-workmanager")
    implementation("io.insert-koin:koin-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation(libs.androidx.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.client)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation(platform("io.insert-koin:koin-bom:3.5.1"))
    implementation(project(":capy"))
    "gplayImplementation"("com.google.firebase:firebase-crashlytics:19.0.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

tasks.register("useGMSDebugFile") {
    description = "Copies the debug google-services.json file if file is missing."
    doLast {
        val googleServicesFile = "google-services.json"
        if (!file("${project.projectDir}/$googleServicesFile").exists()) {
            val debugOnlyFile = "google-services-debug-only.json"
            println("$googleServicesFile file is missing. Copying $debugOnlyFile")
            copy {
                from("${project.projectDir}/$debugOnlyFile")
                into(project.projectDir)
                rename { googleServicesFile }
            }
        }
    }
}

project.tasks.preBuild.dependsOn("useGMSDebugFile")
