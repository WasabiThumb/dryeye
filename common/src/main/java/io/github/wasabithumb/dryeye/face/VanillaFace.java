package io.github.wasabithumb.dryeye.face;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * Registry for vanilla faces
 * as reported by <a href="https://minecraft.wiki/w/Skin#Default_skins">the wiki</a>.
 */
@NullMarked
public final class VanillaFace {

    public static final Face ALEX;
    public static final Face ARI;
    public static final Face EFE;
    public static final Face KAI;
    public static final Face MAKENA;
    public static final Face NOOR;
    public static final Face STEVE;
    public static final Face SUNNY;
    public static final Face ZURI;

    private static final Face[] VALUES;
    static {
        VALUES = new Face[] {
                ALEX   = Face.deserialize("TUP6zojljT/zqFjrmD/fxKLr0LDv2r////8jYiTvu7EBIQIQIQIAISAgMwIiBFRARnVXZFVVVVVVWIVURVVVQw=="),
                ARI    = Face.deserialize("TUP6zomAMBKdRSKTPx7lf2GHNxjxk275p4b///8kAwPxgW4BIjQkQjVTICVmZlImZmZiR4ZodJlmZplmYzZmVmZmZQ=="),
                EFE    = Face.deserialize("TUP6zo2GXI6KYpSVa554T36BWImTaZxmP2mkaECrckz///9pKzSmaEl3QyCcc6kBEhExBFQRYRR4E3EHiIiEGaiKkSiIiIIrjMi9GIiIgQ=="),
                KAI    = Face.deserialize("TUP6zoj77ZH74oroum3OiE7fllj89Kr///9DZ1N3QyAAEAAAAQACIAAANEUDRERFVnRHZVREREAESIRAAEREAA=="),
                MAKENA = Face.deserialize("TUP6zoomEhYfCg4yGR4eDhFENSg4KBvTy8YuGABaRTQwIhgqHg8BIjAgICFEIgJQFFBURERFRnRHZIREREhUSZRFpUREWg=="),
                NOOR   = Face.deserialize("TUP6zoouGA44HhFZNCCnXEO5Z0rNeVupVjr///9YLB1zOyeRRzEBAgEBE0NDQTRURENGZEZkZ4RIdkREREQ0SZRDo0REOg=="),
                STEVE  = Face.deserialize("TUP6zhIzJBE/KhUrHg0kGAibY0mzeV63g2uqclk0JRL///9SPYlqQDCQWT+PXj5JJRB3QjVCHQqBUzmUYD4AAAEBAQEAAgMAAAEBAAEAAgQFBgUHBAgEBwUFBwcHBAcJCgcECgkHBAcHCwsHBwQMDQ4PDxANERIREA4QDhEN"),
                SUNNY  = Face.deserialize("TUP6zokvLy84ODhJSUkiIiLYhEv///8AAADyn193QyDBcj8BASAQASEgEAAgEQAwEwADRWRGVHd3d3dHeId0lHd3SQ=="),
                ZURI   = Face.deserialize("TUP6zokOBAEbEQ0iFhFfPiltSDB+Uzf///9BHgKSZ0x1PCgAEiEAA0VUMEVVVVRVVVVVVnVXZIVVVVhVWZVUNFVVQw==")
        };
    }

    @Contract("-> new")
    public static Face[] values() {
        final int len = VALUES.length;
        Face[] copy = new Face[len];
        System.arraycopy(VALUES, 0, copy, 0, len);
        return copy;
    }

    public static Face of(UUID id) {
        return VALUES[Integer.remainderUnsigned(id.hashCode(), VALUES.length)];
    }

    //

    private VanillaFace() { }

}
