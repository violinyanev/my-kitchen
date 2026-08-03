pluginManagement {
    includeBuild("build-logic")
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "io.ktor.plugin") {
                useModule("io.ktor:ktor-gradle-plugin:${requested.version}")
            }
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "my-kitchen"

include(":shared:domain")
include(":shared:data")
include(":shared:ui")
include(":androidApp")
include(":desktopApp")
include(":webApp")
include(":server")
include(":macrobenchmark")
