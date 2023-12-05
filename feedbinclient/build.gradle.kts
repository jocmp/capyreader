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
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation(libs.moshi.kotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    ksp(libs.moshi.kotlin.codegen)
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation(libs.kotlinx.coroutines.test)
}
