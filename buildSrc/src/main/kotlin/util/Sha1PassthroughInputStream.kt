package util

import java.io.InputStream
import java.security.MessageDigest

class Sha1PassthroughInputStream(
    private val backing: InputStream
) : InputStream() {

    @Volatile
    private var used: Boolean = false
    private val digest: MessageDigest = MessageDigest.getInstance("SHA1")

    //

    fun digest(): ByteArray {
        this.checkUnused()
        val ret = this.digest.digest()
        this.used = true
        return ret
    }

    override fun read(): Int {
        this.checkUnused()
        val r = this.backing.read()
        if (r == -1) return -1
        this.digest.update(r.toByte())
        return r
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        this.checkUnused()
        val read = this.backing.read(b, off, len)
        if (read == -1) return -1
        this.digest.update(b, off, read)
        return read
    }

    override fun close() {
        this.backing.close()
    }

    private fun checkUnused() {
        if (this.used) throw IllegalStateException("Cannot read more bytes after digest has been consumed")
    }

}