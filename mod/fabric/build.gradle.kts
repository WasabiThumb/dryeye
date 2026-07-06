import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
    alias(libs.plugins.indra.git)
    alias(libs.plugins.ttdev)
    alias(libs.plugins.resource.factory.fabric)
    alias(libs.plugins.shadow)
}

description = "Minecraft client mod for automatic skin blinking (Fabric)"
val releaseName = "dryeye-fabric"
val releaseVersion = if (!indraGit.isPresent || "master" == indraGit.branchName().orNull) {
    "${rootProject.version}"
} else {
    "${rootProject.version}+${indraGit.commit().get().abbreviate(7).name()}"
}

repositories {
    mavenCentral()
    ttdev.repository()
    maven("https://maven.terraformersmc.com/") // modmenu
}

dependencies {
    compileOnly(ttdev.minecraftClient(libs.versions.minecraft))
    compileOnly(ttdev.fabricLoader(libs.versions.fabric.loader))
    compileOnly(ttdev.fabricApi(libs.versions.fabric.api))
    compileOnly(libs.fabric.modmenu)
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

fabricModJson {
    id = "dryeye"
    version = releaseVersion
    name = "DryEye"
    description = "${project.description}"
    environment = Environment.CLIENT
    icon("assets/dryeye/icon.png")
    author("Xavier Pedraza")
    license("Apache-2.0")
    entrypoint("client", "io.github.wasabithumb.dryeye.FabricDryEyeMod")
    entrypoint("modmenu", "io.github.wasabithumb.dryeye.integrations.DryEyeModMenuApiImpl")
    depends("fabricloader", ">=0.19.0")
    depends("minecraft", "~26.1", "~26.2-alpha", "~26.2")
    depends("java", ">=25")
    depends("fabric-api", "*")
    mixin("dryeye.mixins.json") { environment = Environment.CLIENT }
    contact {
        homepage = "https://modrinth.com/project/4gxXfaZG"
        sources = "https://github.com/WasabiThumb/dryeye"
        issues = "https://github.com/WasabiThumb/dryeye/issues"
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
