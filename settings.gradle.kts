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
include(":passage-core")
include(":passage-auth-firebase")
include(":passage-auth-supabase")
include(":passage")
include(":sample:shared")
include(":sample:androidApp")
