import ext.vanillaClient

plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
}

description = "Minecraft client mod for automatic skin blinking"

repositories {
    mavenCentral()
}

dependencies {
    // Reference vanilla client symbols
    compileOnly(vanillaClient("${libs.minecraft.get().version}") {
        useLibrary("it.unimi.dsi:fastutil")
        useLibrary("org.slf4j:slf4j-api")
        useLibrary("com.mojang:brigadier")
    })
    compileOnly(libs.fabric.mixin) // Reference mixin symbols
    implementation(project(":common"))
    implementation(libs.jtoml.base)
    implementation(libs.jtoml.reflect)
}

indra {
    github("WasabiThumb", "dryeye")
    apache2License()
    javaVersions {
        target(25)
    }
    configurePublications {
        artifactId = "dryeye-mod-base"
    }
}
