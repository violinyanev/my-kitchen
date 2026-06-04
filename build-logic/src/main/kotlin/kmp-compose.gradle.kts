plugins {
    id("kmp-library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
            implementation(libs.findLibrary("androidx-navigation-compose").get())
            implementation(libs.findLibrary("koin-compose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
        }
    }
}
