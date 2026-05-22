package io.github.wasabithumb.dryeye.face;

import io.github.wasabithumb.dryeye.color.Color;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.io.*;
import java.util.Base64;

@NullMarked
@ApiStatus.Internal
final class FaceIO {

    static Face readBase64(String data) throws IllegalArgumentException {
        byte[] bytes = Base64.getDecoder().decode(data);
        return readBytes(bytes);
    }

    static String writeBase64(Face face) {
        return Base64.getEncoder().encodeToString(writeBytes(face));
    }

    static Face readBytes(byte[] data) throws IllegalArgumentException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            return read(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to parse face data", e);
        }
    }

    static byte[] writeBytes(Face face) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(262)) {
            write(out, face);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Face read(InputStream in) throws IOException {
        WritableFace ret = WritableFace.create();
        int r;

        // Read header and color table
        readHeader(in);
        r = readSingle(in);
        final boolean colorPacking = (r & 0x80) != 0;
        final int colorCount = (r & 0x3F) + 1;
        final Color[] colors = new Color[colorCount];
        for (int i = 0; i < colorCount; i++) {
            int cr = readSingle(in);
            int cg = readSingle(in);
            int cb = readSingle(in);
            colors[i] = Color.of(cr, cg, cb);
        }

        // Read data
        if (colorPacking) {
            int[] indices = new int[2];
            for (int i = 0; i < 32; i++) {
                r = readSingle(in);
                indices[0] = r >>> 4;
                indices[1] = r & 0xF;
                for (int z = 0; z < 2; z++) {
                    int ci = indices[z];
                    if (ci >= colorCount) throw new IOException("unmapped color id " + ci);
                    int di = (i << 1) | z;
                    ret.set(di & 7, di >>> 3, colors[ci]);
                }
            }
        } else {
            for (int i = 0; i < 64; i++) {
                r = readSingle(in);
                if (r >= colorCount) throw new IOException("unmapped color id " + r);
                ret.set(i & 7, i >>> 3, colors[r]);
            }
        }

        return Face.unmodifiableView(ret);
    }

    static void write(OutputStream out, Face face) throws IOException {
        // Write header
        writeHeader(out);

        // Calculate color table and remap data
        Color[] colors = new Color[64];
        int[] indices = new int[64];
        int colorCount = 0;
        for (int i = 0; i < 64; i++) {
            Color color = face.get(i & 7, i >>> 3);
            int colorIndex = -1;
            // O(n) lookup, don't really care to optimize writing
            for (int z = 0; z < colorCount; z++) {
                if (colors[z].equals(color)) {
                    colorIndex = z;
                    break;
                }
            }
            if (colorIndex == -1) {
                colorIndex = colorCount++;
                colors[colorIndex] = color;
            }
            indices[i] = colorIndex;
        }

        // Write color table
        // IMPL NOTE: 2 unused bits here
        boolean colorPacking = colorCount <= 16;
        int f = (colorCount - 1) & 0x3F;
        if (colorPacking) f |= 0x80;
        out.write(f);
        for (int i = 0; i < colorCount; i++) {
            Color color = colors[i];
            out.write(color.red());
            out.write(color.green());
            out.write(color.blue());
        }

        // Write data
        if (colorPacking) {
            for (int i = 0; i < 32; i++) {
                int hi = indices[i << 1];
                int lo = indices[(i << 1) | 1];
                out.write((hi << 4) | lo);
            }
        } else {
            for (int i = 0; i < 64; i++) {
                out.write(indices[i]);
            }
        }
    }

    private static void readHeader(InputStream in) throws IOException {
        if (readSingle(in) != 'M' ||
                readSingle(in) != 'C' ||
                readSingle(in) != 0xFA ||
                readSingle(in) != 0xCE
        )  throw new IOException("malformed face header");
    }

    private static void writeHeader(OutputStream out) throws IOException {
        out.write((int) 'M');
        out.write((int) 'C');
        out.write(0xFA);
        out.write(0xCE);
    }

    private static int readSingle(InputStream in) throws IOException {
        int r = in.read();
        if (r == -1) throw new EOFException("truncated face data");
        return r;
    }

}
