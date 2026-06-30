package io.github.wasabithumb.dryeye.manager;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.wasabithumb.dryeye.config.DryEyeConfig;
import io.github.wasabithumb.dryeye.config.TomlDryEyeConfig;
import io.github.wasabithumb.dryeye.face.Face;
import io.github.wasabithumb.dryeye.face.NativeImageFace;
import io.github.wasabithumb.dryeye.face.eye.EyeScheme;
import io.github.wasabithumb.dryeye.face.eye.EyeSchemes;
import io.github.wasabithumb.dryeye.util.LongSamplers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Random;

@NullMarked
abstract class AbstractDryEyeManager implements DryEyeManager {

    private final Minecraft mc;
    private final Logger logger;
    private final Path configPath;
    private final Int2ObjectMap<Entry> cache;
    private volatile DryEyeConfig config;

    protected AbstractDryEyeManager(Minecraft mc, Logger logger, Path configPath) {
        this.mc = mc;
        this.logger = logger;
        this.configPath = configPath;
        this.cache = new Int2ObjectOpenHashMap<>();
        this.config = DryEyeConfig.disabled();
    }

    //

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public DryEyeConfig config() {
        return this.config;
    }

    @Override
    public void loadConfig() {
        Path file = this.configPath;
        TomlDryEyeConfig newConfig = new TomlDryEyeConfig();
        try {
            if (Files.isRegularFile(file)) {
                newConfig.load(file);
            } else {
                newConfig.save(file);
            }
        } catch (IOException e) {
            this.logger.error("Failed to initialize configuration", e);
        }
        this.config = newConfig;
    }

    @Override
    public void saveConfig() {
        Path file = this.configPath;
        if (this.config instanceof TomlDryEyeConfig toml) {
            try {
                toml.save(file);
            } catch (IOException e) {
                this.logger.error("Failed to save configuration", e);
            }
        } else {
            this.logger.warn("Unable to save configuration before it has been initialized");
        }
    }

    @Override
    public BlinkState query(int id, Identifier skin) {
        final DryEyeConfig config = this.config;
        if (!config.enabled()) return InactiveBlinkState.INSTANCE;

        Entry entry = this.cache.get(id);
        //noinspection ConstantValue
        if (entry == null || this.checkMismatchAndDiscard(entry, skin)) {
            entry = new Entry(id, skin, this.modifySkin(id, skin));
            this.cache.put(id, entry);
        }

        entry.mark();
        if (entry.tick(config)) return new State(entry.modifiedSkin());
        return InactiveBlinkState.INSTANCE;
    }

    @Override
    public void cleanup() {
        ObjectIterator<Int2ObjectMap.Entry<Entry>> iterator = this.cache.int2ObjectEntrySet().iterator();
        Entry next;
        while (iterator.hasNext()) {
            next = iterator.next().getValue();
            if (next.pollMark()) continue;
            this.logger.debug("Clearing cache entry for entity ID {}", next.id());
            iterator.remove();
            this.mc.getTextureManager().release(next.modifiedSkin());
        }
    }

    private Identifier modifySkin(int id, Identifier skin) {
        try (NativeImage surface = loadNativeImage(skin)) {
            if (surface == null) {
                this.logger.warn("Unable to modify skin {}, will not blink", skin);
                return skin;
            }

            NativeImageFace face = new NativeImageFace(surface, 8, 8);
            EyeScheme.Mapper mapper = optimalMapper(face);
            if (mapper.weight() > 0.5) {
                mapper.blink(face);
            } else {
                this.logger.warn("Cannot confidently determine eyes for skin {}, blinking disabled", skin);
                return skin;
            }

            Identifier target = generateIdentifier(id, skin);
            DynamicTexture texture = new DynamicTexture(target.toString(), surface.getWidth(), surface.getHeight(), false);
            texture.setPixels(surface);
            texture.upload();
            this.mc.getTextureManager().register(target, texture);
            this.logger.debug("Generated blink variant {} for entity ID {} with skin {}", target, id, skin);
            return target;
        }
    }

    private @Nullable NativeImage loadNativeImage(Identifier identifier) {
        AbstractTexture texture = this.mc.getTextureManager().getTexture(identifier);
        if (texture instanceof SimpleTexture simple) {
            TextureContents contents;
            try {
                contents = simple.loadContents(this.mc.getResourceManager());
            } catch (IOException e) {
                this.logger.atError()
                        .setCause(e)
                        .setMessage("Failed to load texture " + identifier)
                        .log();
                return null;
            }
            return contents.image();
        } else if (texture instanceof DynamicTexture dynamic) {
            NativeImage src = dynamic.getPixels();
            NativeImage dest = new NativeImage(src.format(), src.getWidth(), src.getHeight(), false);
            dest.copyFrom(src);
            return dest;
        } else {
            this.logger.error("Texture {} is not loadable", identifier);
            return null;
        }
    }

    private boolean checkMismatchAndDiscard(Entry entry, Identifier skin) {
        if (skin.equals(entry.skin)) return false;
        this.mc.getTextureManager().release(entry.modifiedSkin);
        return true;
    }

    private static EyeScheme.Mapper optimalMapper(Face face) {
        Iterator<EyeScheme> iter = EyeSchemes.all().iterator();
        EyeScheme.Mapper ret = iter.next().newMapper(face);
        EyeScheme.Mapper next;
        while (iter.hasNext()) {
            next = iter.next().newMapper(face);
            if (next.weight() > ret.weight()) ret = next;
        }
        return ret;
    }

    private static Identifier generateIdentifier(int id, Identifier skin) {
        return Identifier.parse(
                "dryeye:skin_patch/" +
                        id + "/" +
                        Integer.toString(skin.hashCode(), 16)
        );
    }

    //

    private record State(
            Identifier modifiedSkin
    ) implements BlinkState {

        @Override
        public boolean active() {
            return true;
        }

    }

    private static final class Entry {

        private final int id;
        private final Identifier skin;
        private final Identifier modifiedSkin;
        private final Random random;
        private boolean marked;
        private long lastBlink;
        private long scheduledBlinkDelay;
        private long scheduledBlinkDuration;

        Entry(
                int id,
                Identifier skin,
                Identifier modifiedSkin
        ) {
            this.id = id;
            this.skin = skin;
            this.modifiedSkin = modifiedSkin;
            this.random = new Random();
            this.marked = false;
            this.lastBlink = -1L;
            this.scheduledBlinkDelay = -1L;
            this.scheduledBlinkDuration = -1L;
        }

        //

        public int id() {
            return this.id;
        }

        public Identifier skin() {
            return this.skin;
        }

        public Identifier modifiedSkin() {
            return this.modifiedSkin;
        }

        public void mark() {
            this.marked = true;
        }

        public boolean pollMark() {
            if (this.marked) {
                this.marked = false;
                return true;
            }
            return false;
        }

        /** Returns true if currently blinking */
        public boolean tick(DryEyeConfig config) {
            long now = System.currentTimeMillis();
            long lastBlink = this.lastBlink;
            long blinkDelay = this.scheduledBlinkDelay;
            long blinkDuration = this.scheduledBlinkDuration;
            long elapsed;

            if (
                    lastBlink == -1L ||
                    (elapsed = now - lastBlink) >= (blinkDelay + blinkDuration)
            ) {
                this.lastBlink = now;
                this.scheduledBlinkDelay = config.blinkDelay()
                        .sample(LongSamplers.inverseQuadratic(), this.random);
                this.scheduledBlinkDuration = config.blinkDuration()
                        .sample(LongSamplers.quadratic(), this.random);
                return false;
            }

            return elapsed >= blinkDelay;
        }

    }

}
