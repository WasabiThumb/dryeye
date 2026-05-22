package io.github.wasabithumb.dryeye.config.screen;

import io.github.wasabithumb.dryeye.config.DryEyeConfig;
import io.github.wasabithumb.dryeye.i18n.LangComponents;
import io.github.wasabithumb.dryeye.manager.DryEyeManager;
import io.github.wasabithumb.dryeye.util.MillisRange;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@NullMarked
public final class DryEyeConfigScreen extends Screen {

    private static final int PADDING = 8;
    private static final MillisRange DELAY_RANGE = MillisRange.of(50L, 100000L);
    private static final MillisRange DURATION_RANGE = MillisRange.of(10L, 1000L);

    //

    private final DryEyeManager manager;
    private final @Nullable Screen parent;
    private @UnknownNullability OptionsWidgets options;

    public DryEyeConfigScreen(@Nullable Screen parent) {
        super(LangComponents.of("text.dryeye.config"));
        this.manager = DryEyeManager.getInstance();
        this.parent = parent;
    }

    //

    @Override
    protected void init() {
        // Header
        StringWidget title = new StringWidget(LangComponents.of("text.dryeye.config"), this.font);
        title.setPosition(
                (this.width - title.getWidth()) / 2,
                PADDING
        );

        // Cancel & Save
        Button btnCancel = Button.builder(LangComponents.of("text.dryeye.config.cancel"), this::onPressCancel).build();
        Button btnSave = Button.builder(LangComponents.of("text.dryeye.config.save"), this::onPressSave).build();
        int btnWidth = (this.width - 4 * PADDING) / 2;
        int btnHeight = Math.max(btnCancel.getHeight(), btnSave.getHeight());
        btnCancel.setSize(btnWidth, btnHeight);
        btnCancel.setPosition(PADDING, this.height - PADDING - btnHeight);
        btnSave.setSize(btnWidth, btnHeight);
        btnSave.setPosition(this.width - PADDING - btnWidth, this.height - PADDING - btnHeight);

        // Options
        OptionsWidgets opts = initOptionsWidgets(this.manager.config(), this.font);
        int optsTop = 2 * PADDING + title.getHeight();
        int optsHeight = this.height - optsTop - (2 * PADDING) - btnHeight;
        arrangeOptionsWidgets(
                opts,
                PADDING, optsTop,
                this.width - (2 * PADDING),
                optsHeight
        );

        // Bind
        this.addRenderableWidget(title);
        this.addRenderableWidget(btnCancel);
        this.addRenderableWidget(btnSave);
        for (AbstractWidget widget : opts.all()) this.addRenderableWidget(widget);
        this.options = opts;
    }

    private void onPressCancel(Button ignored) {
        this.doClose(false);
    }

    private void onPressSave(Button ignored) {
        this.doClose(true);
    }

    @Override
    public void onClose() {
        this.doClose(true);
    }

    private void doClose(boolean save) {
        this.minecraft.setScreen(this.parent);
        if (!save) return;

        // Save
        OptionsWidgets options = this.options;
        DryEyeConfig config = this.manager.config();
        config.enabled(options.get(OptionsWidgets.Anchor.CHECKBOX_ENABLE).selected());
        config.blinkDelay(MillisRange.lenient(
                options.get(OptionsWidgets.Anchor.SLIDER_MIN_DELAY).value(),
                options.get(OptionsWidgets.Anchor.SLIDER_MAX_DELAY).value()
        ));
        config.blinkDuration(MillisRange.lenient(
                options.get(OptionsWidgets.Anchor.SLIDER_MIN_DURATION).value(),
                options.get(OptionsWidgets.Anchor.SLIDER_MAX_DURATION).value()
        ));
        this.manager.saveConfig();
    }

    private static OptionsWidgets initOptionsWidgets(DryEyeConfig config, Font font) {
        OptionsWidgets ret = new OptionsWidgets();
        ret.put(
                OptionsWidgets.Anchor.CHECKBOX_ENABLE,
                Checkbox.builder(LangComponents.of("text.dryeye.config.enable"), font)
                        .selected(config.enabled())
                        .build()
        );
        ret.put(new StringWidget(LangComponents.of("text.dryeye.config.delay"), font));
        ret.put(
                OptionsWidgets.Anchor.SLIDER_MIN_DELAY,
                MillisSlider.builder(LangComponents.of("text.dryeye.config.minimum"), DELAY_RANGE)
                        .initialValue(config.blinkDelay().from())
                        .build()
        );
        ret.put(
                OptionsWidgets.Anchor.SLIDER_MAX_DELAY,
                MillisSlider.builder(LangComponents.of("text.dryeye.config.maximum"), DELAY_RANGE)
                        .initialValue(config.blinkDelay().to())
                        .build()
        );
        ret.put(new StringWidget(LangComponents.of("text.dryeye.config.duration"), font));
        ret.put(
                OptionsWidgets.Anchor.SLIDER_MIN_DURATION,
                MillisSlider.builder(LangComponents.of("text.dryeye.config.minimum"), DURATION_RANGE)
                        .initialValue(config.blinkDuration().from())
                        .build()
        );
        ret.put(
                OptionsWidgets.Anchor.SLIDER_MAX_DURATION,
                MillisSlider.builder(LangComponents.of("text.dryeye.config.maximum"), DURATION_RANGE)
                        .initialValue(config.blinkDuration().to())
                        .build()
        );
        return ret;
    }

    @SuppressWarnings("SameParameterValue")
    private static void arrangeOptionsWidgets(
            OptionsWidgets widgets,
            int left, int top,
            int width, int height
    ) {
        int innerWidth = 0;
        int innerHeight = 0;

        for (AbstractWidget widget : widgets.all()) {
            if (innerHeight != 0) innerHeight += PADDING;
            innerWidth = Math.max(innerHeight, widget.getWidth());
            innerHeight += widget.getHeight();
        }

        int x = left + (width - innerWidth) / 2;
        int y = top + (height - innerHeight) / 2;

        for (AbstractWidget widget : widgets.all()) {
            widget.setPosition(x, y);
            widget.setWidth(innerWidth);
            y += widget.getHeight() + PADDING;
        }
    }

    //

    private static final class OptionsWidgets {

        private static final int MAX = 8;

        //

        private final AbstractWidget[] widgets;
        private final int[] anchors;
        private int count;

        public OptionsWidgets() {
            this.widgets = new AbstractWidget[MAX];
            this.anchors = new int[MAX];
            this.count = 0;
            Arrays.fill(this.anchors, -1);
        }

        //

        public @Unmodifiable List<AbstractWidget> all() {
            return Collections.unmodifiableList(Arrays.asList(this.widgets).subList(0, this.count));
        }

        public <T extends AbstractWidget> T get(Anchor<T> anchor) {
            int index = this.anchors[anchor.ordinal];
            if (index == -1) throw new IllegalStateException("Anchor " + anchor.ordinal + " not set");
            return anchor.typeClass.cast(this.widgets[index]);
        }

        public <T extends AbstractWidget> void put(@Nullable Anchor<T> anchor, T widget) {
            if (this.count >= MAX) throw new IllegalStateException("Too many widgets");
            int index = this.count++;
            this.widgets[index] = widget;
            if (anchor != null) this.anchors[anchor.ordinal] = index;
        }

        public void put(AbstractWidget widget) {
            this.put(null, widget);
        }

        //

        public record Anchor<T>(
                @Range(from = 0, to = MAX) int ordinal,
                Class<T> typeClass
        ) {

            public static final Anchor<Checkbox> CHECKBOX_ENABLE = new Anchor<>(0, Checkbox.class);
            public static final Anchor<MillisSlider> SLIDER_MIN_DELAY = new Anchor<>(1, MillisSlider.class);
            public static final Anchor<MillisSlider> SLIDER_MAX_DELAY = new Anchor<>(2, MillisSlider.class);
            public static final Anchor<MillisSlider> SLIDER_MIN_DURATION = new Anchor<>(3, MillisSlider.class);
            public static final Anchor<MillisSlider> SLIDER_MAX_DURATION = new Anchor<>(4, MillisSlider.class);

        }

    }

}
