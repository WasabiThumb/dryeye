plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
    alias(libs.plugins.jakery)
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.jspecify)
    api(libs.jetbrains.annotations)
    implementation(libs.jakery.runtime)
}

indra.javaVersions {
    target(25)
}

jakery {
    autoRuntimeDependency = false
}
