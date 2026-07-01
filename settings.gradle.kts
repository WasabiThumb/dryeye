pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") // Fabric
        maven("https://maven.neoforged.net/releases") // NeoForge
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "dryeye"

include(
    ":common",
    ":app",
    ":app:bootstrap",
    ":mod-base",
    ":fabric",
    ":neoforge",
    ":forge"
)
