import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":capy"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.sqldelight.sqlite.driver)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.jsoup)
    implementation(compose.materialIconsExtended)
}

compose.desktop {
    application {
        mainClass = "com.jocmp.capyreader.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Capy Reader"
            packageVersion = "1.0.0"

            macOS {
                bundleID = "com.jocmp.capyreader.desktop"
            }
        }
    }
}
