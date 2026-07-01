import tasks.FlattenConfigurationTask
import tasks.TransformResourceBundlesTask

plugins {
    id("java-library")
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.indra.base)
    alias(libs.plugins.indra.git)
}

description = "Minecraft client mod for automatic skin blinking (Forge)"
val releaseName = "dryeye-forge"
val releaseVersion = if (!indraGit.isPresent || "master" == indraGit.branchName().orNull) {
    "${rootProject.version}"
} else {
    "${rootProject.version}+${indraGit.commit().get().abbreviate(7).name()}"
}
val minecraftVersion = libs.versions.minecraft.get()
val forgeVersion = libs.versions.forge.asProvider().get()

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

repositories {
    mavenCentral()

    // Forge
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

configurations {
    val peer = register("peer") { isCanBeResolved = true }
    compileClasspath { extendsFrom(peer) }
}

dependencies {
    add("peer", project(":mod-base"))
    implementation(libs.jspecify)
    implementation(libs.jetbrains.annotations)

    // Forge
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    implementation(minecraft.dependency("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")) {
        exclude("net.minecraftforge", "JarJarSelector")
        exclude("net.minecraftforge", "JarJarMetadata")
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

minecraft {
    mappings("official", minecraftVersion)
    runs {
        configureEach {
            workingDir.convention(layout.projectDirectory.dir("run"))
            args("--mixin.config=dryeye.mixins.json")
        }
        register("client") {
            systemProperty("forge.enabledGameTestNamespaces", "dryeye")
        }
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
        "io/github/wasabithumb/dryeye/bundle",
        "org"
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
    // Inject variables
    val props = mapOf(
        "mod_version" to releaseVersion,
        "mc_version" to minecraftVersion
    )
    inputs.properties(props)
    filesMatching("**/mods.toml") {
        expand(props)
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
    manifest.attributes["MixinConfigs"] = "dryeye.mixins.json"
}

tasks.javadocJar {
    enabled = false
}

tasks.sourcesJar {
    enabled = false
}
