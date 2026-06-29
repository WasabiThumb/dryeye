allprojects {
    group = "io.github.wasabithumb"
    version = "0.1.1"
}

plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
}

repositories {
    mavenCentral()
}

indra.javaVersions {
    target(25)
}
