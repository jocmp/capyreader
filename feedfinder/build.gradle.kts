plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp") version libs.versions.ksp
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.moshi)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp.client)
    implementation(project(":rssparser"))
    ksp(libs.moshi.kotlin.codegen)
    testImplementation(kotlin("test"))
    testImplementation(libs.tests.junit)
    testImplementation(libs.tests.kotlinx.coroutines)
    testImplementation(libs.mockwebserver3)
}
