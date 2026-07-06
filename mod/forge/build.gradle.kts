plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
    alias(libs.plugins.indra.git)
    alias(libs.plugins.ttdev)
    alias(libs.plugins.shadow)
}

description = "Minecraft client mod for automatic skin blinking (Forge)"
val releaseName = "dryeye-forge"
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
    compileOnly(ttdev.forge(libs.versions.forge))
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

tasks.processResources {
    // Inject variables
    val props = mapOf(
        "mod_version" to releaseVersion,
        "mc_version" to libs.versions.minecraft.get()
    )
    inputs.properties(props)
    filesMatching("**/mods.toml") {
        expand(props)
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
    manifest.attributes["MixinConfigs"] = "dryeye.mixins.json"

    // Forge doesn't like us shading annotations because... something something JPMS?
    // This doesn't break anything because the annotations don't exist at runtime, but it's strange.
    exclude("org/intellij/lang/annotations/**/*", "org/jetbrains/**/*", "org/jspecify/**/*")
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
