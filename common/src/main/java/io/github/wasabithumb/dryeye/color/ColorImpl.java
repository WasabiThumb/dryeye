package io.github.wasabithumb.dryeye.color;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.ref.SoftReference;

@NullMarked
@ApiStatus.Internal
final class ColorImpl implements Color {

    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public static Color fromValue(int value) {
        return CACHE.get().get(value & 0xFFFFFF);
    }

    public static Color fromComponents(int r, int g, int b) {
        return CACHE.get()
                .get(
                        ((r & 0xFF) << 16) |
                        ((g & 0xFF) << 8) |
                        (b & 0xFF)
                );
    }

    //

    private final int value;
    private java.awt.@Nullable Color awt;

    private ColorImpl(int value) {
        this.value = value;
    }

    //

    @Override
    public @Range(from = 0x000000, to = 0xFFFFFF) int value() {
        return this.value;
    }

    @Override
    public @Range(from = 0x00, to = 0xFF) int red() {
        return (this.value >> 16) & 0xFF;
    }

    @Override
    public @Range(from = 0x00, to = 0xFF) int green() {
        return (this.value >> 8) & 0xFF;
    }

    @Override
    public @Range(from = 0x00, to = 0xFF) int blue() {
        return this.value & 0xFF;
    }

    @Override
    public void dump(byte[] out, int offset) {
        out[offset] = (byte) (this.value >> 16);
        out[offset + 1] = (byte) (this.value >> 8);
        out[offset + 2] = (byte) this.value;
    }

    @Override
    public synchronized java.awt.Color toAWT() {
        java.awt.Color ret = this.awt;
        if (ret == null) this.awt = ret = new java.awt.Color(this.value, false);
        return ret;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Color other &&
                this.value == other.value();
    }

    @Override
    public String toString() {
        return "Color{red=" + this.red() +
                ", green=" + this.green() +
                ", blue=" + this.blue() +
                "}";
    }

    //

    private static final class Cache {

        private static final int INITIAL_CAPACITY = 16;

        //

        private @Nullable Node[] nodes;
        private int capacity;
        private int size;

        Cache() {
            this.nodes = new Node[INITIAL_CAPACITY];
            this.capacity = INITIAL_CAPACITY;
            this.size = 0;
        }

        //

        public ColorImpl get(int value) {
            final int hash = Integer.remainderUnsigned(value, this.capacity);
            final Node existing = this.nodes[hash];
            Node head = this.nodes[hash];
            while (head != null) {
                ColorImpl color = head.value.get();
                if (color != null && color.value == value) {
                    // cache hit
                    return color;
                }
                head = head.next;
            }

            // cache miss
            ColorImpl ret = new ColorImpl(value);
            this.nodes[hash] = new Node(ret, existing);
            int newSize = ++this.size;
            int threshold = (this.capacity * 3) / 4;
            if (newSize >= threshold) this.rekey();
            return ret;
        }

        private void rekey() {
            // Initially try to double the buffer capacity,
            // however if collections are detected we might
            // retain or shrink the capacity

            int nc = this.capacity << 1;
            Node[] buf = new Node[nc];
            int size = rekeyFixed(this.nodes, this.capacity, buf, nc);

            if (size == this.size) {
                // Grow
                this.nodes = buf;
                this.capacity = nc;
                return;
            }

            int rc = this.capacity;
            while (true) {
                int shrink = rc >> 1;
                int threshold = (shrink * 3) / 4;
                if (size >= threshold) break;
                rc = shrink;
            }

            // Shrink/Retain
            Node[] buf2 = new Node[rc];
            size = rekeyFixed(buf, nc, buf2, rc);
            this.size = size;
            this.nodes = buf2;
            this.capacity = rc;
        }

        @Contract(mutates = "param3")
        private static int rekeyFixed(
                @Flow(target = "buf") @Nullable Node[] src, int srcCapacity,
                @Nullable Node[] buf, int bufCapacity
        ) {
            int nelem = 0;
            for (int i = 0; i < srcCapacity; i++) {
                Node node = src[i];
                while (node != null) {
                    final Node next = node.next;
                    ColorImpl color = node.value.get();
                    if (color != null) {
                        int hash = Integer.remainderUnsigned(color.value, bufCapacity);
                        node.next = buf[hash];
                        buf[hash] = node;
                        nelem++;
                    }
                    node = next;
                }
            }
            return nelem;
        }

        //

        private static final class Node {

            private final SoftReference<ColorImpl> value;
            private @Nullable Node next;

            Node(
                    ColorImpl value,
                    @Nullable Node next
            ) {
                this.value = new SoftReference<>(value);
                this.next = next;
            }

        }

    }

}
