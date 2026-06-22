import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kmp-library")
}

android {
    namespace = "com.ultraviolince.mykitchen.shared.domain"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        val jv = JavaVersion.toVersion(libs.versions.javaVersion.get())
        sourceCompatibility = jv
        targetCompatibility = jv
    }
}

kotlin {
    androidTarget {
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
