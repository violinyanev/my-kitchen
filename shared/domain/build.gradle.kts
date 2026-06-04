plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // No android-specific deps for domain
        }
    }
}

android {
    namespace = "com.ultraviolince.mykitchen.shared.domain"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
