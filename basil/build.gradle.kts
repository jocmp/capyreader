plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version libs.versions.ksp
    id("app.cash.sqldelight") version libs.versions.sqldelight
}

android {
    namespace = "com.jocmp.basil"
    compileSdk = 34

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    sqldelight {
        databases {
            create("Database") {
                packageName.set("com.jocmp.basil.db")
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

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":feedbinclient"))
    implementation(project(":feedfinder"))
    implementation("androidx.datastore:datastore-core:1.0.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.github.requery:sqlite-android:3.43.0")
    testImplementation("app.cash.sqldelight:sqlite-driver:$sqldelightVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation(testLibs.mockk.android)
    testImplementation(testLibs.mockk.agent)
    testImplementation(kotlin("test"))
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
