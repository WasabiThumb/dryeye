pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") // Fabric
        gradlePluginPortal()
    }
}

rootProject.name = "dryeye"

include(
    ":common",
    ":app",
    ":app:bootstrap",
    ":fabric"
)
