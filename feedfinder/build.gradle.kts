plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.jsoup:jsoup:1.17.1")
    implementation("com.prof18.rssparser:rssparser:6.0.4")
    implementation(libs.moshi.kotlin)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.moshi.kotlin.codegen)
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.kotlinx.coroutines.test)
}
