import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.ultraviolince.mykitchen"
    compileSdk = libs.versions.compileSdk.get().toInt()

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
        versionCode = project.findProperty("versionCode")?.toString()?.toInt() ?: 1
        versionName = project.findProperty("versionName")?.toString() ?: "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")

            if (project.hasProperty("snapshotBuild") && project.property("snapshotBuild") == "true") {
                applicationIdSuffix = ".preview"
                resValue("string", "app_name", "Kitchen on fire!")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs("-Drobolectric.pixelCopyRenderMode=hardware")
                it.systemProperties["roborazzi.output.dir"] = "${project.projectDir}/src/test/screenshots"
            }
        }
    }
}

dependencies {
    implementation(project(":shared:ui"))
    implementation(project(":shared:data"))
    implementation(project(":shared:domain"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.roborazzi.preview.scanner)
    testImplementation(libs.preview.scanner.compose)
    testImplementation(libs.robolectric)
}

roborazzi {
    generateComposePreviewRobolectricTests {
        enable = true
        includePrivatePreviews = false
        packages = listOf("com.ultraviolince.mykitchen.ui")
        testRunnerClass = "com.ultraviolince.mykitchen.ScreenshotTestRunner"
    }
}
