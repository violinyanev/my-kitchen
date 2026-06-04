import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kmp-library")
}

kotlin {
    android {
        namespace = "com.ultraviolince.mykitchen.shared.domain"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        androidMain.dependencies {
            // No android-specific deps for domain
        }
    }
}

