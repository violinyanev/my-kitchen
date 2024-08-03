kotlin {
    jvmToolchain(17)
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.ultraviolince.mykitchen"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ultraviolince.mykitchen"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.ultraviolince.mykitchen.recipes.utils.TestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            resValue("string", "clear_text_config", "false")
        }
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "clear_text_config", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        allWarningsAsErrors = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // This is needed for koin+KSP
    applicationVariants.forEach { variant ->
        variant.sourceSets.forEach {
            it.javaDirectories += files("build/generated/ksp/${variant.name}/kotlin")
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    buildToolsVersion = "34.0.0"

    ksp {
        arg("KOIN_CONFIG_CHECK", "true")
    }
}

dependencies {
    // Standard android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.core)
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.ext) // For custom icons
    implementation(libs.androidx.compose.ui.tooling.preview)
    // Koin - dependency injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)
    // Database local storage
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // Ktor - backend interaction
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    //implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.resources)

    // Debug dependencies
    // debugImplementation(libs.androidx.monitor)
    debugImplementation(libs.androidx.compose.ui.tooling) // For previews
    // debugImplementation(libs.androidx.test.manifest)    // For tests

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.runner)
    // Needed to fix a bug in ui-test (pins espresso to 3.5.0 which has a bug)
    androidTestImplementation(libs.androidx.espresso.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    // For better coroutine tests
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.koin.test.junit4)

    detektPlugins(libs.detektTwitterPlugin)
    detektPlugins(libs.detektFormattingPlugin)
}

val excludedClasses = listOf(
    "*Activity",
    "*Activity\$*",
    "*_Impl",
    "*.BuildConfig",
    "ComposableSingletons\$*"
)

val excludedPackages = listOf(
    // Dependency injection itself doesn't need to be tested
    "com.ultraviolince.mykitchen.di",
    // Presentation not unit test(able) currently, could revisit later (maybe try paparazzi + compose?)
    "com.ultraviolince.mykitchen.recipes.presentation",
    // Theme values are generated, no need to unit test
    "com.ultraviolince.mykitchen.ui.theme",
    "dagger.hilt.internal.aggregatedroot.codegen",
    "_HiltModules",
    "Hilt_",
    "dagger.hilt.internal.aggregatedroot.codegen",
    "hilt_aggregated_deps",
    "ViewModel_Factory"
)

kover {
    reports {
        filters {
            excludes {
                classes(excludedClasses)
                packages(excludedPackages)
            }
        }
    }
}

detekt {
    autoCorrect = true
    config.setFrom("${project.rootDir}/gradle/detekt.yml")
}
