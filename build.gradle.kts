// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // alias(libs.plugins.spotless)
    alias(libs.plugins.ksp) apply false
    // alias(libs.plugins.android.test) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
