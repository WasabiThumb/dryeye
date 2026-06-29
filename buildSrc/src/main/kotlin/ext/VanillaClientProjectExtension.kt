package ext

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import tasks.FetchVanillaClientTask

fun Project.vanillaClient(version: String): FileCollection {
    val task = this.tasks.register("fetchVanillaClient", FetchVanillaClientTask::class.java) {
        this.version.set(version)
    }
    return files(task.flatMap { it.output.asFile })
}
