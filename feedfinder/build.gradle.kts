plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp") version libs.versions.ksp
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.rssparser)
    implementation(libs.moshi)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp.client)
    ksp(libs.moshi.kotlin.codegen)
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation(testLibs.kotlinx.coroutines.test)
}
