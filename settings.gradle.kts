pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") // Fabric
        maven("https://maven.neoforged.net/releases") // NeoForge
        gradlePluginPortal()
    }
}

rootProject.name = "dryeye"

include(
    ":common",
    ":app",
    ":app:bootstrap",
    ":mod-base",
    ":fabric",
    ":neoforge"
)
