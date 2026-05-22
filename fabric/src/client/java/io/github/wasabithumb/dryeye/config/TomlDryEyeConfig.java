package io.github.wasabithumb.dryeye.config;

import io.github.wasabithumb.dryeye.util.MillisRange;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.comment.Comment;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@NullMarked
public final class TomlDryEyeConfig implements DryEyeConfig {

    private static final JToml TOML = JToml.jToml();

    //

    private Data data;

    public TomlDryEyeConfig() {
        this.data = new Data();
    }

    //

    @Override
    public boolean enabled() {
        return this.data.enabled;
    }

    @Override
    public void enabled(boolean enabled) {
        this.data.enabled = enabled;
    }

    @Override
    public MillisRange blinkDelay() {
        return this.data.delay.toMillisRange();
    }

    @Override
    public void blinkDelay(MillisRange blinkDelay) {
        this.data.delay = Data.MinMax.fromMillisRange(blinkDelay);
    }

    @Override
    public MillisRange blinkDuration() {
        return this.data.duration.toMillisRange();
    }

    @Override
    public void blinkDuration(MillisRange blinkDuration) {
        this.data.duration = Data.MinMax.fromMillisRange(blinkDuration);
    }

    public void load(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            TomlTable doc = TOML.read(in);
            this.data = TOML.fromToml(Data.class, doc);
        }
    }

    public void save(Path file) throws IOException {
        try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            TomlTable table = TOML.toToml(Data.class, this.data);
            TOML.write(out, table);
        }
    }

    //

    @Comment.Pre("DryEye Configuration")
    private static final class Data implements TomlSerializable {

        boolean enabled = true;
        MinMax delay = new MinMax(1000, 4000);
        MinMax duration = new MinMax(50, 200);

        //

        Data() { }

        //

        private record MinMax(
                long min,
                long max
        ) {

            static MinMax fromMillisRange(MillisRange range) {
                return new MinMax(range.from(), range.to());
            }

            MillisRange toMillisRange() {
                return MillisRange.lenient(this.min, this.max);
            }

        }

    }

}
