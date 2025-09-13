import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import java.util.Properties

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.roborazzi)
}

val vName = project.findProperty("versionName") as String? ?: "v1.0.0"

// Allow overriding versionCode via property, otherwise calculate from versionName
val vCode = if (project.hasProperty("versionCode")) {
    (project.findProperty("versionCode") as String).toInt()
} else {
    val versionParts = vName.removePrefix("v").split(".")
    val major = versionParts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = versionParts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = versionParts.getOrNull(2)?.toIntOrNull() ?: 0

    // This is not ideal, but easier than using a custom versioning scheme
    major * 10000 + minor * 100 + patch
}

println("Version Name: $vName")
println("Version Code: $vCode")

android {
    namespace = "com.ultraviolince.mykitchen"
    compileSdk = libs.versions.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    // Load keystore properties
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.ultraviolince.mykitchen"

        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = vCode.toInt()
        versionName = vName

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
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "DEFAULT_SERVER", "\"\"")
            buildConfigField ("String", "DEFAULT_USERNAME", "\"\"")
            buildConfigField("String", "DEFAULT_PASSWORD", "\"\"")

            // For snapshot builds (release candidates), use debug-like configuration
            if (project.hasProperty("snapshotBuild") && project.property("snapshotBuild") == "true") {
                // Use debug app name and add debug suffix to distinguish from production
                applicationIdSuffix = ".preview"
                // Copy debug resources for app name and icon
                resValue("string", "app_name", "Kitchen on fire!")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "DEFAULT_SERVER", "\"http://10.0.2.2:5000\"")
            buildConfigField ("String", "DEFAULT_USERNAME", "\"test@user.com\"")
            buildConfigField("String", "DEFAULT_PASSWORD", "\"TestPassword\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware"
            }
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

    ksp {
        arg("KOIN_CONFIG_CHECK", "true")
        arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
        arg("KOIN_DEFAULT_MODULE", "false")
    }
}

dependencies {
    // Shared module
    implementation(project(":shared"))
    
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
    implementation(libs.androidx.datastore.preferences)
    // Ktor - backend interaction
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.resources)

    // Debug dependencies
    debugImplementation(libs.androidx.compose.ui.tooling) // For previews

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

    // Roborazzi screenshot testing
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.preview.scanner)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.preview.scanner.compose)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)

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
    "org.koin.ksp.generated",
    // Presentation not unit test(able) currently, could revisit later (maybe try paparazzi + compose?)
    "com.ultraviolince.mykitchen.recipes.presentation",
    // Theme values are generated, no need to unit test
    "com.ultraviolince.mykitchen.ui.theme",
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

roborazzi {
    @OptIn(ExperimentalRoborazziApi::class)
    generateComposePreviewRobolectricTests {
        enable = true
        includePrivatePreviews = false
        packages = listOf("com.ultraviolince.mykitchen.recipes.presentation")
    }
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
}

// Fix task dependencies for KSP and Roborazzi
afterEvaluate {
    tasks.named("kspDebugUnitTestKotlin").configure {
        dependsOn("generateDebugComposePreviewRobolectricTests")
        dependsOn("generateReleaseComposePreviewRobolectricTests")
    }
    tasks.named("kspReleaseUnitTestKotlin").configure {
        dependsOn("generateDebugComposePreviewRobolectricTests")
        dependsOn("generateReleaseComposePreviewRobolectricTests")
    }
}
