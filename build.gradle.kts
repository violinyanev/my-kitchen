// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    // alias(libs.plugins.spotless)
    alias(libs.plugins.ksp) apply false
    // alias(libs.plugins.android.test) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.build.health) apply true
    alias(libs.plugins.roborazzi) apply false
}
