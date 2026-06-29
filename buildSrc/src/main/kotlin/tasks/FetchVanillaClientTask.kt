package tasks

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
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

    @get:OutputFile
    abstract val output: RegularFileProperty

    init {
        this.output.convention(this.project.layout.buildDirectory.file("tmp/${this.name}/client.jar"))
    }

    //

    @TaskAction
    fun fetch() {
        val version = this.version.get()
        val dest = this.output.get().asFile.toPath()
        val destDir = dest.parent
        if (!Files.exists(destDir)) Files.createDirectories(destDir)

        this.logger.lifecycle("fetching client JAR: $version")
        val info = fetchClientInfo(version)
        val sha1 = info.sha1.hexToByteArray()

        if (Files.exists(dest)) {
            // check hash
            val fileHash = sha1File(dest)
            if (sha1.contentEquals(fileHash)) {
                this.logger.lifecycle("hash matched, skipping")
                return
            }
        }

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


    //

    private data class ClientInfo(
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

        private fun fetchClientInfo(version: String): ClientInfo {
            val pkg = fetchPackage(version)
            val downloads = pkg.get("downloads") ?: throw IllegalStateException("missing required key: downloads")
            if (!downloads.isJsonObject) throw IllegalStateException("\"downloads\" is not an object")
            val client = downloads.asJsonObject.get("client") ?: throw IllegalStateException("missing required key: client")
            if (!client.isJsonObject) throw IllegalStateException("\"client\" is not an object")
            val url = client.asJsonObject.get("url") ?: throw IllegalStateException("missing required key: url")
            if (!url.isJsonPrimitive || !url.asJsonPrimitive.isString) throw IllegalStateException("\"url\" is not a string")
            val sha1 = client.asJsonObject.get("sha1") ?: throw IllegalStateException("missing required key: sha1")
            if (!sha1.isJsonPrimitive || !sha1.asJsonPrimitive.isString) throw IllegalStateException("\"sha1\" is not a string")
            return ClientInfo(
                url = url.asJsonPrimitive.asString,
                sha1 = sha1.asJsonPrimitive.asString
            )
        }

        private fun fetchPackage(version: String): JsonObject {
            val manifest = fetchJsonObject("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
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