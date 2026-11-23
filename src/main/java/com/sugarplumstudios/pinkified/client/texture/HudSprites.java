package com.sugarplumstudios.pinkified.client.texture;

import com.sugarplumstudios.pinkified.client.theme.CrosshairStyle;
import com.sugarplumstudios.pinkified.client.theme.FoodStyle;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.util.Colors;

import java.util.HashMap;
import java.util.Map;

/**
 * Original HUD pixel art (hearts, treats, armor, bubbles, XP bar, hotbar frame, crosshairs),
 * coloured from a {@link Palette} and painted into the exact sprite slots vanilla reads from
 * {@code icons.png} / {@code widgets.png}. Vanilla keeps doing all the HUD logic.
 *
 * <p>Template legend: {@code O} outline, {@code F} fill, {@code S} shade, {@code H} shine,
 * {@code E} emblem, {@code A}/{@code a} accent and its shade, {@code W}/{@code w} secondary
 * light colour, {@code k} stick, {@code .} transparent.
 */
public final class HudSprites {
    private HudSprites() {
    }

    // ------------------------------------------------------------------ templates

    static final String[] HEART = {
            ".OO...OO.",
            "OHHO.OFFO",
            "OHFFOFFFO",
            "OFFFFFFSO",
            ".OFFFFSO.",
            "..OFFSO..",
            "...OSO...",
            "....O....",
    };

    static final String[] HEART_HARDCORE = {
            ".OO...OO.",
            "OHHO.OHFO",
            "OHFFOFFFO",
            "OFFHFFFSO",
            ".OFFFFSO.",
            "..OFFSO..",
            "...OSO...",
            "....O....",
    };

    static final String[] ARMOR = {
            "OOOOOOOOO",
            "OHHFFFFSO",
            "OHFEFEFSO",
            "OFEEEEESO",
            "OFFEEESSO",
            ".OFFESSO.",
            ".OFFFSSO.",
            "..OFSSO..",
            "...OOO...",
    };

    static final String[] BUBBLE = {
            "..OOOOO..",
            ".OHHFFFO.",
            "OHHFFFFFO",
            "OHFFFFFFO",
            "OFFFFFFSO",
            "OFFFFFFSO",
            "OFFFFFSSO",
            ".OFFSSSO.",
            "..OOOOO..",
    };

    static final String[] BUBBLE_POP = {
            "....O....",
            ".O.....O.",
            "...HHH...",
            "..H...H..",
            "OH.....HO",
            "..H...H..",
            "...HHH...",
            ".O.....O.",
            "....O....",
    };

    static final String[] STRAWBERRY = {
            "...A.A...",
            "..AAaAA..",
            ".OFAAAFO.",
            "OFHFFFHFO",
            "OFFFHFFSO",
            "OHFFFFHSO",
            ".OFFHFSO.",
            "..OFSSO..",
            "...OOO...",
    };

    static final String[] MACARON = {
            ".........",
            "..OOOOO..",
            ".OHFFFFO.",
            "OFHFFFFSO",
            "OAAAAAAAO",
            "OaaaaaaaO",
            "OFFFFFFSO",
            ".OFFFSSO.",
            "..OOOOO..",
    };

    static final String[] DONUT = {
            "..OOOOO..",
            ".OFHFFHO.",
            "OFFFFFFFO",
            "OHFOOOFHO",
            "OFFO.OFSO",
            "OAFOOOFSO",
            "OaAFFFSAO",
            ".OaAAAaO.",
            "..OOOOO..",
    };

    static final String[] DANGO = {
            "......kk.",
            "....OOOk.",
            "...OFFSO.",
            "..OOFHSO.",
            ".OWWOOO..",
            "OWWwO....",
            "OAAO.....",
            "OaaO.....",
            ".OO......",
    };

    static final String[] CUPCAKE = {
            "....H....",
            "..OOHOO..",
            ".OFFFFFO.",
            "OFHFFFFSO",
            "OSFFFFSSO",
            ".OAaAaAO.",
            ".OAaAaAO.",
            "..OAaAO..",
            "..OOOOO..",
    };

    static final String[] BONBON = {
            ".........",
            "O..OOO..O",
            "AOOFFHOOA",
            "AAOFFFOAA",
            "aAOFFSOAa",
            "AAOFSSOAA",
            "AOOFSSOOA",
            "O..OOO..O",
            ".........",
    };

    static final String[] SELECTION_BOW = {
            "OO....OO",
            "OAO..OAO",
            "OAAOOAAO",
            "OAO..OAO",
            "OO....OO",
    };

    static final String[] CROSS_HEART = HEART;

    static final String[] CROSS_SPARKLE = {
            "....O....",
            "...OFO...",
            "...OFO...",
            "..OFHFO..",
            "OOFHHHFOO",
            "..OFHFO..",
            "...OFO...",
            "...OFO...",
            "....O....",
    };

    static final String[] CROSS_FLOWER = {
            "..OOO..",
            ".OFFFO.",
            "OFOHOFO",
            "OFHSHFO",
            "OFOHOFO",
            ".OFFFO.",
            "..OOO..",
    };

    static final String[] CROSS_BOW = {
            "OO.....OO",
            "OFOO.OOFO",
            "OFFOHOFFO",
            "OFOO.OOFO",
            "OO.....OO",
    };

    static final String[] CROSS_DOT = {
            ".OOO.",
            "OFHFO",
            "OHFFO",
            "OFFFO",
            ".OOO.",
    };

    static final String[] CROSS_PLUS = {
            "....O....",
            "...OFO...",
            "...OFO...",
            "OOOOFOOOO",
            "OFFFHFFFO",
            "OOOOFOOOO",
            "...OFO...",
            "...OFO...",
            "....O....",
    };

    // ------------------------------------------------------------------ helpers

    static Map<Character, Integer> pal(Object... kv) {
        Map<Character, Integer> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((Character) kv[i], (Integer) kv[i + 1]);
        return m;
    }

    /** Palette that turns every coloured template character into "empty slot" colours. */
    static Map<Character, Integer> emptyPal(int outline, int empty) {
        Map<Character, Integer> m = new HashMap<>();
        for (char ch : "FSHEAawWk".toCharArray()) m.put(ch, empty);
        m.put('O', outline);
        return m;
    }

    private static int sick(int c) {
        return Colors.lerp(c, 0xFF9FCB6B, 0.55f);
    }

    // ------------------------------------------------------------------ icons.png

    private static final int[] NORMAL_HEART_X = {52, 70};
    private static final int HEART_ROW = 0, HARDCORE_ROW = 45;

    /** Paints all player and mount heart sprites. */
    public static void paintHearts(Canvas c, Palette p) {
        for (int row : new int[]{HEART_ROW, HARDCORE_ROW}) {
            String[] t = row == HARDCORE_ROW ? HEART_HARDCORE : HEART;
            int empty = Colors.withAlpha(p.heartEmpty, 0xD8);
            Map<Character, Integer> container = emptyPal(p.heartInk, empty);
            Map<Character, Integer> containerLit = emptyPal(p.light, empty);
            for (int x : new int[]{16, 34}) {
                c.clear(x, row, 9, 9);
                c.stamp(t, x, row, container);
            }
            for (int x : new int[]{25, 43}) {
                c.clear(x, row, 9, 9);
                c.stamp(t, x, row, containerLit);
            }
            heartSet(c, t, 52, row, p.heart, p.heartShade, p.heartShine, p.heartInk, true);
            heartSet(c, t, 88, row, 0xFFA8D26B, 0xFF7EAD4A, 0xFFE4F7C2, p.heartInk, true);
            heartSet(c, t, 124, row, 0xFF6B5A66, 0xFF4A3D46, 0xFF9C8A96, 0xFF1E1419, true);
            heartSet(c, t, 160, row, 0xFFFFD66E, 0xFFE0AE3E, 0xFFFFF3C2, p.heartInk, false);
            heartSet(c, t, 178, row, 0xFFA8DDF5, 0xFF74B8DE, 0xFFE8F8FF, p.heartInk, false);
        }
        // mount hearts use the accent colours so they read as "not you"
        int empty = Colors.withAlpha(p.heartEmpty, 0xD8);
        for (int x : new int[]{52, 70}) {
            c.clear(x, 9, 9, 9);
            c.stamp(HEART, x, 9, emptyPal(p.heartInk, empty));
        }
        for (int x : new int[]{61, 79}) {
            c.clear(x, 9, 9, 9);
            c.stamp(HEART, x, 9, emptyPal(p.light, empty));
        }
        heartSet(c, HEART, 88, 9, p.accent, p.accentDeep, p.accentLight, p.heartInk, true);
    }

    /** full, half, (blink full, blink half) starting at x. */
    private static void heartSet(Canvas c, String[] t, int x, int y, int fill, int shade, int shine, int ink, boolean blink) {
        Map<Character, Integer> normal = pal('O', ink, 'F', fill, 'S', shade, 'H', shine);
        int bf = Colors.lerp(fill, 0xFFFFFFFF, 0.45f), bs = Colors.lerp(shade, 0xFFFFFFFF, 0.35f);
        Map<Character, Integer> lit = pal('O', ink, 'F', bf, 'S', bs, 'H', 0xFFFFFFFF);
        c.clear(x, y, 9, 9);
        c.stamp(t, x, y, normal);
        c.clear(x + 9, y, 9, 9);
        c.stamp(t, x + 9, y, normal, 0, 4);
        if (blink) {
            c.clear(x + 18, y, 9, 9);
            c.stamp(t, x + 18, y, lit);
            c.clear(x + 27, y, 9, 9);
            c.stamp(t, x + 27, y, lit, 0, 4);
        }
    }

    public static void paintArmor(Canvas c, Palette p) {
        int ink = p.heartInk;
        Map<Character, Integer> full = pal('O', ink, 'F', p.armorMain, 'S', p.armorShade, 'H', 0xFFFFFFFF, 'E', p.armorEmblem);
        Map<Character, Integer> empty = emptyPal(ink, Colors.withAlpha(p.heartEmpty, 0xC8));
        c.clear(16, 9, 9, 9);
        c.stamp(ARMOR, 16, 9, empty);
        c.clear(25, 9, 9, 9);
        c.stamp(ARMOR, 25, 9, empty);
        c.stamp(ARMOR, 25, 9, full, 0, 4);
        for (int x : new int[]{34, 43}) {
            c.clear(x, 9, 9, 9);
            c.stamp(ARMOR, x, 9, full);
        }
    }

    public static void paintAir(Canvas c, Palette p) {
        int fill = Colors.withAlpha(Colors.lerp(p.accentLight, p.light, 0.35f), 0xD0);
        int shade = Colors.withAlpha(p.accent, 0xE0);
        Map<Character, Integer> bubble = pal('O', p.accentDeep, 'F', fill, 'S', shade, 'H', 0xFFFFFFFF);
        c.clear(16, 18, 9, 9);
        c.stamp(BUBBLE, 16, 18, bubble);
        c.clear(25, 18, 9, 9);
        c.stamp(BUBBLE_POP, 25, 18, pal('O', p.accentDeep, 'H', Colors.withAlpha(p.light, 0xF0)));
    }

    static String[] foodTemplate(FoodStyle style) {
        return switch (style) {
            case MACARON -> MACARON;
            case DONUT -> DONUT;
            case DANGO -> DANGO;
            case CUPCAKE -> CUPCAKE;
            case BONBON -> BONBON;
            default -> STRAWBERRY;
        };
    }

    public static void paintFood(Canvas c, Palette p, FoodStyle style) {
        String[] t = foodTemplate(style == FoodStyle.AUTO ? p.food : style);
        int ink = p.heartInk;
        int empty = Colors.withAlpha(p.heartEmpty, 0xC8);
        Map<Character, Integer> full = foodPal(p, ink, false);
        Map<Character, Integer> hunger = foodPal(p, Colors.lerp(ink, 0xFF2F4A1E, 0.5f), true);
        // backgrounds: normal (16), highlight variants vanilla rarely uses (25/34/43), rotten (124), hunger (133)
        for (int x : new int[]{16, 34, 124}) {
            c.clear(x, 27, 9, 9);
            c.stamp(t, x, 27, emptyPal(ink, empty));
        }
        for (int x : new int[]{25, 43}) {
            c.clear(x, 27, 9, 9);
            c.stamp(t, x, 27, emptyPal(p.light, empty));
        }
        c.clear(133, 27, 9, 9);
        c.stamp(t, 133, 27, emptyPal(Colors.lerp(ink, 0xFF5E8A3A, 0.6f), Colors.withAlpha(sick(p.heartEmpty), 0xC8)));
        foodPair(c, t, 52, full);
        foodPair(c, t, 70, full);
        foodPair(c, t, 88, hunger);
        foodPair(c, t, 106, hunger);
    }

    private static void foodPair(Canvas c, String[] t, int x, Map<Character, Integer> pal) {
        c.clear(x, 27, 9, 9);
        c.stamp(t, x, 27, pal);
        c.clear(x + 9, 27, 9, 9);
        c.stamp(t, x + 9, 27, pal, 4, 8);
    }

    private static Map<Character, Integer> foodPal(Palette p, int ink, boolean hunger) {
        int f = p.foodMain, s = p.foodShade, a = p.foodAccent, as = p.foodAccentShade, h = p.foodShine;
        int w = p.foodShine, ws = Colors.lerp(p.foodShine, p.foodShade, 0.35f), k = Colors.lerp(ink, p.foodAccentShade, 0.4f);
        if (hunger) {
            f = sick(f);
            s = sick(s);
            a = sick(a);
            as = sick(as);
            h = sick(h);
            w = sick(w);
            ws = sick(ws);
        }
        return pal('O', ink, 'F', f, 'S', s, 'A', a, 'a', as, 'H', h, 'W', w, 'w', ws, 'k', k);
    }

    /** Experience bar (y 64/69) and the horse jump bar (y 84/89). Pixels are fully opaque or clear. */
    public static void paintXp(Canvas c, Palette p) {
        paintBar(c, p, 64, 69, p.xpStart, p.xpEnd, true);
    }

    public static void paintJump(Canvas c, Palette p) {
        paintBar(c, p, 84, 89, p.accent, p.accentLight, false);
    }

    private static void paintBar(Canvas c, Palette p, int bgY, int fillY, int start, int end, boolean sparkles) {
        int w = 182;
        c.clear(0, bgY, w, 5);
        c.clear(0, fillY, w, 5);
        int ink = p.heartInk;
        int trough = Colors.lerp(p.deep, ink, 0.35f);
        int notch = Colors.lerp(trough, p.shadow, 0.45f);
        for (int x = 0; x < w; x++) {
            boolean end0 = x == 0 || x == w - 1;
            for (int y = 0; y < 5; y++) {
                boolean edge = y == 0 || y == 4 || end0;
                if (end0 && (y == 0 || y == 4)) continue;
                c.set(x, bgY + y, edge ? ink : ((x % 20 == 10 && y == 2) ? notch : trough));
                if (edge) {
                    c.set(x, fillY + y, ink);
                } else {
                    float t = x / (float) (w - 1);
                    int col = Colors.lerp(start, end, t);
                    if (y == 1) col = Colors.lerp(col, 0xFFFFFFFF, 0.38f);
                    if (y == 3) col = Colors.lerp(col, ink, 0.18f);
                    if (sparkles && y == 1 && x % 23 == 11) col = 0xFFFFFFFF;
                    c.set(x, fillY + y, col);
                }
            }
        }
    }

    // ------------------------------------------------------------------ widgets.png

    /** The hotbar selection frame at (0,22), 24x24. */
    public static void paintSelection(Canvas c, Palette p) {
        int x0 = 0, y0 = 22;
        c.clear(x0, y0, 24, 24);
        for (int y = 0; y < 24; y++)
            for (int x = 0; x < 24; x++) {
                int ring = Math.min(Math.min(x, 23 - x), Math.min(y, 23 - y));
                int col = switch (ring) {
                    case 0 -> p.ink;
                    case 1 -> p.light;
                    case 2 -> p.accent;
                    default -> 0;
                };
                if (col != 0) c.set(x0 + x, y0 + y, col);
            }
        Recolor.roundCorners(c, x0, y0, 24, 24);
        c.stamp(SELECTION_BOW, x0 + 8, y0, pal('O', p.ink, 'A', p.accentLight));
    }

    // ------------------------------------------------------------------ crosshair

    /** A 15x15 ARGB crosshair sprite in the theme colours. */
    public static Canvas crosshair(CrosshairStyle style, Palette p) {
        Canvas c = new Canvas(15, 15);
        String[] t = switch (style) {
            case SPARKLE -> CROSS_SPARKLE;
            case FLOWER -> CROSS_FLOWER;
            case BOW -> CROSS_BOW;
            case DOT -> CROSS_DOT;
            case CROSS -> CROSS_PLUS;
            default -> CROSS_HEART;
        };
        int ink = Colors.withAlpha(p.heartInk, 0xE0);
        Map<Character, Integer> m = pal('O', ink, 'F', p.heart, 'S', p.heartShade, 'H', Colors.lerp(p.heartShine, 0xFFFFFFFF, 0.4f));
        int ox = (15 - t[0].length()) / 2, oy = (15 - t.length) / 2;
        if (style == CrosshairStyle.HEART) oy = 4;
        c.stamp(t, ox, oy, m);
        return c;
    }
}
