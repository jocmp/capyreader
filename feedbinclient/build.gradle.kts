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
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation(libs.moshi.kotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation(libs.retrofit2.retrofit)
    ksp(libs.moshi.kotlin.codegen)
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation(testLibs.mockk.mockk)
    testImplementation(testLibs.kotlinx.coroutines.test)
}
