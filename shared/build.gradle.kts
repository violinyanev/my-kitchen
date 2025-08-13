import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    // TODO: Re-enable JS target once repository issues are resolved
    // js(IR) {
    //     browser {
    //         commonWebpackConfig {
    //             cssSupport {
    //                 enabled.set(true)
    //             }
    //         }
    //     }
    //     binaries.executable()
    // }
    
    sourceSets {
        commonMain.dependencies {
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            
            // Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.client.logging)
            
            // Dependency Injection
            implementation(libs.koin.core)
            
            // Date/Time
            implementation(libs.kotlinx.datetime)
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        
        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        
        // TODO: Re-enable JS dependencies when JS target is enabled
        // jsMain.dependencies {
        //     implementation(libs.ktor.client.js)
        // }
    }
}

android {
    namespace = "com.ultraviolince.mykitchen.shared"
    compileSdk = libs.versions.compileSdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}