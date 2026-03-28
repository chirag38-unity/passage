plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.nativeCocoaPods)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
}

android {
    namespace = ProjectConfiguration.Passage.packageName + ".firebase"
    compileSdk = ProjectConfiguration.Passage.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.Passage.minSDK
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") { }
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
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfiguration.Compiler.jvmTarget))
        }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "passage-auth-firebase"
            isStatic = true
        }
    }

    cocoapods {
        ios.deploymentTarget = ProjectConfiguration.iOS.deploymentTarget
        pod("GoogleSignIn")
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":passage-core"))
            implementation(libs.android.annotations)
            implementation(libs.kmpkit)
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.firebase.auth)
            implementation(compose.foundation)
        }

        androidMain.dependencies {
            implementation(libs.kotlin.coroutines.android)
            implementation(libs.android.core)
            implementation(libs.bundles.googleSignIn)
            implementation(libs.android.activity.compose)
        }

        iosMain.dependencies { }
    }
}
