plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
    alias(libs.plugins.jakery)
}

description = "DryEye core components used by the client mod and external tooling"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.jspecify)
    api(libs.jetbrains.annotations)
    implementation(libs.jakery.runtime)
}

indra {
    github("WasabiThumb", "dryeye")
    apache2License()
    javaVersions {
        target(25)
    }
    configurePublications {
        artifactId = "dryeye-common"
    }
}

jakery {
    autoRuntimeDependency = false
}
