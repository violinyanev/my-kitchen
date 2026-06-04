plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":shared:ui"))
    implementation(compose.desktop.currentOs)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
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
            packageVersion = "2.0.0"
        }
    }
}
