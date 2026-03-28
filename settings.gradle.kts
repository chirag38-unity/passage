pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "Passage"
include(":passage")
include(":passage-core")
include(":auth-firebase")
include(":auth-supabase")
include(":auth-custom")
include(":sample:shared")
include(":sample:androidApp")
