// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    // alias(libs.plugins.spotless)
    alias(libs.plugins.ksp) apply false
    // alias(libs.plugins.android.test) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    // Warning suppressed: Dependency Analysis plugin v2.19.0 officially supports AGP 8.3.0-8.10.0
    // but is known to work with AGP 8.12.0. Newer plugin versions supporting 8.12.0 are not yet released.
    alias(libs.plugins.build.health) apply true
    alias(libs.plugins.roborazzi) apply false
}
