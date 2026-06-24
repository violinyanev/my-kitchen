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
// directory and register it via sourceSets.assets.srcDir() (enabled by
// android.sourceset.disallowProvider=false in gradle.properties) for both the main source set
// (APK → instrumented tests) and the test source set (mergeDebugUnitTestAssets → Robolectric).
// Explicit dependsOn below compensates for the disabled auto-tracking.
// Note: cmpAssetsDir is captured as a plain Provider<Directory> (not derived from the task
// provider) so AGP can evaluate it eagerly under disallowProvider=false without realizing
// copyCmpAssetsForAndroid during configuration — avoiding cross-project tasks.named() failures.
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
        // CMP-9547: wire copyCmpAssetsForAndroid into main (APK → instrumented tests) and test
        // (mergeDebugUnitTestAssets → Robolectric android.merged_assets) source sets.
        // Provider-based srcDir() is allowed by android.sourceset.disallowProvider=false in
        // gradle.properties; explicit dependsOn below compensates for disabled auto-tracking.
        getByName("main") {
            assets.srcDir(cmpAssetsDir)
        }
        getByName("test") {
            assets.srcDir(cmpAssetsDir)
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

// android.sourceset.disallowProvider=false (gradle.properties) disables automatic Gradle task
// dependency tracking when Provider<Directory> is used in sourceSets.assets.srcDir(). Wire
// copyCmpAssetsForAndroid explicitly into every merge*Assets task so that AGP runs it before
// merging assets for production APKs, instrumented tests, and Robolectric unit tests.
// tasks.configureEach (not tasks.matching) is truly lazy: it never eagerly realizes tasks.
tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("Assets")) {
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
