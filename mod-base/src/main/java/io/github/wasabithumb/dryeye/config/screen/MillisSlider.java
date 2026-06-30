package io.github.wasabithumb.dryeye.config.screen;

import io.github.wasabithumb.dryeye.util.MillisRange;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MillisSlider extends AbstractSliderButton {

    @Contract("_, _ -> new")
    public static Builder builder(Component label, MillisRange range) {
        return new Builder(label, range);
    }

    private static Component computeMessage(Component label, long value) {
        return Component.empty()
                .append(label)
                .append(Component.literal(": " + value));
    }

    private static final double CURVE_EXPONENT = 4d;
    private static final double CURVE_RECIPROCAL = 1d / CURVE_EXPONENT;

    //

    private final Component label;
    private final MillisRange range;

    public MillisSlider(
            int x, int y,
            int width, int height,
            Component label,
            MillisRange range,
            long initialValue
    ) {
        super(x, y, width, height, computeMessage(label, initialValue), Math.pow(range.fraction(initialValue), CURVE_RECIPROCAL));
        this.label = label;
        this.range = range;
    }

    //

    public long value() {
        return this.range.atFraction(Math.pow(this.value, CURVE_EXPONENT));
    }

    @Override
    protected void updateMessage() {
        this.setMessage(computeMessage(this.label, this.value()));
    }

    @Override
    protected void applyValue() { }

    //

    public static final class Builder {

        private final Component label;
        private final MillisRange range;
        private long initialValue;
        private int x;
        private int y;
        private int width ;
        private int height;

        Builder(
                Component label,
                MillisRange range
        ) {
            this.label = label;
            this.range = range;
            this.initialValue = range.from();
            this.x = 0;
            this.y = 0;
            this.width = 150;
            this.height = 20;
        }

        //

        @Contract("_ -> this")
        public Builder initialValue(long initialValue) {
            this.initialValue = initialValue;
            return this;
        }

        @Contract("_, _ -> this")
        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        @Contract("_, _ -> this")
        public Builder size(int w, int h) {
            this.width = w;
            this.height = h;
            return this;
        }

        @Contract("-> new")
        public MillisSlider build() {
            return new MillisSlider(
                    this.x, this.y,
                    this.width, this.height,
                    this.label,
                    this.range,
                    this.initialValue
            );
        }

    }

}
