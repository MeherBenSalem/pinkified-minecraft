package com.sugarplumstudios.pinkified.client.util;

/**
 * Small ARGB colour toolkit. Colours are packed {@code 0xAARRGGBB} ints throughout the mod;
 * this class has no Minecraft dependencies so it can be used by offline tools as well.
 */
public final class Colors {
    public static final int WHITE = 0xFFFFFFFF;
    public static final int BLACK = 0xFF000000;

    private Colors() {
    }

    public static int alpha(int c) {
        return c >>> 24;
    }

    public static int red(int c) {
        return (c >> 16) & 0xFF;
    }

    public static int green(int c) {
        return (c >> 8) & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    public static int argb(int a, int r, int g, int b) {
        return (clamp255(a) << 24) | (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b);
    }

    public static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static int withAlpha(int c, int a) {
        return (clamp255(a) << 24) | (c & 0xFFFFFF);
    }

    public static int withAlpha(int c, float a) {
        return withAlpha(c, Math.round(a * 255f));
    }

    /** Multiplies the existing alpha by {@code f}. */
    public static int fade(int c, float f) {
        return withAlpha(c, Math.round(alpha(c) * clamp01(f)));
    }

    public static int lerp(int c0, int c1, float t) {
        t = clamp01(t);
        return argb(
                Math.round(alpha(c0) + (alpha(c1) - alpha(c0)) * t),
                Math.round(red(c0) + (red(c1) - red(c0)) * t),
                Math.round(green(c0) + (green(c1) - green(c0)) * t),
                Math.round(blue(c0) + (blue(c1) - blue(c0)) * t));
    }

    public static int lighten(int c, float t) {
        return withAlpha(lerp(c, WHITE, t), alpha(c));
    }

    public static int darken(int c, float t) {
        return withAlpha(lerp(c, BLACK, t), alpha(c));
    }

    /** Perceived brightness in [0,1]. */
    public static float luma(int c) {
        return (0.299f * red(c) + 0.587f * green(c) + 0.114f * blue(c)) / 255f;
    }

    /** Returns {h in [0,360), s in [0,1], l in [0,1]}. */
    public static float[] toHsl(int c) {
        float r = red(c) / 255f, g = green(c) / 255f, b = blue(c) / 255f;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        float l = (max + min) / 2f;
        float h = 0, s = 0;
        float d = max - min;
        if (d > 1e-5f) {
            s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
            if (max == r) h = (g - b) / d + (g < b ? 6 : 0);
            else if (max == g) h = (b - r) / d + 2;
            else h = (r - g) / d + 4;
            h *= 60f;
        }
        return new float[]{h, s, l};
    }

    public static int fromHsl(float h, float s, float l) {
        return fromHsl(h, s, l, 255);
    }

    public static int fromHsl(float h, float s, float l, int alpha) {
        h = ((h % 360f) + 360f) % 360f / 360f;
        s = clamp01(s);
        l = clamp01(l);
        float r, g, b;
        if (s < 1e-5f) {
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hue(p, q, h + 1f / 3f);
            g = hue(p, q, h);
            b = hue(p, q, h - 1f / 3f);
        }
        return argb(alpha, Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
    }

    private static float hue(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f / 6f) return p + (q - p) * 6 * t;
        if (t < 0.5f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6;
        return p;
    }

    /** Parses "#RRGGBB" / "RRGGBB" / "#AARRGGBB"; returns {@code fallback} on bad input. */
    public static int parseHex(String s, int fallback) {
        if (s == null) return fallback;
        String t = s.trim();
        if (t.startsWith("#")) t = t.substring(1);
        try {
            if (t.length() == 6) return 0xFF000000 | Integer.parseUnsignedInt(t, 16);
            if (t.length() == 8) return (int) Long.parseLong(t, 16);
        } catch (NumberFormatException ignored) {
        }
        return fallback;
    }

    public static String toHex(int c) {
        return String.format("#%06X", c & 0xFFFFFF);
    }

    /** Converts ARGB to the ABGR layout used by {@code NativeImage}. */
    public static int toAbgr(int argb) {
        return (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16);
    }

    /** Converts NativeImage ABGR to ARGB (the transform is its own inverse). */
    public static int fromAbgr(int abgr) {
        return toAbgr(abgr);
    }

    public static float clamp01(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
