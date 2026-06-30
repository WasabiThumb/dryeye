allprojects {
    group = "io.github.wasabithumb"
    version = "0.2.0"
}

plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
    alias(libs.plugins.indra.git)
}

repositories {
    mavenCentral()
}

indra.javaVersions {
    target(25)
}
