package ext

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import tasks.FetchVanillaClientTask

fun Project.vanillaClient(version: String): FileCollection {
    return this.vanillaClient(version) { }
}

fun Project.vanillaClient(version: String, configure: Action<FetchVanillaClientTask>): FileCollection {
    val task = this.tasks.register("fetchVanillaClient", FetchVanillaClientTask::class.java) {
        this.version.set(version)
        configure.execute(this)
    }
    return files(task.map { it.output.asFileTree })
}
