
plugins {
    id("java")
    alias(libs.plugins.indra.base)
    alias(libs.plugins.shadow)
}

val main = "io.github.wasabithumb.dryeye.app.Bootstrap"

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

indra.javaVersions {
    target(25)
}

tasks.sourcesJar {
    enabled = false
}

tasks.javadocJar {
    enabled = false
}

tasks.jar {
    archiveBaseName = "dryeye-app"
}

tasks.shadowJar {
    archiveClassifier = ""
    archiveBaseName = "dryeye-app"
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
