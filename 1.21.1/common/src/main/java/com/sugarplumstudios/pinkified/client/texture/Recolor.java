package com.sugarplumstudios.pinkified.client.texture;

import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.util.Colors;

import java.util.HashMap;
import java.util.Map;

/**
 * Pixel recolouring used to turn vanilla (or resource-pack) GUI art into themed art at
 * runtime. Nothing here is Minecraft specific.
 */
public final class Recolor {
    private Recolor() {
    }

    /**
     * Maps a whole canvas through the theme's ramps.
     *
     * <ul>
     *   <li>neutral greys (and very dark pixels) follow the neutral ramp by luminance;</li>
     *   <li>blue-ish greys follow the accent ramp when {@code accentForBlue} is set (vanilla uses
     *       blue for focus/hover states);</li>
     *   <li>any other colour is softened toward pastel so icons, flames and arrows stay readable.</li>
     * </ul>
     */
    public static void ramp(Canvas c, Palette p, boolean accentForBlue) {
        ramp(c, p, accentForBlue, 0, 0, c.width, c.height, false);
    }

    /** Recolours one raw-pixel rectangle; {@code useAccent} forces the accent ramp for greys. */
    public static void ramp(Canvas c, Palette p, boolean accentForBlue, int x0, int y0, int w, int h, boolean useAccent) {
        int[] neutral = useAccent ? p.accentRamp() : p.neutralRamp();
        int[] accent = p.accentRamp();
        Map<Integer, Integer> memo = new HashMap<>();
        for (int y = y0; y < Math.min(c.height, y0 + h); y++)
            for (int x = x0; x < Math.min(c.width, x0 + w); x++) {
                int i = y * c.width + x;
                int src = c.px[i];
                if ((src >>> 24) == 0) continue;
                Integer cached = memo.get(src);
                if (cached == null) {
                    cached = mapPixel(src, neutral, accent, accentForBlue);
                    memo.put(src, cached);
                }
                c.px[i] = cached;
            }
    }

    static int mapPixel(int src, int[] neutral, int[] accent, boolean accentForBlue) {
        int a = src >>> 24, r = Colors.red(src), g = Colors.green(src), b = Colors.blue(src);
        int max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        int chroma = max - min;
        float lum = Colors.luma(src);
        float[] hsl = Colors.toHsl(src);
        // greys, near-blacks and dusty beiges (anvil/enchanting fields) all count as "interface"
        if (chroma <= 24 || max < 72 || hsl[1] < 0.22f) {
            return Colors.withAlpha(sample(neutral, lum), a);
        }
        if (accentForBlue && hsl[0] >= 185 && hsl[0] <= 265 && hsl[1] < 0.55f) {
            return Colors.withAlpha(sample(accent, lum), a);
        }
        return Colors.withAlpha(pastelize(src, 0.22f, 0.88f), a);
    }

    /** Piecewise-linear lookup across {@link Palette#RAMP_STOPS}. */
    public static int sample(int[] ramp, float t) {
        float[] stops = Palette.RAMP_STOPS;
        if (t <= stops[0]) return ramp[0];
        for (int i = 1; i < stops.length; i++) {
            if (t <= stops[i]) {
                float f = (t - stops[i - 1]) / (stops[i] - stops[i - 1]);
                return Colors.lerp(ramp[i - 1], ramp[i], f);
            }
        }
        return ramp[ramp.length - 1];
    }

    /** Lightens and desaturates a colour, keeping its hue. */
    public static int pastelize(int c, float lighten, float saturation) {
        float[] hsl = Colors.toHsl(c);
        float l = hsl[2] + (1 - hsl[2]) * lighten;
        return Colors.fromHsl(hsl[0], hsl[1] * saturation, l, c >>> 24);
    }

    /**
     * Rewrites the enchantment glint so it shimmers in the theme's pink while keeping the
     * original brightness pattern (the glint is drawn additively).
     */
    public static void glint(Canvas c, Palette p) {
        float hue = Colors.toHsl(p.glint)[0];
        for (int i = 0; i < c.px.length; i++) {
            int src = c.px[i];
            int a = src >>> 24;
            if (a == 0) continue;
            float v = Math.max(Colors.red(src), Math.max(Colors.green(src), Colors.blue(src))) / 255f;
            // HSV(hue, 0.55, v) expressed through HSL
            float s = 0.55f;
            float l = v * (1 - s / 2);
            float sl = (l == 0 || l == 1) ? 0 : (v - l) / Math.min(l, 1 - l);
            c.px[i] = Colors.fromHsl(hue, sl, l, a);
        }
    }

    /** Softens a colourful status-effect icon and pulls its outline toward the theme ink. */
    public static void effectIcon(Canvas c, Palette p) {
        float themeHue = Colors.toHsl(p.heart)[0];
        for (int i = 0; i < c.px.length; i++) {
            int src = c.px[i];
            int a = src >>> 24;
            if (a == 0) continue;
            float[] hsl = Colors.toHsl(src);
            if (hsl[2] < 0.2f) {
                c.px[i] = Colors.withAlpha(Colors.lerp(p.ink, src, 0.35f), a);
                continue;
            }
            float hue = hsl[0];
            if (hsl[1] < 0.12f) hue = themeHue;
            float l = 0.22f + hsl[2] * 0.78f;
            c.px[i] = Colors.fromHsl(hue, hsl[1] * 0.78f, l, a);
        }
    }

    /**
     * Gives a rectangle (template pixels) 2px rounded corners: the corner pixel and its two
     * neighbours become transparent and the diagonal pixel takes the border colour found two
     * pixels along the edge, so white-bordered (hovered) buttons stay white.
     */
    public static void roundCorners(Canvas c, int x, int y, int w, int h) {
        int[][] corners = {{x, y, 1, 1}, {x + w - 1, y, -1, 1}, {x, y + h - 1, 1, -1}, {x + w - 1, y + h - 1, -1, -1}};
        for (int[] k : corners) {
            int cx = k[0], cy = k[1], dx = k[2], dy = k[3];
            int outline = c.get((cx + 2 * dx) * c.scale, cy * c.scale);
            c.set(cx, cy, 0);
            c.set(cx + dx, cy, 0);
            c.set(cx, cy + dy, 0);
            c.set(cx + dx, cy + dy, outline);
        }
    }
}
