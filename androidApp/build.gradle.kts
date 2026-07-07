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
// KotlinMultiplatformAndroidVariant.sources.assets == null in AGP 9.x, so CMP 1.11.1's
// copyAndroidMainComposeResourcesToAndroidAssets task fails ("outputDirectory doesn't have a
// configured value") and the generated .cvr files never reach the Android assets. Work around
// this by copying shared:ui's prepared resources into a local directory and registering it as a
// generated asset source on androidApp (which uses com.android.application and is unaffected).
//
// Critical detail: the CMP runtime (DefaultAndroidResourceReader) looks the files up under
//   composeResources/<packageOfResClass>/values/strings.commonMain.cvr
// but prepareComposeResourcesTaskForCommonMain lays them out WITHOUT the package segment:
//   composeResources/values/strings.commonMain.cvr
// So the copy must re-insert the package directory, otherwise the resource is "missing" at
// runtime in both the APK and Robolectric unit tests (the bug that caused MissingResourceException).
abstract class CopyDirTask @Inject constructor(
    private val fileSystemOperations: FileSystemOperations,
) : DefaultTask() {
    // The shared:ui preparedResources/commonMain/composeResources directory (values/, values-de/…).
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    // The packageOfResClass that CMP's runtime expects between composeResources/ and values/.
    @get:Input
    abstract val resourcePackage: Property<String>

    // The assets root that gets registered as a generated asset source. Files are written under
    // composeResources/<package>/ inside it so the final asset path matches what CMP requests.
    @get:OutputDirectory
    abstract val destinationDirectory: DirectoryProperty

    @TaskAction
    fun copy() {
        fileSystemOperations.sync {
            from(sourceDirectory)
            into(destinationDirectory.get().dir("composeResources").dir(resourcePackage.get()))
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
    // Source is the composeResources/ dir itself (containing values/, values-de/…) so that the
    // task action can re-root it under composeResources/<package>/ in the destination.
    sourceDirectory.set(
        project(":shared:ui").layout.buildDirectory
            .dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"),
    )
    // Must match shared:ui's compose.resources.packageOfResClass.
    resourcePackage.set("com.ultraviolince.mykitchen.ui.generated.resources")
    destinationDirectory.set(cmpAssetsDir)
}

android {
    namespace = "com.ultraviolince.mykitchen"
    compileSdk = libs.versions.compileSdk.get().toInt()

    buildFeatures {
        // AGP 9.x disables resValues by default; build types use resValue() for
        // per-variant app names, so this must be explicitly enabled.
        resValues = true
    }

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
            resValue("string", "app_name", "My Kitchen")
        }
        create("preview") {
            initWith(getByName("release"))
            applicationIdSuffix = ".preview"
            resValue("string", "app_name", "Kitchen on fire")
        }
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Kitchen on fire")
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

// CMP-9547: register the copied assets as a generated asset source on every application variant.
// addGeneratedSourceDirectory wires the task dependency automatically and is the AGP 9.x preferred
// API. variant.sources.assets is non-null for application variants, so this feeds mergeDebugAssets /
// mergeReleaseAssets — which in turn feed the APK, instrumented tests, and (via
// packageDebugUnitTestForUnitTest) the Robolectric unit-test classpath. A single registration on
// the production variant therefore covers production builds and unit tests alike.
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
    // Store golden screenshots under the version-controlled directory that the verify-screenshots
    // workflow commits (androidApp/src/test/screenshots). The roborazzi plugin otherwise defaults
    // to build/outputs/roborazzi (a build dir), which would never be committed — so references
    // could never be recorded and verification would fail forever. The raw
    // testOptions roborazzi.output.dir system property does not work because the plugin overrides
    // it; outputDir is the supported DSL hook.
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
    generateComposePreviewRobolectricTests {
        enable = true
        includePrivatePreviews = false
        packages = listOf("com.ultraviolince.mykitchen.ui")
    }
}
