import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kmp-compose")
}

// Without an explicit group in this project, CMP would derive a package of "ui.generated.resources"
// which doesn't match the imports used across the codebase. Pin it explicitly.
compose.resources {
    packageOfResClass = "com.ultraviolince.mykitchen.ui.generated.resources"
}

kotlin {
    android {
        namespace = "com.ultraviolince.mykitchen.shared.ui"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
            implementation(project(":shared:data"))
            implementation(compose.materialIconsExtended)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(compose.components.uiToolingPreview)
        }
    }
}
