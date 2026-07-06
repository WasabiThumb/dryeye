import tasks.TransformResourceBundlesTask

plugins {
    id("java-library")
    alias(libs.plugins.indra.base)
    alias(libs.plugins.ttdev)
}

description = "Minecraft client mod for automatic skin blinking (Base)"

repositories {
    mavenCentral()
    ttdev.repository()
}

dependencies {
    compileOnly(ttdev.minecraftClient(libs.versions.minecraft))
    compileOnly(libs.fabric.mixin)
    implementation(project(":common"))
    implementation(libs.jtoml.base)
    implementation(libs.jtoml.reflect)
}

indra {
    github("WasabiThumb", "dryeye")
    apache2License()
    javaVersions {
        target(25)
    }
    configurePublications {
        artifactId = "dryeye-mod-base"
    }
}

val transformResourceBundles = tasks.register("transformResourceBundles", TransformResourceBundlesTask::class) {
    description = "Extracts the translation strings from the common project and transforms it into the format Minecraft expects"

    val common = project(":common")
    dependsOn(common.tasks.processResources)

    bundlesDir = common.layout.buildDirectory.dir("resources/main/io/github/wasabithumb/dryeye/bundle")
    transformer = TransformResourceBundlesTask.Transformer.JSON
}

tasks.processResources {
    // Add translations
    dependsOn(transformResourceBundles)
    into("assets/dryeye/lang") {
        from(transformResourceBundles.map { it.output })
    }
}
