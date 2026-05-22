package io.github.wasabithumb.dryeye.app;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.wasabithumb.dryeye.app.components.EyeSchemeComboBox;
import io.github.wasabithumb.dryeye.app.components.FacePanel;
import io.github.wasabithumb.dryeye.app.context.ApplicationContext;
import io.github.wasabithumb.dryeye.face.Face;
import io.github.wasabithumb.dryeye.face.WritableFace;
import io.github.wasabithumb.dryeye.face.eye.EyeScheme;
import io.github.wasabithumb.dryeye.face.eye.EyeSchemes;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Very messy and unoptimized omni class.
 * I don't really care. :)
 */
@NullMarked
final class Window extends JFrame {

    private static final Gson GSON = new Gson();

    private static Components populateInner(ApplicationContext ctx, JPanel panel) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5d;
        c.weighty = 0.5d;
        c.insets = new Insets(0, 0, 0, 0);
        FacePanel face = new FacePanel(ctx);
        face.setMinimumSize(new Dimension(64, 64));
        face.setPreferredSize(new Dimension(256, 256));
        panel.add(face, c);

        JPanel settings = new JPanel();
        settings.setLayout(new GridBagLayout());
        c.insets = new Insets(4, 8, 4, 8);
        c.gridx = 1;
        panel.add(settings, c);

        JLabel labelEyeScheme = new JLabel(ApplicationString.EYE_SCHEME_LABEL);
        c.gridx = 0;
        c.gridy = 0;
        settings.add(labelEyeScheme, c);

        EyeSchemeComboBox combo = new EyeSchemeComboBox(ctx);
        c.gridx = 1;
        settings.add(combo, c);

        JButton autoDetect = new JButton(ApplicationString.EYE_SCHEME_AUTO_DETECT_LABEL);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        settings.add(autoDetect, c);
        autoDetect.addActionListener(_ -> {
            Face ref = ctx.getFace();

            ctx.log(Level.INFO, "== match info ==");
            for (EyeScheme scheme : EyeSchemes.all()) {
                double weight = scheme.weight(ref);
                ctx.log(Level.INFO, scheme.name() + " - " + (new DecimalFormat("0.##")).format(weight * 100) + "%");
            }
            ctx.log(Level.INFO, "================");

            EyeScheme scheme = EyeSchemes.match(ref);
            ctx.setEyeScheme(scheme);
            combo.setSelectedItem(scheme);
            combo.repaint();
        });

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        JLabel labelDelay = new JLabel(ApplicationString.BLINK_DELAY_LABEL);
        settings.add(labelDelay, c);

        c.gridx = 1;
        JSlider sliderDelay = new JSlider();
        sliderDelay.setMinimum(100);
        sliderDelay.setMajorTickSpacing(100);
        sliderDelay.setMaximum(5000);
        sliderDelay.setValue((int) ctx.getBlinkDelay());
        sliderDelay.addChangeListener(_ -> ctx.setBlinkDelay(sliderDelay.getValue()));
        settings.add(sliderDelay, c);

        c.gridx = 0;
        c.gridy = 3;
        JLabel labelDuration = new JLabel(ApplicationString.BLINK_DURATION_LABEL);
        settings.add(labelDuration, c);

        c.gridx = 1;
        JSlider sliderDuration = new JSlider();
        sliderDuration.setMinimum(1);
        sliderDuration.setMajorTickSpacing(10);
        sliderDuration.setMaximum(1000);
        sliderDuration.setValue((int) ctx.getBlinkDuration());
        sliderDuration.addChangeListener(_ -> ctx.setBlinkDuration(sliderDuration.getValue()));
        settings.add(sliderDuration, c);

        return new Components(face, combo);
    }

    private static Components populate(ApplicationContext ctx, JFrame frame) {
        frame.setSize(640, 480);
        frame.setMenuBar(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        frame.add(panel, BorderLayout.CENTER);
        Components components = populateInner(ctx, panel);

        JPanel bar = new JPanel();
        bar.setLayout(new BorderLayout());
        frame.add(bar, BorderLayout.PAGE_END);

        JTextField fieldUsername = new JTextField();
        bar.add(fieldUsername, BorderLayout.CENTER);

        JButton buttonSetUser = new JButton(ApplicationString.FETCH_SKIN_BUTTON);
        bar.add(buttonSetUser, BorderLayout.LINE_END);
        buttonSetUser.addActionListener(_ -> {
            String name = fieldUsername.getText();
            fieldUsername.setText("");
            if (name.isBlank()) {
                ctx.log(Level.WARNING, "Ignoring blank username");
                return;
            }
            ctx.log(Level.INFO, "Fetching player head: " + name);
            SkinFetcher fetcher = new SkinFetcher(name, ctx.getLogger(), face -> {
                if (face == null) return;
                ctx.log(Level.INFO, "Success");
                ctx.setFace(face);
                SwingUtilities.invokeLater(() -> {
                    EyeSchemeComboBox comboBox = components.comboBox;
                    comboBox.setSelectedItem(ctx.getEyeScheme());
                    comboBox.repaint();
                });
            });
            fetcher.start();
        });

        JScrollPane consolePane = new JScrollPane();
        JPanel consoleContent = new JPanel();
        consoleContent.setLayout(new BoxLayout(consoleContent, BoxLayout.PAGE_AXIS));
        consolePane.getViewport().add(consoleContent);
        consolePane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 64));
        bar.add(consolePane, BorderLayout.PAGE_END);
        ctx.getLogger().addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                String time = record.getInstant()
                        .atOffset(OffsetDateTime.now().getOffset())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS"));

                JLabel label = new JLabel();
                label.setText("[" + time + "] [" + record.getLevel().getName() + "] " + record.getMessage());
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
                        label.setForeground(Color.RED);
                    } else {
                        label.setForeground(Color.YELLOW);
                    }
                }
                consoleContent.add(label);
                consolePane.validate();
                consolePane.getViewport().scrollRectToVisible(label.getBounds());
                consolePane.repaint();
            }
            @Override
            public void flush() { }
            @Override
            public void close() { }
        });

        return components;
    }

    //

    private final Components components;

    public Window(ApplicationContext ctx) {
        super(ApplicationString.WINDOW_TITLE);
        this.components = populate(ctx, this);
    }

    public void onTick() {
        this.components.facePanel.repaint(50L);
    }

    //

    private record Components(
            FacePanel facePanel,
            EyeSchemeComboBox comboBox
    ) { }

    private static final class SkinFetcher extends Thread {

        private final String username;
        private final Logger logger;
        private final Consumer<@Nullable Face> callback;

        SkinFetcher(
                String username,
                Logger logger,
                Consumer<@Nullable Face> callback
        ) {
            this.setDaemon(true);
            this.username = username;
            this.logger = logger;
            this.callback = callback;
        }

        //

        @Override
        public void run() {
            Face value = null;
            try {
                value = resolve(this.username, this.logger);
            } finally {
                this.callback.accept(value);
            }
        }

        private static @Nullable Face resolve(String username, Logger logger) {
            try {
                URL lookupUrl = URI.create("https://api.mojang.com/minecraft/profile/lookup/name/" + username).toURL();
                HttpURLConnection lookupConnection = (HttpURLConnection) lookupUrl.openConnection();
                int lookupStatus = lookupConnection.getResponseCode();
                if (lookupStatus != 200) {
                    logger.log(
                            (400 <= lookupStatus && lookupStatus <= 499) ? Level.WARNING : Level.SEVERE,
                            "HTTP " + lookupStatus + " (" + lookupConnection.getResponseMessage() + ")"
                    );
                    return null;
                }

                JsonObject result;
                try (InputStream in = lookupConnection.getInputStream();
                     InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)
                ) {
                    result = GSON.fromJson(reader, JsonObject.class);
                }

                String id = result.get("id")
                        .getAsString();

                URL profileUrl = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + id).toURL();
                HttpURLConnection profileConnection = (HttpURLConnection) profileUrl.openConnection();
                int profileStatus = profileConnection.getResponseCode();
                if (profileStatus != 200) {
                    logger.log(Level.SEVERE, "HTTP " + profileStatus + " (" + profileConnection.getResponseMessage() + ")");
                    return null;
                }

                try (InputStream in = profileConnection.getInputStream();
                     InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)
                ) {
                    result = GSON.fromJson(reader, JsonObject.class);
                }

                String textures = null;
                for (JsonElement element : result.get("properties").getAsJsonArray()) {
                    JsonObject qual = element.getAsJsonObject();
                    if (!"textures".equals(qual.get("name").getAsString())) continue;
                    textures = qual.get("value").getAsString();
                    break;
                }

                if (textures == null) {
                    logger.log(Level.SEVERE, "No texture found in profile");
                    return null;
                }

                String texturesDecoded = new String(
                        Base64.getDecoder().decode(textures.getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8
                );
                result = GSON.fromJson(texturesDecoded, JsonObject.class);


                String skinUrlString = result.get("textures")
                        .getAsJsonObject()
                        .get("SKIN")
                        .getAsJsonObject()
                        .get("url")
                        .getAsString();

                URL skinUrl = URI.create(skinUrlString).toURL();
                HttpURLConnection skinConnection = (HttpURLConnection) skinUrl.openConnection();

                try (InputStream in = skinConnection.getInputStream()) {
                    BufferedImage img = ImageIO.read(in);
                    img = img.getSubimage(8, 8, 8, 8);
                    return WritableFace.of(img);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to fetch skin", e);
                return null;
            }
        }

    }

}
