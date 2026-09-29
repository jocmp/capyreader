plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.jsoup)
    compileOnly(libs.jspecify)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
}
