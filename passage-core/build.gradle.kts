import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
}

android {
    namespace = "com.tweener.passage.core"
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
            baseName = "passage-core"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.coroutines.core)

            // Compose
            implementation(compose.runtime)
        }

        androidMain.dependencies {
            // Compose (for LocalContext, rememberLauncherForActivityResult)
            implementation(compose.foundation)
            implementation(libs.android.activity.compose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlin.coroutines.test)
        }
    }
}
