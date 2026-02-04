import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version libs.versions.kotlin
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

val secrets = Properties()

if (rootProject.file("secrets.properties").exists()) {
    secrets.load(rootProject.file("secrets.properties").inputStream())
}

android {
    namespace = "com.capyreader.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.capyreader.app"
        minSdk = 30
        targetSdk = 36
        versionCode = 1189
        versionName = "2026.02.1189"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
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
        debug {
            applicationIdSuffix = ".debug"
        }
        create("nightly") {
            initWith(getByName("release"))
            applicationIdSuffix = ".nightly"
            // https://developer.android.com/build/build-variants#resolve_matching_errors
            matchingFallbacks += "release"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}


dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.preferences)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)
    implementation(libs.coil.video)
    implementation(libs.lazycolumnscrollbar)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.client)
    implementation(libs.sqldelight.android.driver)
    implementation(libs.zoomable)
    implementation(libs.zoomable.image.coil)
    implementation(platform(libs.androidx.compose.bom))
    implementation(project(":capy"))
    implementation(project(":feedfinder"))
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.glance.appwidget.preview)
    "gplayImplementation"(libs.firebase.crashlytics)
    testImplementation(libs.tests.junit)
    androidTestImplementation(libs.tests.androidx.test.ext)
    androidTestImplementation(libs.tests.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.tests.androidx.ui.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.tests.androidx.ui.manifest)
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
