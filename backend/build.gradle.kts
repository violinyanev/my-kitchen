plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    
    // Serialization
    implementation(libs.ktor.serialization.json)
    
    // YAML and JSON Schema
    implementation(libs.snakeyaml)
    implementation(libs.json.schema.validator)
    
    // Logging
    implementation(libs.logback.classic)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    
    // Code quality
    detektPlugins(libs.detektTwitterPlugin)
    detektPlugins(libs.detektFormattingPlugin)
}

application {
    mainClass.set("com.ultraviolince.mykitchen.backend.ApplicationKt")
    applicationName = "backend"
}

tasks.test {
    useJUnit()
}

detekt {
    config.setFrom("$rootDir/gradle/detekt.yml")
    buildUponDefaultConfig = true
    autoCorrect = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    setSource(files("src/main/kotlin", "src/test/kotlin"))
    exclude("**/build/**")
}

