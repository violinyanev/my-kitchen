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
// directory and wire them into the build via two complementary paths:
//   1. androidComponents.onVariants + addGeneratedSourceDirectory → covers all production
//      variants (debug, release) via mergeDebugAssets / mergeReleaseAssets. This is the AGP 9.x
//      preferred API and correctly handles both debug and release APK packaging.
//   2. sourceSets["test"].assets.srcDir(File) + explicit dependsOn → covers the unit-test
//      variant (mergeDebugUnitTestAssets → Robolectric android.merged_assets). AGP 9.x exposes
//      variant.sources.assets == null for the unit-test component, so addGeneratedSourceDirectory
//      cannot be used there; the old sourceSets API is the only viable hook.
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

val cmpAssetsDir = layout.buildDirectory.dir("generated/cmp-assets")

val copyCmpAssets = tasks.register<CopyDirTask>("copyCmpAssetsForAndroid") {
    // Declare explicit dependencies on all three CMP tasks that write to preparedResources/commonMain.
    // This block is lazy (runs after all projects configure), so shared:ui tasks are already registered.
    dependsOn(
        project(":shared:ui").tasks.named("prepareComposeResourcesTaskForCommonMain"),
        project(":shared:ui").tasks.named("convertXmlValueResourcesForCommonMain"),
        project(":shared:ui").tasks.named("copyNonXmlValueResourcesForCommonMain"),
    )
    sourceDirectory.set(
        project(":shared:ui").layout.buildDirectory
            .dir("generated/compose/resourceGenerator/preparedResources/commonMain"),
    )
    destinationDirectory.set(cmpAssetsDir)
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

    sourceSets {
        // CMP-9547 path 2: wire CMP assets into the unit-test source set so that
        // mergeDebugUnitTestAssets (Robolectric android.merged_assets) picks them up.
        // srcDir(File) is safe here because layout.buildDirectory is always resolved at
        // configuration time. Explicit dependsOn in tasks.configureEach below handles the
        // missing task-dependency that srcDir(File) does not carry automatically.
        getByName("test") {
            assets.srcDir(cmpAssetsDir.get().asFile)
        }
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

// CMP-9547 path 1: production variants (debug, release APK, instrumented tests).
// addGeneratedSourceDirectory wires the task dependency automatically and is the AGP 9.x
// preferred API. variant.sources.assets is non-null for application variants.
androidComponents {
    onVariants(selector().all()) { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(copyCmpAssets) { it.destinationDirectory }
    }
}

// CMP-9547 path 2: srcDir(File) on sourceSets["test"] (above) doesn't carry task-dependency
// information, so explicitly wire copyCmpAssetsForAndroid before every unit-test asset merge.
tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("UnitTestAssets")) {
        dependsOn(copyCmpAssets)
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
