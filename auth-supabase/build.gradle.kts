import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.tweener.passage.auth.supabase"
    compileSdk = ProjectConfiguration.Passage.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.Passage.minSDK
    }

    compileOptions {
        sourceCompatibility = ProjectConfiguration.Compiler.javaCompatibility
        targetCompatibility = ProjectConfiguration.Compiler.javaCompatibility
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(ProjectConfiguration.Compiler.jvmTarget))
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "auth-supabase"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":passage-core"))

            // Coroutines
            implementation(libs.kotlin.coroutines.core)

            // Supabase
            implementation(platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
        }

        androidMain.dependencies {
            // Ktor engine for Android
            implementation(libs.ktor.client.android)
        }

        iosMain.dependencies {
            // Ktor engine for iOS
            implementation(libs.ktor.client.darwin)
        }
    }
}
