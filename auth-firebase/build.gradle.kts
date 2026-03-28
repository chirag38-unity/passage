import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.kotlin.nativeCocoaPods)
}

android {
    namespace = "com.tweener.passage.auth.firebase"
    compileSdk = ProjectConfiguration.Passage.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.Passage.minSDK
    }

    buildFeatures {
        compose = true
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
            baseName = "auth-firebase"
            isStatic = true
        }
    }

    cocoapods {
        ios.deploymentTarget = ProjectConfiguration.iOS.deploymentTarget

        pod("GoogleSignIn")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":passage-core"))

            // Tweener
            implementation(libs.kmpkit)

            // Coroutines
            implementation(libs.kotlin.coroutines.core)

            // Firebase
            implementation(libs.firebase.auth)

            // Compose
            implementation(compose.runtime)
        }

        androidMain.dependencies {
            // Coroutines
            implementation(libs.kotlin.coroutines.android)

            // Google Sign In
            implementation(libs.bundles.googleSignIn)
            implementation(libs.android.activity.compose)
        }

        iosMain.dependencies {

        }
    }
}
