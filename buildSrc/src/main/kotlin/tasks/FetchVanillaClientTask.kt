package tasks

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import util.Sha1PassthroughInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

@CacheableTask
abstract class FetchVanillaClientTask : DefaultTask() {

    @get:Input
    abstract val version: Property<String>

    @get:Nested
    abstract val libraries: SetProperty<Library>

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    init {
        this.libraries.convention(setOf())
        this.output.convention(this.project.layout.buildDirectory.dir("tmp/${this.name}"))
    }

    //

    @TaskAction
    fun fetch() {
        val version = this.version.get()
        val dir = this.output.get().asFile.toPath()
        val libs = this.libraries.get()
        if (!Files.exists(dir)) Files.createDirectories(dir)

        this.logger.lifecycle("fetching client: $version")
        for (info in fetchArtifactInfo(version)) {
            if (Library.CLIENT != info.library && !libs.contains(info.library)) {
                continue
            }

            val dest = dir.resolve(info.name)
            val sha1 = info.sha1.hexToByteArray()
            this.logger.lifecycle("- ${info.name}")

            // Check hash
            if (Files.exists(dest)) {
                val fileHash = sha1File(dest)
                if (sha1.contentEquals(fileHash)) {
                    this.logger.lifecycle("hash matched, skipping")
                    continue
                }
            }

            // Download
            val c = httpConnection(info.url)
            val ok = c.getInputStream().use { raw ->
                val src = Sha1PassthroughInputStream(raw)
                Files.newOutputStream(dest).use { out ->
                    val buf = ByteArray(8192)
                    var read: Int
                    while (true) {
                        read = src.read(buf)
                        if (read == -1) break
                        out.write(buf, 0, read)
                    }
                    sha1.contentEquals(src.digest())
                }
            }

            if (!ok) {
                Files.deleteIfExists(dest)
                throw IllegalStateException("failed to download (hash mismatch)")
            }
        }
    }

    fun useLibrary(libraryNotation: String) {
        this.libraries.add(Library.fromNotation(libraryNotation))
    }

    //


    data class Library(
        @get:Input val group: String,
        @get:Input val name: String
    ) {

        companion object {

            val CLIENT = Library("net.minecraft", "client")

            fun fromNotation(notation: String): Library {
                val split = notation.split(':')
                if (split.size < 2) throw IllegalArgumentException()
                return Library(split[0], split[1])
            }

        }

    }

    private data class ArtifactInfo(
        val library: Library,
        val name: String,
        val url: String,
        val sha1: String
    )

    companion object {

        private val GSON: Gson = Gson()

        private fun sha1File(path: Path): ByteArray {
            return Files.newInputStream(path).use {
                val stream = Sha1PassthroughInputStream(it)
                val buf = ByteArray(8192)
                var read: Int
                do {
                    read = stream.read(buf)
                } while (read != -1)
                stream.digest()
            }
        }

        private fun fetchArtifactInfo(version: String): List<ArtifactInfo> {
            val ret = mutableListOf<ArtifactInfo>()
            val pkg = fetchPackage(version)

            // Client JAR
            val downloads = pkg.get("downloads") ?: throw IllegalStateException("missing required key: downloads")
            if (!downloads.isJsonObject) throw IllegalStateException("\"downloads\" is not an object")
            val client = downloads.asJsonObject.get("client") ?: throw IllegalStateException("missing required key: client")
            if (!client.isJsonObject) throw IllegalStateException("\"client\" is not an object")
            val clientUrl = client.asJsonObject.get("url") ?: throw IllegalStateException("missing required key: url")
            if (!clientUrl.isJsonPrimitive || !clientUrl.asJsonPrimitive.isString) throw IllegalStateException("\"url\" is not a string")
            val clientSha1 = client.asJsonObject.get("sha1") ?: throw IllegalStateException("missing required key: sha1")
            if (!clientSha1.isJsonPrimitive || !clientSha1.asJsonPrimitive.isString) throw IllegalStateException("\"sha1\" is not a string")
            ret.add(ArtifactInfo(
                library = Library.CLIENT,
                name = "client.jar",
                url = clientUrl.asJsonPrimitive.asString,
                sha1 = clientSha1.asJsonPrimitive.asString
            ))

            // Libraries
            val libraries = pkg.get("libraries") ?: throw IllegalStateException("missing required key: libraries")
            if (!libraries.isJsonArray) throw IllegalStateException("\"libraries\" is not an array")
            for (element in libraries.asJsonArray) {
                val entry = element.asJsonObject
                if (entry.has("rules") && entry.get("rules").asJsonArray.size() != 0) continue

                val name = entry.get("name")
                    .asJsonPrimitive
                    .asString

                val artifact = entry
                    .get("downloads")
                    .asJsonObject
                    .get("artifact")
                    .asJsonObject

                val url = artifact.get("url").asJsonPrimitive.asString
                val sha1 = artifact.get("sha1").asJsonPrimitive.asString
                ret.add(ArtifactInfo(
                    library = Library.fromNotation(name),
                    name = url.substring(url.lastIndexOf('/') + 1),
                    url = url,
                    sha1 = sha1
                ))
            }

            return ret
        }

        private fun fetchPackage(version: String): JsonObject {
            val manifest = fetchJsonObject("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
            val versions = manifest.get("versions") ?: throw IllegalStateException("missing required key: versions")
            if (!versions.isJsonArray) throw IllegalStateException("\"versions\" is not an array")
            val version = versions.asJsonArray.find {
                if (!it.isJsonObject) {
                    false
                } else {
                    val id = it.asJsonObject.get("id")
                    id.isJsonPrimitive &&
                            id.asJsonPrimitive.isString &&
                            version == id.asJsonPrimitive.asString
                }
            } ?: throw IllegalStateException("version \"${version}\" not found in manifest")
            val url = version.asJsonObject.get("url") ?: throw IllegalStateException("missing required key: url")
            if (!url.isJsonPrimitive || !url.asJsonPrimitive.isString) throw IllegalStateException("\"url\" is not a string")
            return fetchJsonObject(url.asJsonPrimitive.asString)
        }

        private fun fetchJsonObject(destination: String): JsonObject {
            val connection = httpConnection(destination)
            connection.setRequestProperty("Accept", "application/json")
            return connection.getInputStream().use { stream ->
                val reader = InputStreamReader(stream, StandardCharsets.UTF_8)
                GSON.fromJson(reader, JsonObject::class.java)
            }
        }

        private fun httpConnection(destination: String): HttpURLConnection {
            val url = URI.create(destination).toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "dryeye; xpedraza542@gmail.com")
            return connection
        }

    }

}