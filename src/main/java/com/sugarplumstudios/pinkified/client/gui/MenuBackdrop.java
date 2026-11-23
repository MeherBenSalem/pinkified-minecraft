package com.sugarplumstudios.pinkified.client.gui;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * The animated pastel sky shared by every menu: a soft gradient, drifting clouds, out-of-focus
 * light, falling petals and twinkling sparkles. A single instance keeps animating across screen
 * changes so moving between menus feels continuous.
 */
public final class MenuBackdrop {
    public static final MenuBackdrop INSTANCE = new MenuBackdrop();

    private static final class Petal {
        float x, y, vx, vy, rot, spin, sway, phase, scale;
        int sprite, tint;
    }

    private static final class Sparkle {
        float x, y, period, phase, scale;
        boolean big;
    }

    private static final class Bokeh {
        float x, y, vx, vy, size, alpha;
        int tint;
    }

    private static final class Cloud {
        float x, y, speed, scale;
        int sprite;
    }

    private final RandomSource random = RandomSource.create();
    private final List<Petal> petals = new ArrayList<>();
    private final List<Sparkle> sparkles = new ArrayList<>();
    private final List<Bokeh> bokeh = new ArrayList<>();
    private final List<Cloud> clouds = new ArrayList<>();
    private int width, height;
    private long lastMillis;

    private MenuBackdrop() {
    }

    /** Full opaque backdrop for menus outside of a world. */
    public void renderMenu(GuiGraphics g, int w, int h) {
        Palette p = Themes.palette();
        update(w, h);
        g.fillGradient(0, 0, w, h, p.skyTop, p.skyBottom);
        drawBokeh(g, p, 1f);
        drawClouds(g, 0.85f);
        // a whisper of shade at the bottom keeps white button text readable
        g.fillGradient(0, h * 2 / 3, w, h, 0, Colors.withAlpha(p.ink, 0x38));
        drawPetals(g, p, 1f);
        drawSparkles(g, p, 1f);
    }

    /** Light translucent tint used behind in-game menus (pause, inventories). */
    public void renderInWorld(GuiGraphics g, int w, int h) {
        Palette p = Themes.palette();
        update(w, h);
        g.fillGradient(0, 0, w, h, Colors.withAlpha(p.skyTop, 0x55), Colors.withAlpha(p.skyBottom, 0x40));
        drawSparkles(g, p, 0.55f);
    }

    private void update(int w, int h) {
        long now = Anim.millis();
        float dt = lastMillis == 0 ? 0 : Math.min(0.1f, (now - lastMillis) / 1000f);
        lastMillis = now;
        if (w != width || h != height) {
            width = w;
            height = h;
            populate();
        }
        float speed = Anim.intensity();
        if (speed <= 0) return;
        float t = Anim.seconds();
        for (Petal pt : petals) {
            pt.y += pt.vy * dt * speed;
            pt.x += (pt.vx + (float) Math.sin(t * pt.sway + pt.phase) * 9f) * dt * speed;
            pt.rot += pt.spin * dt * speed;
            if (pt.y > height + 12 || pt.x > width + 20 || pt.x < -20) respawnPetal(pt, false);
        }
        for (Bokeh b : bokeh) {
            b.x += b.vx * dt * speed;
            b.y += b.vy * dt * speed;
            if (b.x < -b.size) b.x = width + b.size;
            if (b.x > width + b.size) b.x = -b.size;
            if (b.y < -b.size) b.y = height + b.size;
            if (b.y > height + b.size) b.y = -b.size;
        }
        for (Cloud c : clouds) {
            c.x += c.speed * dt * speed;
            if (c.x > width + 10) c.x = -48 * c.scale - random.nextInt(60);
        }
    }

    private void populate() {
        petals.clear();
        sparkles.clear();
        bokeh.clear();
        clouds.clear();
        int area = Math.max(1, width * height);
        int petalCount = Math.min(60, area / 9000 + 10);
        for (int i = 0; i < petalCount; i++) {
            Petal pt = new Petal();
            respawnPetal(pt, true);
            petals.add(pt);
        }
        int sparkleCount = Math.min(40, area / 14000 + 8);
        for (int i = 0; i < sparkleCount; i++) {
            Sparkle s = new Sparkle();
            s.x = random.nextFloat() * width;
            s.y = random.nextFloat() * height;
            s.period = 1.6f + random.nextFloat() * 2.4f;
            s.phase = random.nextFloat();
            s.big = random.nextFloat() < 0.3f;
            s.scale = 0.6f + random.nextFloat() * 0.5f;
            sparkles.add(s);
        }
        for (int i = 0; i < 9; i++) {
            Bokeh b = new Bokeh();
            b.x = random.nextFloat() * width;
            b.y = random.nextFloat() * height;
            b.vx = (random.nextFloat() - 0.5f) * 6f;
            b.vy = (random.nextFloat() - 0.5f) * 4f;
            b.size = 30 + random.nextFloat() * 70;
            b.alpha = 0.10f + random.nextFloat() * 0.14f;
            b.tint = i % 3;
            bokeh.add(b);
        }
        for (int i = 0; i < 5; i++) {
            Cloud c = new Cloud();
            c.scale = 1f + random.nextFloat() * 1.5f;
            c.x = random.nextFloat() * (width + 100) - 50;
            c.y = 8 + random.nextFloat() * height * 0.55f;
            c.speed = 3f + random.nextFloat() * 5f;
            c.sprite = i % 2;
            clouds.add(c);
        }
    }

    private void respawnPetal(Petal pt, boolean anywhere) {
        pt.x = random.nextFloat() * (width + 40) - 20;
        pt.y = anywhere ? random.nextFloat() * height : -10 - random.nextFloat() * 30;
        pt.vx = 4f + random.nextFloat() * 8f;
        pt.vy = 10f + random.nextFloat() * 16f;
        pt.rot = random.nextFloat() * 360f;
        pt.spin = (random.nextFloat() - 0.5f) * 120f;
        pt.sway = 0.6f + random.nextFloat() * 1.2f;
        pt.phase = random.nextFloat() * 6.28f;
        pt.scale = 0.8f + random.nextFloat() * 0.9f;
        pt.sprite = random.nextInt(PinkTextures.PETALS.length);
        pt.tint = random.nextInt(4);
    }

    private void drawBokeh(GuiGraphics g, Palette p, float alpha) {
        for (Bokeh b : bokeh) {
            int tint = b.tint == 0 ? p.light : (b.tint == 1 ? p.accentLight : p.petal(0));
            int s = Math.round(b.size);
            RenderKit.spriteTransformed(g, PinkTextures.BOKEH, b.x, b.y, 32, 32, 0, s / 32f, tint, b.alpha * alpha);
        }
    }

    private void drawClouds(GuiGraphics g, float alpha) {
        for (Cloud c : clouds) {
            RenderKit.spriteTransformed(g, PinkTextures.CLOUDS[c.sprite], c.x + 24 * c.scale, c.y, 48, 20, 0, c.scale, 0xFFFFFFFF, 0.55f * alpha);
        }
    }

    private void drawPetals(GuiGraphics g, Palette p, float alpha) {
        if (!PinkConfig.on(PinkConfig.MENU_PETALS)) return;
        for (Petal pt : petals) {
            int[] size = PinkTextures.PETAL_SIZES[pt.sprite];
            RenderKit.spriteTransformed(g, PinkTextures.PETALS[pt.sprite], pt.x, pt.y, size[0], size[1], pt.rot, pt.scale, p.petal(pt.tint), 0.9f * alpha);
        }
    }

    private void drawSparkles(GuiGraphics g, Palette p, float alpha) {
        if (!PinkConfig.on(PinkConfig.MENU_PETALS)) return;
        boolean animate = Anim.enabled();
        float t = Anim.seconds();
        for (Sparkle s : sparkles) {
            float tw = animate ? 0.5f + 0.5f * (float) Math.sin((t / s.period + s.phase) * Math.PI * 2) : 0.6f;
            tw = tw * tw;
            int size = s.big ? 9 : 5;
            RenderKit.spriteTransformed(g, s.big ? PinkTextures.SPARKLE_BIG : PinkTextures.SPARKLE_SMALL, s.x, s.y, size, size,
                    0, s.scale * (0.6f + 0.4f * tw), p.sparkle, tw * 0.9f * alpha);
        }
    }
}
