import tasks.FlattenConfigurationTask
import tasks.TransformResourceBundlesTask

plugins {
    id("java-library")
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.indra.base)
    alias(libs.plugins.indra.git)
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
    maven("https://maven.terraformersmc.com/") // modmenu
}

configurations {
    val peer = register("peer") { isCanBeResolved = true }
    compileClasspath { extendsFrom(peer) }
}

dependencies {
    add("peer", project(":mod-base"))
    implementation(libs.jspecify)
    implementation(libs.jetbrains.annotations)

    // Fabric
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(libs.fabric.modmenu) // modmenu
}

loom {
    splitEnvironmentSourceSets()
    mods {
        register("dryeye") {
            sourceSet("main")
            sourceSet("client")
        }
    }
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

// Can't simply use the shade plugin due to
// the build time shenanigans of Fabric
val flattenPeers = tasks.register("flattenPeers", FlattenConfigurationTask::class) {
    description = "Extracts the classes and resources from peer dependencies for shading into the mod JAR"

    configuration.set(configurations.named("peer"))
    exclusions = setOf(
        "META-INF/MANIFEST.MF",
        "META-INF/versions",
        "io/github/wasabithumb/dryeye/bundle"
    )
}

val transformResourceBundles = tasks.register("transformResourceBundles", TransformResourceBundlesTask::class) {
    description = "Extracts the translation strings from the common project and transforms it into the format Fabric expects"

    val common = project(":common")
    dependsOn(common.tasks.processResources)

    bundlesDir = common.layout.buildDirectory.dir("resources/main/io/github/wasabithumb/dryeye/bundle")
    transformer = TransformResourceBundlesTask.Transformer.JSON
}

tasks.processResources {
    // Inject version
    filesMatching("fabric.mod.json") {
        expand("version" to releaseVersion)
    }

    // Shade peer dependencies
    dependsOn(flattenPeers)
    from(flattenPeers.map { it.output })

    // Add translations
    dependsOn(transformResourceBundles)
    into("assets/dryeye/lang") {
        from(transformResourceBundles.map { it.output })
    }
}

tasks.jar {
    archiveBaseName = releaseName
    archiveVersion = releaseVersion
    indraGit.applyVcsInformationToManifest(manifest)
}

tasks.javadocJar {
    enabled = false
}

tasks.sourcesJar {
    enabled = false
}

