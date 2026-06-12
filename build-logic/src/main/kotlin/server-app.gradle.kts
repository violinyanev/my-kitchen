plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.ktor.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlinx.kover")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
