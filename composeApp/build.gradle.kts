import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.jvmToolchain.get()
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Koin
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Extra UI
            implementation(compose.materialIconsExtended)

            // Logging
            implementation(libs.napier.logging)

            // Koin
            api(libs.koin.core)
            implementation(libs.koin.compose)

            // Viewmodels
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.navigation.compose)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.resources)

            // Room
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(kotlin("test-annotations-common"))
            implementation(libs.assertk)
        }
    }
}

val vName = project.findProperty("versionName") as String? ?: "1.0.0"
val vCode = project.findProperty("versionCode") as String? ?: "1"

android {
    namespace = "com.ultraviolince.mykitchen"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.ultraviolince.mykitchen"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = vCode.toInt()
        versionName = vName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
                "proguard-rules.pro",
            )
            resValue("string", "clear_text_config", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf(libs.versions.javaSourceVersion.get())
        targetCompatibility = JavaVersion.valueOf(libs.versions.javaTargetVersion.get())
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.ultraviolince.mykitchen"
            packageVersion = "1.0.0"
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
}

val excludedClasses =
    listOf(
        "*Activity",
        "*Activity\$*",
        "*.BuildConfig",
        "ComposableSingletons\$*",
    )

val excludedPackages =
    listOf(
        // Dependency injection itself doesn't need to be tested
        "com.ultraviolince.mykitchen.di",
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

ktlint {
    filter {
        exclude("**/generated/**")
    }
}
