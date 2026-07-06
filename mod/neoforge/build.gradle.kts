plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
    alias(libs.plugins.indra.git)
    alias(libs.plugins.ttdev)
    alias(libs.plugins.resource.factory.neoforge)
    alias(libs.plugins.shadow)
}

description = "Minecraft client mod for automatic skin blinking (NeoForge)"
val releaseName = "dryeye-neoforge"
val releaseVersion = if (!indraGit.isPresent || "master" == indraGit.branchName().orNull) {
    "${rootProject.version}"
} else {
    "${rootProject.version}+${indraGit.commit().get().abbreviate(7).name()}"
}

repositories {
    mavenCentral()
    ttdev.repository()
}

dependencies {
    compileOnly(ttdev.minecraftClient(libs.versions.minecraft))
    compileOnly(ttdev.neoForge(libs.versions.neoforge))
    implementation(project(":mod:base"))
    implementation(libs.jspecify)
    implementation(libs.jetbrains.annotations)
}

indra {
    github("WasabiThumb", "dryeye")
    apache2License()
    javaVersions {
        target(25)
    }
    configurePublications {
        artifactId = releaseName
    }
}

neoForgeModsToml {
    apache2License()
    mod("dryeye") {
        displayName = "DryEye"
        displayUrl = "https://modrinth.com/project/4gxXfaZG"
        logoFile = "assets/dryeye/icon.png"
        version = releaseVersion
        description = "${project.description}"
        authors = "Xavier Pedraza"
        mixin("dryeye.mixins.json")
        dependencies {
            required("neoforge", "[26.1.2.71,)")
            required("minecraft", "[26.1,${libs.versions.minecraft.get()}]")
        }
    }
}

tasks.jar {
    archiveBaseName = releaseName
    archiveVersion = releaseVersion
}

tasks.shadowJar {
    archiveClassifier = ""
    archiveBaseName = releaseName
    archiveVersion = releaseVersion
    indraGit.applyVcsInformationToManifest(manifest)
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.javadocJar {
    enabled = false
}

tasks.sourcesJar {
    enabled = false
}
