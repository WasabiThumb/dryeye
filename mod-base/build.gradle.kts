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
    compileOnly(vanillaClient("${libs.minecraft.get().version}")) // Reference vanilla client symbols
    compileOnly(libs.fabric.mixin)                                         // Reference mixin symbols
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
