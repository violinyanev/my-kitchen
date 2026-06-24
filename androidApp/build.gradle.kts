@file:OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)

import java.util.Properties
import javax.inject.Inject
import org.gradle.api.file.FileSystemOperations

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.roborazzi)
}

// CMP-9547: shared:ui uses com.android.kotlin.multiplatform.library whose
// KotlinMultiplatformAndroidVariant.sources.assets == null in AGP 9.x, so CMP 1.11.1 silently
// skips registration of generated .cvr files. Copy the prepared resources into a local build
// directory and register it via the AGP Variant API (addGeneratedSourceDirectory) so that
// mergeDebugAssets (APK / instrumented tests) and mergeDebugUnitTestAssets (Robolectric) both
// pick up the .cvr files automatically. AGP 9.2.1 blocks assets.srcDir(Provider) in sourceSets.
abstract class CopyDirTask @Inject constructor(
    private val fileSystemOperations: FileSystemOperations,
) : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val destinationDirectory: DirectoryProperty

    @TaskAction
    fun copy() {
        fileSystemOperations.sync {
            from(sourceDirectory)
            into(destinationDirectory)
        }
    }
}

val copyCmpAssets = tasks.register<CopyDirTask>("copyCmpAssetsForAndroid") {
    dependsOn(project(":shared:ui").tasks.named("prepareComposeResourcesTaskForCommonMain"))
    sourceDirectory.set(
        project(":shared:ui").layout.buildDirectory
            .dir("generated/compose/resourceGenerator/preparedResources/commonMain"),
    )
    destinationDirectory.set(layout.buildDirectory.dir("generated/cmp-assets"))
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
        val jv = JavaVersion.toVersion(libs.versions.javaVersion.get())
        sourceCompatibility = jv
        targetCompatibility = jv
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

// AGP 9.x Variant API: register copyCmpAssetsForAndroid output as a generated asset source for
// every variant. This wires copyCmpAssetsForAndroid as a dependency of mergeDebugAssets /
// mergeReleaseAssets (APK packaging → instrumented tests) and, because isIncludeAndroidResources
// = true, also of mergeDebugUnitTestAssets whose output directory AGP passes to Robolectric via
// android.merged_assets. No manual android.merged_assets override is required.
androidComponents {
    onVariants(selector().all()) { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(copyCmpAssets) { it.destinationDirectory }
    }
}

dependencies {
    implementation(project(":shared:ui"))
    implementation(project(":shared:data"))
    implementation(project(":shared:domain"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(compose.components.resources)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.roborazzi.preview.scanner)
    testImplementation(libs.preview.scanner.compose)
    testImplementation(libs.robolectric)
    testImplementation(compose.components.resources)
}

roborazzi {
    generateComposePreviewRobolectricTests {
        enable = true
        includePrivatePreviews = false
        packages = listOf("com.ultraviolince.mykitchen.ui")
    }
}
