plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":shared:ui"))
    implementation(project(":shared:data"))
    implementation(project(":shared:domain"))
    implementation(compose.desktop.currentOs)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.ultraviolince.mykitchen.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )
            packageName = "My Kitchen"
            // Deb/Dmg/Msi require a numeric-only version (no leading 'v', no pre-release suffix).
            // Strip the 'v' prefix and everything from the first '-' onward: "v0.6.9-rc" → "0.6.9".
            packageVersion = (project.findProperty("versionName")?.toString() ?: "1.0.0")
                .removePrefix("v")
                .substringBefore("-")
                .ifBlank { "1.0.0" }
        }
    }
}
