plugins {
    id("kmp-compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
            implementation(project(":shared:data"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "com.ultraviolince.mykitchen.shared.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
