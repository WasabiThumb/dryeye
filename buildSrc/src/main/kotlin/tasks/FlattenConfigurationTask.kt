package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.function.Consumer
import kotlin.io.path.absolute

@CacheableTask
abstract class FlattenConfigurationTask : DefaultTask() {

    @get:InputFiles
    @get:Classpath
    abstract val configuration: Property<FileCollection>

    @get:Input
    abstract val exclusions: SetProperty<String>

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    init {
        this.output.convention(this.project.layout.buildDirectory.dir("tmp/${this.name}"))
        this.exclusions.convention(setOf("META-INF/MANIFEST.MF"))
    }

    //

    @TaskAction
    fun flatten() {
        val files = this.configuration.get()
        val dest = this.output.get().asFile.toPath()
        val fileLog: MutableSet<String> = mutableSetOf()
        if (!Files.exists(dest)) Files.createDirectories(dest)
        for (file in files) {
            val path = file.toPath()
            this.logger.lifecycle("extracting archive: ${path.absolute()}")
            FileSystems.newFileSystem(path).use { fs ->
                this.flattenAt("", fs.getPath(""), dest, fileLog)
            }
        }
    }

    private fun flattenAt(prefix: String, src: Path, dest: Path, fileLog: MutableSet<String>) {
        traverse(src) {
            val name = it.fileName.toString()
            val entry = prefix + name
            if (this.exclusions.get().contains(entry)) return@traverse
            val target = dest.resolve(name)
            if (Files.isDirectory(it)) {
                if (!Files.isDirectory(target)) Files.createDirectories(target)
                this.flattenAt("$entry/", it, target, fileLog)
            } else {
                if (!fileLog.add(entry)) {
                    if (!entry.startsWith("META-INF/services/")) throw IllegalStateException("duplicate file: $entry")
                    Files.newOutputStream(target, StandardOpenOption.WRITE, StandardOpenOption.APPEND).use { targetStream ->
                        Files.newInputStream(it).use { sourceStream ->
                            targetStream.write('\n'.code)
                            val buf = ByteArray(8192)
                            var read: Int
                            while (true) {
                                read = sourceStream.read(buf)
                                if (read == -1) break
                                targetStream.write(buf, 0, read)
                            }
                            targetStream.flush()
                        }
                    }
                } else {
                    Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }

    //

    companion object {

        private fun traverse(dir: Path, action: Consumer<Path>) {
            Files.list(dir).use { stream ->
                for (path in stream) action.accept(path)
            }
        }

    }

}