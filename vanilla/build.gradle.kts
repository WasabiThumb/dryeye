import ext.vanillaClient

plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
}

repositories {
    mavenCentral()
}

dependencies {
    // Add vanilla client to compile classpath
    compileOnly(vanillaClient("${libs.minecraft.get().version}"))
}

indra.javaVersions {
    target(25)
}
