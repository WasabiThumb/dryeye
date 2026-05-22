import tasks.FlattenConfigurationTask
import tasks.TransformResourceBundlesTask

plugins {
    id("java-library")
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.indra.base)
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
    add("peer", project(":common"))
    add("peer", libs.jtoml.base)
    add("peer", libs.jtoml.reflect)
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

indra.javaVersions {
    target(25)
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
    transformer = TransformResourceBundlesTask.Transformer.FABRIC
}

tasks.processResources {
    // Inject version
    filesMatching("fabric.mod.json") {
        expand("version" to "${project.version}")
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
    archiveBaseName = "dryeye-fabric"
}

tasks.javadocJar {
    enabled = false
}

tasks.sourcesJar {
    enabled = false
}

