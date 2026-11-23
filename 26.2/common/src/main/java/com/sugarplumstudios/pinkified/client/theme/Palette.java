package com.sugarplumstudios.pinkified.client.theme;

import com.sugarplumstudios.pinkified.client.util.Colors;

/**
 * Every colour a theme needs, from the GUI recolour ramp to particle tints.
 *
 * <p>The six "ramp" colours replace vanilla's interface greys one-for-one
 * (#000000, #373737, #555555, #8B8B8B, #C6C6C6, #FFFFFF), so recoloured textures keep
 * their original shading and every slot, border and bevel still lines up.
 */
public final class Palette {
    // neutral ramp
    public final int ink, deep, shadow, slot, base, light;
    // accent ramp, used for hover/focus states
    public final int accentDeep, accent, accentLight;
    // text drawn by Pinkified's own screens
    public final int text, title;
    // hearts
    public final int heart, heartShade, heartShine, heartInk, heartEmpty;
    // food
    public final FoodStyle food;
    public final int foodMain, foodShade, foodAccent, foodAccentShade, foodShine;
    // armor
    public final int armorMain, armorShade, armorEmblem;
    // experience bar
    public final int xpStart, xpEnd;
    // menu sky gradient
    public final int skyTop, skyBottom;
    // decorations and particles
    public final int[] petals;
    public final int sparkle;
    // tooltips
    public final int tooltipBg, tooltipBorderTop, tooltipBorderBottom;
    // tint of the "Pinkified" word in the main menu logo
    public final int logo;
    // hue source for the enchantment glint
    public final int glint;

    private Palette(Builder b) {
        ink = b.ink;
        deep = b.deep;
        shadow = b.shadow;
        slot = b.slot;
        base = b.base;
        light = b.light;
        accentDeep = b.accentDeep;
        accent = b.accent;
        accentLight = b.accentLight;
        text = b.text;
        title = b.title;
        heart = b.heart;
        heartShade = b.heartShade;
        heartShine = b.heartShine;
        heartInk = b.heartInk;
        heartEmpty = b.heartEmpty;
        food = b.food;
        foodMain = b.foodMain;
        foodShade = b.foodShade;
        foodAccent = b.foodAccent;
        foodAccentShade = b.foodAccentShade;
        foodShine = b.foodShine;
        armorMain = b.armorMain;
        armorShade = b.armorShade;
        armorEmblem = b.armorEmblem;
        xpStart = b.xpStart;
        xpEnd = b.xpEnd;
        skyTop = b.skyTop;
        skyBottom = b.skyBottom;
        petals = b.petals.clone();
        sparkle = b.sparkle;
        tooltipBg = b.tooltipBg;
        tooltipBorderTop = b.tooltipBorderTop;
        tooltipBorderBottom = b.tooltipBorderBottom;
        logo = b.logo;
        glint = b.glint == 0 ? b.heart : b.glint;
    }

    /** Positions of the neutral ramp stops (the luminance of vanilla's GUI greys). */
    public static final float[] RAMP_STOPS = {0f, 0.2157f, 0.3333f, 0.5451f, 0.7765f, 1f};

    public int[] neutralRamp() {
        return new int[]{ink, deep, shadow, slot, base, light};
    }

    public int[] accentRamp() {
        return new int[]{ink, Colors.lerp(ink, accentDeep, 0.6f), accentDeep, accent, accentLight, light};
    }

    public int petal(int index) {
        return petals[Math.floorMod(index, petals.length)];
    }

    public static Palette of(ThemeId id, int customPrimary, int customAccent) {
        return switch (id) {
            case STRAWBERRY_MILK -> STRAWBERRY_MILK;
            case LAVENDER_DREAMS -> LAVENDER_DREAMS;
            case PINK_COQUETTE -> PINK_COQUETTE;
            case CHERRY_BLOSSOM -> CHERRY_BLOSSOM;
            case ANGELCORE -> ANGELCORE;
            case PASTEL_PRINCESS -> PASTEL_PRINCESS;
            case CUSTOM -> custom(customPrimary, customAccent);
        };
    }

    // ------------------------------------------------------------------ built-in themes

    public static final Palette STRAWBERRY_MILK = new Builder()
            .ramp(0x4A1D2F, 0x7D3550, 0xB0567A, 0xE38FAD, 0xF8C6D6, 0xFFF4F6)
            .accent(0xA8304F, 0xE2557A, 0xFF9FB8)
            .text(0x5A2440, 0xFFFFFF)
            .heart(0xF2557E, 0xC73A60, 0xFFC2D2, 0x5C1A30, 0x4A2333)
            .food(FoodStyle.STRAWBERRY, 0xF2557E, 0xC73A60, 0x7ED17C, 0x4F9E58, 0xFFE7A8)
            .armor(0xFFE8EF, 0xE8A3BC, 0xF2557E)
            .xp(0xFF7FA6, 0xFFC7D9)
            .sky(0xEE9DBC, 0xF9CCDB)
            .petals(0xFFD1DF, 0xFFB3C9, 0xFFFFFF, 0xFF9CB9)
            .sparkle(0xFFFFFF)
            .tooltip(0x2B1020, 0xF48FB1, 0xB565A7)
            .logo(0xFF8DB4)
            .build();

    public static final Palette LAVENDER_DREAMS = new Builder()
            .ramp(0x2E1F4A, 0x4F3A7A, 0x7A62AE, 0xAC95DC, 0xDCCFF6, 0xF8F4FF)
            .accent(0x9E4A8A, 0xD477B8, 0xF4B4DE)
            .text(0x3D2A63, 0xFFFFFF)
            .heart(0xB583F0, 0x8A5BCB, 0xE6D4FF, 0x34205A, 0x2E2140)
            .food(FoodStyle.MACARON, 0xC9A8F2, 0x9C78D6, 0xFFF4FA, 0xF3C6E4, 0xF1E6FF)
            .armor(0xF1E9FF, 0xB9A2E3, 0xB583F0)
            .xp(0xB583F0, 0xF0B6E6)
            .sky(0xB39BE6, 0xE3D5F8)
            .petals(0xE4D5FF, 0xC9B2F2, 0xFFFFFF, 0xF4C8EC)
            .sparkle(0xFFFFFF)
            .tooltip(0x1E1430, 0xC9A8F2, 0x7A62AE)
            .logo(0xC9A8F2)
            .glint(0xB583F0)
            .build();

    public static final Palette PINK_COQUETTE = new Builder()
            .ramp(0x4B1C2A, 0x7E3346, 0xB85C75, 0xEBA4B8, 0xFBD7E0, 0xFFF9F3)
            .accent(0x9E2440, 0xD94A6B, 0xF59AAE)
            .text(0x5A2234, 0xFFFFFF)
            .heart(0xE8456B, 0xB52D4F, 0xFFC4D1, 0x4B1C2A, 0x3F1E28)
            .food(FoodStyle.DONUT, 0xF59AB8, 0xD9718F, 0xF2C99B, 0xC9925F, 0xFFFFFF)
            .armor(0xFFF3EC, 0xEDB9C6, 0xE8456B)
            .xp(0xF06D8E, 0xFFC4D3)
            .sky(0xF2A9BE, 0xFDE0E7)
            .petals(0xFFD9E2, 0xFFFFFF, 0xF7B5C7, 0xFFEFF3)
            .sparkle(0xFFFFFF)
            .tooltip(0x2A1118, 0xF59AAE, 0xB85C75)
            .logo(0xFF9EBB)
            .build();

    public static final Palette CHERRY_BLOSSOM = new Builder()
            .ramp(0x4A2430, 0x7A4253, 0xA9677D, 0xE8ABC0, 0xFCDEE8, 0xFFF8FA)
            .accent(0xA84A72, 0xE07CA6, 0xF9BFD5)
            .text(0x5B2E3D, 0xFFFFFF)
            .heart(0xF27BA3, 0xCF5A84, 0xFFD6E4, 0x4A2430, 0x3D2530)
            .food(FoodStyle.DANGO, 0xF7A8C4, 0xD97C9F, 0xA6D98C, 0x79B067, 0xFFF8F0)
            .armor(0xFFF0F5, 0xE9B5C8, 0xF27BA3)
            .xp(0xF27BA3, 0xFFE0EA)
            .sky(0xF0AAC5, 0xFFE4EE)
            .petals(0xFFD6E4, 0xFBC0D5, 0xFFFFFF, 0xF5A3C0)
            .sparkle(0xFFF6FA)
            .tooltip(0x2C1219, 0xF9BFD5, 0xA9677D)
            .logo(0xF7A8C4)
            .build();

    public static final Palette ANGELCORE = new Builder()
            .ramp(0x4D4232, 0x6F6047, 0x907E60, 0xCDBFA2, 0xF2ECE0, 0xFFFFFF)
            .accent(0x5E7FA8, 0x8FB1D8, 0xCFE3F5)
            .text(0x4D4232, 0xFFFFFF)
            .heart(0xFFE4EC, 0xF2B8C9, 0xFFFFFF, 0xA8802E, 0x5A4E3A)
            .food(FoodStyle.CUPCAKE, 0xFFFFFF, 0xE8DCC8, 0xF3D27A, 0xC9A54E, 0xFF9DBA)
            .armor(0xFFFFFF, 0xE3D7BD, 0xF3D27A)
            .xp(0xF3D27A, 0xFFF6D8)
            .sky(0xBFD5EE, 0xFFF1E2)
            .petals(0xFFFFFF, 0xFFF3D6, 0xE6F0FA, 0xFFE6EE)
            .sparkle(0xFFF6D8)
            .tooltip(0x2A2418, 0xF3D27A, 0xA8977A)
            .logo(0xFFE3A0)
            .glint(0xF3C25A)
            .build();

    public static final Palette PASTEL_PRINCESS = new Builder()
            .ramp(0x45193F, 0x763063, 0xA9528F, 0xE093C7, 0xF7CBE7, 0xFFF5FC)
            .accent(0x6B4FA8, 0x9F86DB, 0xD3C6F5)
            .text(0x551E4C, 0xFFFFFF)
            .heart(0xF06EB4, 0xC44B8E, 0xFFD0EA, 0x45193F, 0x3A1A35)
            .food(FoodStyle.BONBON, 0xF7A6D6, 0xD27AB2, 0xFFD66E, 0xD9A93E, 0xFFFFFF)
            .armor(0xFFF0F8, 0xE2A9CE, 0xFFD66E)
            .xp(0xF06EB4, 0xFFD66E)
            .sky(0xE49DCF, 0xFBDDF0)
            .petals(0xFFD0EA, 0xE8C5F5, 0xFFF0B8, 0xFFFFFF)
            .sparkle(0xFFF6D0)
            .tooltip(0x2A0F26, 0xFFD66E, 0xA9528F)
            .logo(0xF7A6D6)
            .build();

    /** Derives a full palette from two colours picked in the settings screen. */
    public static Palette custom(int primary, int accentColor) {
        float[] p = Colors.toHsl(primary);
        float[] a = Colors.toHsl(accentColor);
        float h = p[0], s = Math.max(0.12f, p[1]);
        float ah = a[0], as = Math.max(0.15f, a[1]);
        int ink = Colors.fromHsl(h, s * 0.45f, 0.15f);
        int deep = Colors.fromHsl(h, s * 0.42f, 0.30f);
        int shadow = Colors.fromHsl(h, s * 0.45f, 0.48f);
        int slot = Colors.fromHsl(h, s * 0.62f, 0.72f);
        int base = Colors.fromHsl(h, s * 0.78f, 0.87f);
        int light = Colors.fromHsl(h, s * 0.9f, 0.97f);
        int heart = Colors.fromHsl(h, Math.min(1f, s * 1.1f + 0.15f), 0.64f);
        return new Builder()
                .rampArgb(ink, deep, shadow, slot, base, light)
                .accentArgb(Colors.fromHsl(ah, as * 0.65f, 0.40f), Colors.fromHsl(ah, as * 0.7f, 0.62f), Colors.fromHsl(ah, as * 0.75f, 0.82f))
                .textArgb(Colors.fromHsl(h, s * 0.5f, 0.22f), Colors.WHITE)
                .heartArgb(heart, Colors.fromHsl(h, Math.min(1f, s + 0.1f), 0.50f), Colors.fromHsl(h, s, 0.86f), ink, Colors.fromHsl(h, s * 0.35f, 0.20f))
                .foodArgb(FoodStyle.MACARON, Colors.fromHsl(h, s * 0.7f, 0.76f), Colors.fromHsl(h, s * 0.7f, 0.62f),
                        Colors.fromHsl(ah, as * 0.5f, 0.92f), Colors.fromHsl(ah, as * 0.5f, 0.80f), Colors.fromHsl(h, s * 0.6f, 0.94f))
                .armorArgb(light, slot, heart)
                .xpArgb(heart, Colors.fromHsl(ah, as * 0.8f, 0.80f))
                .skyArgb(Colors.fromHsl(h, s * 0.68f, 0.76f), Colors.fromHsl(ah, as * 0.55f, 0.90f))
                .petalsArgb(Colors.fromHsl(h, s * 0.8f, 0.88f), Colors.fromHsl(ah, as * 0.6f, 0.86f), Colors.WHITE, Colors.fromHsl(h, s * 0.8f, 0.80f))
                .sparkleArgb(Colors.WHITE)
                .tooltipArgb(Colors.fromHsl(h, s * 0.45f, 0.08f), Colors.fromHsl(h, s * 0.8f, 0.75f), Colors.fromHsl(ah, as * 0.6f, 0.45f))
                .logoArgb(Colors.fromHsl(h, s * 0.9f, 0.76f))
                .build();
    }

    // ------------------------------------------------------------------ builder

    static final class Builder {
        int ink, deep, shadow, slot, base, light;
        int accentDeep, accent, accentLight;
        int text, title;
        int heart, heartShade, heartShine, heartInk, heartEmpty;
        FoodStyle food = FoodStyle.STRAWBERRY;
        int foodMain, foodShade, foodAccent, foodAccentShade, foodShine;
        int armorMain, armorShade, armorEmblem;
        int xpStart, xpEnd;
        int skyTop, skyBottom;
        int[] petals = {Colors.WHITE};
        int sparkle = Colors.WHITE;
        int tooltipBg, tooltipBorderTop, tooltipBorderBottom;
        int logo;
        int glint;

        private static int o(int rgb) {
            return Colors.opaque(rgb);
        }

        Builder ramp(int ink, int deep, int shadow, int slot, int base, int light) {
            return rampArgb(o(ink), o(deep), o(shadow), o(slot), o(base), o(light));
        }

        Builder rampArgb(int ink, int deep, int shadow, int slot, int base, int light) {
            this.ink = ink;
            this.deep = deep;
            this.shadow = shadow;
            this.slot = slot;
            this.base = base;
            this.light = light;
            return this;
        }

        Builder accent(int deep, int mid, int light) {
            return accentArgb(o(deep), o(mid), o(light));
        }

        Builder accentArgb(int deep, int mid, int light) {
            accentDeep = deep;
            accent = mid;
            accentLight = light;
            return this;
        }

        Builder text(int text, int title) {
            return textArgb(o(text), o(title));
        }

        Builder textArgb(int text, int title) {
            this.text = text;
            this.title = title;
            return this;
        }

        Builder heart(int fill, int shade, int shine, int ink, int empty) {
            return heartArgb(o(fill), o(shade), o(shine), o(ink), o(empty));
        }

        Builder heartArgb(int fill, int shade, int shine, int ink, int empty) {
            heart = fill;
            heartShade = shade;
            heartShine = shine;
            heartInk = ink;
            heartEmpty = empty;
            return this;
        }

        Builder food(FoodStyle style, int main, int shade, int accent, int accentShade, int shine) {
            return foodArgb(style, o(main), o(shade), o(accent), o(accentShade), o(shine));
        }

        Builder foodArgb(FoodStyle style, int main, int shade, int accent, int accentShade, int shine) {
            food = style;
            foodMain = main;
            foodShade = shade;
            foodAccent = accent;
            foodAccentShade = accentShade;
            foodShine = shine;
            return this;
        }

        Builder armor(int main, int shade, int emblem) {
            return armorArgb(o(main), o(shade), o(emblem));
        }

        Builder armorArgb(int main, int shade, int emblem) {
            armorMain = main;
            armorShade = shade;
            armorEmblem = emblem;
            return this;
        }

        Builder xp(int start, int end) {
            return xpArgb(o(start), o(end));
        }

        Builder xpArgb(int start, int end) {
            xpStart = start;
            xpEnd = end;
            return this;
        }

        Builder sky(int top, int bottom) {
            return skyArgb(o(top), o(bottom));
        }

        Builder skyArgb(int top, int bottom) {
            skyTop = top;
            skyBottom = bottom;
            return this;
        }

        Builder petals(int... rgb) {
            int[] out = new int[rgb.length];
            for (int i = 0; i < rgb.length; i++) out[i] = o(rgb[i]);
            return petalsArgb(out);
        }

        Builder petalsArgb(int... argb) {
            petals = argb;
            return this;
        }

        Builder sparkle(int rgb) {
            return sparkleArgb(o(rgb));
        }

        Builder sparkleArgb(int argb) {
            sparkle = argb;
            return this;
        }

        Builder tooltip(int bg, int top, int bottom) {
            return tooltipArgb(o(bg), o(top), o(bottom));
        }

        Builder tooltipArgb(int bg, int top, int bottom) {
            tooltipBg = bg;
            tooltipBorderTop = top;
            tooltipBorderBottom = bottom;
            return this;
        }

        Builder logo(int rgb) {
            return logoArgb(o(rgb));
        }

        Builder logoArgb(int argb) {
            logo = argb;
            return this;
        }

        Builder glint(int rgb) {
            glint = o(rgb);
            return this;
        }

        Palette build() {
            return new Palette(this);
        }
    }
}
