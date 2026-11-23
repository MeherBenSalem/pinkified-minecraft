package com.sugarplumstudios.pinkified.client.texture;

import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.util.Colors;

/** Procedural tiling backgrounds that replace the vanilla dirt texture. */
public final class Patterns {
    private Patterns() {
    }

    private static final String[] TINY_HEART = {
            ".#.#.",
            "#####",
            ".###.",
            "..#..",
    };

    /**
     * A soft gingham tile with a tiny heart. Vanilla draws this texture darkened to 25% (and
     * 12.5% inside lists), so the base colours are kept bright.
     */
    public static Canvas background(int width, int height, Palette p, boolean light) {
        int size = Math.max(16, Math.min(width, height));
        int k = size / 16;
        Canvas c = new Canvas(size, size);
        c.scale = k;
        int base = light ? p.light : p.base;
        int stripe = light ? p.base : p.slot;
        int cross = Colors.lerp(stripe, p.shadow, light ? 0.25f : 0.35f);
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++) {
                boolean vx = (x / 4) % 2 == 0, vy = (y / 4) % 2 == 0;
                int col = vx && vy ? cross : (vx || vy ? Colors.lerp(base, stripe, 0.55f) : base);
                c.set(x, y, col);
            }
        int heart = light ? p.accentLight : p.light;
        for (int y = 0; y < TINY_HEART.length; y++)
            for (int x = 0; x < TINY_HEART[y].length(); x++)
                if (TINY_HEART[y].charAt(x) == '#') c.set(10 + x, 10 + y, heart);
        return c;
    }
}
