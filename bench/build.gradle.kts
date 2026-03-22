plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("com.jocmp.bench.MainKt")
}

dependencies {
    implementation(project(":capy"))
    implementation(libs.sqldelight.sqlite.driver)
    implementation(libs.kotlinx.coroutines.core)
}
