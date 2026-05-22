
plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
}

repositories {
    mavenCentral()
}

indra.javaVersions {
    target(8)
    minimumToolchain(25)
}
