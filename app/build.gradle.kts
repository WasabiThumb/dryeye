
plugins {
    id("java")
    alias(libs.plugins.indra.base)
    alias(libs.plugins.indra.git)
    alias(libs.plugins.shadow)
}

description = "GUI for the DryEye core image algorithm"
val main = "io.github.wasabithumb.dryeye.app.Bootstrap"
val releaseName = "dryeye-app"
val releaseVersion = if (!indraGit.isPresent || "master" == indraGit.branchName().orNull) {
    "${rootProject.version}"
} else {
    "${rootProject.version}+${indraGit.commit().get().abbreviate(7).name()}"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":app:bootstrap"))
    implementation(libs.jspecify)
    implementation(libs.jetbrains.annotations)
    implementation(libs.gson)
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

tasks.sourcesJar {
    enabled = false
}

tasks.javadocJar {
    enabled = false
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
    manifest.attributes["Main-Class"] = main
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.register("launch", JavaExec::class) {
    dependsOn(tasks.build)
    classpath = tasks.shadowJar.get().outputs.files
    mainClass = main
}
