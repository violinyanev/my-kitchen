plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.roborazzi) apply false
}

// Aggregate unit-test coverage from the modules that have tests.
// shared:ui is intentionally excluded — it has no unit tests.
dependencies {
    kover(project(":shared:domain"))
    kover(project(":shared:data"))
    kover(project(":server"))
}

kover {
    reports {
        filters {
            excludes {
                // DI wiring: Koin module declarations have no logic to test
                classes("*.di.*Module*", "*.di.*ModuleKt")
                // Platform DB/driver factories: delegate to Room internals
                classes("*.data.local.*Database*", "*.data.local.*DatabaseKt")
                // Generated Room DAO implementations (KSP output) — untestable generated code
                classes("*.data.local.*_Impl*")
                // Room DAO interfaces (old androidx.room and KMP androidx.room3)
                annotatedBy("androidx.room.Dao")
                annotatedBy("androidx.room3.Dao")
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        config.setFrom(rootProject.files("gradle/detekt.yml"))
        buildUponDefaultConfig = true
    }

    val catalog = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies {
        "detektPlugins"(catalog.findLibrary("detekt-formatting").get())
    }
}
