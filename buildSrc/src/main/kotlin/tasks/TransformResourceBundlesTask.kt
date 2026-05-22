package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.Locale
import java.util.Properties
import java.util.regex.Pattern

@CacheableTask
abstract class TransformResourceBundlesTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val bundlesDir: DirectoryProperty

    @get:Input
    abstract val transformer: Property<Transformer>

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    init {
        this.output.convention(this.project.layout.buildDirectory.dir("tmp/${this.name}"))
    }

    //

    @TaskAction
    fun transform() {
        val transformer = this.transformer.get().instance as TransformerInstance
        val src = this.bundlesDir.get().asFile.toPath()
        val dest = this.output.get().asFile.toPath()
        if (!Files.isDirectory(dest)) Files.createDirectory(dest)

        Files.list(src).use { stream ->
            for (file in stream) {
                if (!Files.isRegularFile(file)) continue
                val name = file.fileName.toString()
                val matcher = BUNDLE_PATTERN.matcher(name)
                if (!matcher.matches()) continue

                val locale = matcher.group(1)
                this.logger.lifecycle("transforming resource bundle: $locale")

                val data = Files.newInputStream(file).use {
                    val properties = Properties()
                    properties.load(InputStreamReader(it, StandardCharsets.UTF_8))
                    properties
                }

                val target = dest.resolve(transformer.fileName(locale))
                Files.newOutputStream(
                    target,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
                ).use {
                    val writer = OutputStreamWriter(it, StandardCharsets.UTF_8)
                    transformer.write(data, writer)
                    writer.flush()
                }
            }
        }
    }

    //

    companion object {
        private val BUNDLE_PATTERN = Pattern.compile("^Bundle_([a-zA-Z_]+)\\.properties$")
    }

    enum class Transformer(
        val instance: Any
    ) {
        FABRIC(TransformerInstance.Fabric)
    }

    private sealed interface TransformerInstance {

        fun fileName(locale: String): String

        fun write(data: Properties, out: Writer)

        //

        object Fabric : TransformerInstance {

            override fun fileName(locale: String): String {
                return "${locale.lowercase(Locale.ROOT)}.json"
            }

            override fun write(data: Properties, out: Writer) {
                out.write("{\n")

                val iter = data.keys.iterator()
                while (iter.hasNext()) {
                    val key = "${iter.next()}"
                    val value = data.getProperty(key)

                    out.write("    ")
                    this.writeString(out, key)
                    out.write(": ")
                    this.writeString(out, value)
                    if (iter.hasNext()) out.write(','.code)
                    out.write('\n'.code)
                }

                out.write("}\n")
            }

            private fun writeString(out: Writer, value: String) {
                out.write('"'.code)
                for (c in value) {
                    if (c == '"') out.write('\\'.code)
                    out.write(c.code)
                }
                out.write('"'.code)
            }

        }

    }

}