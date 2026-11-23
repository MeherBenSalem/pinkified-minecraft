import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates every static texture shipped with Pinkified Minecraft.
 *
 * All art is original pixel art drawn procedurally from the templates below, so the
 * mod never redistributes Mojang assets. Run from the project root:
 *
 *   java tools/assetgen/AssetGenerator.java <version>/common/src/main/resources
 *
 * (once for each workspace: 1.20.1, 1.21.1, 26.2 and 26.3 ship the same art)
 *
 * Sprites that the mod tints at runtime (menu decorations, particles, logo base) are
 * drawn in grayscale so a single texture can follow any theme colour.
 */
public final class AssetGenerator {

    // ------------------------------------------------------------------ canvas

    static final class Img {
        final int w, h;
        final int[] p;

        Img(int w, int h) {
            this.w = w;
            this.h = h;
            this.p = new int[w * h];
        }

        boolean in(int x, int y) {
            return x >= 0 && y >= 0 && x < w && y < h;
        }

        int get(int x, int y) {
            return in(x, y) ? p[y * w + x] : 0;
        }

        void set(int x, int y, int c) {
            if (in(x, y)) p[y * w + x] = c;
        }

        void blend(int x, int y, int c) {
            if (!in(x, y)) return;
            int sa = c >>> 24;
            if (sa == 0) return;
            int d = p[y * w + x];
            int da = d >>> 24;
            float a = sa / 255f, b = da / 255f * (1 - a);
            float oa = a + b;
            if (oa <= 0) return;
            int r = (int) ((((c >> 16) & 255) * a + ((d >> 16) & 255) * b) / oa);
            int g = (int) ((((c >> 8) & 255) * a + ((d >> 8) & 255) * b) / oa);
            int bl = (int) (((c & 255) * a + (d & 255) * b) / oa);
            p[y * w + x] = ((int) (oa * 255f + 0.5f) << 24) | (r << 16) | (g << 8) | bl;
        }

        void fill(int x0, int y0, int x1, int y1, int c) {
            for (int y = y0; y < y1; y++) for (int x = x0; x < x1; x++) set(x, y, c);
        }

        void disc(double cx, double cy, double r, int c) {
            for (int y = (int) Math.floor(cy - r - 1); y <= cy + r + 1; y++)
                for (int x = (int) Math.floor(cx - r - 1); x <= cx + r + 1; x++) {
                    double dx = x + 0.5 - cx, dy = y + 0.5 - cy;
                    if (dx * dx + dy * dy <= r * r) set(x, y, c);
                }
        }

        void ellipse(double cx, double cy, double rx, double ry, double angle, int c) {
            double ca = Math.cos(angle), sa = Math.sin(angle);
            double m = Math.max(rx, ry) + 1;
            for (int y = (int) Math.floor(cy - m); y <= cy + m; y++)
                for (int x = (int) Math.floor(cx - m); x <= cx + m; x++) {
                    double dx = x + 0.5 - cx, dy = y + 0.5 - cy;
                    double u = dx * ca + dy * sa, v = -dx * sa + dy * ca;
                    if ((u * u) / (rx * rx) + (v * v) / (ry * ry) <= 1.0) set(x, y, c);
                }
        }

        void line(int x0, int y0, int x1, int y1, int c) {
            int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
            int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
            while (true) {
                set(x0, y0, c);
                if (x0 == x1 && y0 == y1) break;
                int e2 = 2 * err;
                if (e2 >= dy) { err += dy; x0 += sx; }
                if (e2 <= dx) { err += dx; y0 += sy; }
            }
        }

        /** Paints a character template; '.' and ' ' are transparent. */
        void stamp(String[] rows, int ox, int oy, Map<Character, Integer> pal) {
            for (int y = 0; y < rows.length; y++)
                for (int x = 0; x < rows[y].length(); x++) {
                    char ch = rows[y].charAt(x);
                    Integer c = pal.get(ch);
                    if (c != null) set(ox + x, oy + y, c);
                }
        }

        /** Adds a 1px outline (4-neighbourhood) of colour c around opaque pixels. */
        void outline(int c) {
            outline(c, false);
        }

        void outline(int c, boolean diagonal) {
            int[] copy = p.clone();
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) {
                    if ((copy[y * w + x] >>> 24) != 0) continue;
                    boolean near = false;
                    for (int dy = -1; dy <= 1 && !near; dy++)
                        for (int dx = -1; dx <= 1 && !near; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            if (!diagonal && dx != 0 && dy != 0) continue;
                            int nx = x + dx, ny = y + dy;
                            if (nx >= 0 && ny >= 0 && nx < w && ny < h && (copy[ny * w + nx] >>> 24) > 100) near = true;
                        }
                    if (near) p[y * w + x] = c;
                }
        }

        /** Darkens opaque pixels that touch transparency on their bottom/right side. */
        void innerShade(int c) {
            int[] copy = p.clone();
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) {
                    if ((copy[y * w + x] >>> 24) == 0) continue;
                    boolean edge = (y + 1 >= h || (copy[(y + 1) * w + x] >>> 24) == 0)
                            || (x + 1 >= w || (copy[y * w + x + 1] >>> 24) == 0);
                    if (edge) p[y * w + x] = c;
                }
        }

        void draw(Img src, int ox, int oy) {
            for (int y = 0; y < src.h; y++)
                for (int x = 0; x < src.w; x++) blend(ox + x, oy + y, src.p[y * src.w + x]);
        }

        Img scaled(int k) {
            Img o = new Img(w * k, h * k);
            for (int y = 0; y < o.h; y++)
                for (int x = 0; x < o.w; x++) o.p[y * o.w + x] = p[(y / k) * w + (x / k)];
            return o;
        }

        Img padded(int nw, int nh) {
            Img o = new Img(nw, nh);
            o.draw(this, (nw - w) / 2, (nh - h) / 2);
            return o;
        }

        void save(File f) throws IOException {
            f.getParentFile().mkdirs();
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            bi.setRGB(0, 0, w, h, p, 0, w);
            ImageIO.write(bi, "png", f);
        }
    }

    static Map<Character, Integer> pal(Object... kv) {
        Map<Character, Integer> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((Character) kv[i], (Integer) kv[i + 1]);
        return m;
    }

    static int rgb(int rgb) {
        return 0xFF000000 | rgb;
    }

    static int argb(int a, int rgb) {
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    static int lerp(int c0, int c1, double t) {
        t = Math.max(0, Math.min(1, t));
        int a = (int) Math.round(((c0 >>> 24) & 255) * (1 - t) + ((c1 >>> 24) & 255) * t);
        int r = (int) Math.round(((c0 >> 16) & 255) * (1 - t) + ((c1 >> 16) & 255) * t);
        int g = (int) Math.round(((c0 >> 8) & 255) * (1 - t) + ((c1 >> 8) & 255) * t);
        int b = (int) Math.round((c0 & 255) * (1 - t) + (c1 & 255) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // Grayscale shades for tintable sprites.
    static final int W = rgb(0xFFFFFF), L = rgb(0xEDEDED), M = rgb(0xD2D2D2), S = rgb(0xB4B4B4), D = rgb(0x8E8E8E);

    static File root;

    static void save(Img img, String path) throws IOException {
        img.save(new File(root, path));
        System.out.println("  wrote " + path + " (" + img.w + "x" + img.h + ")");
    }

    // ------------------------------------------------------------------ shapes

    static final String[] HEART = {
            ".OO...OO.",
            "OWWO.OWWO",
            "OWWWOWWWO",
            "OWWWWWWSO",
            ".OWWWWSO.",
            "..OWWSO..",
            "...OSO...",
            "....O....",
    };

    static final String[] SPARKLE_BIG = {
            "....W....",
            "....W....",
            "...MWM...",
            "..MWWWM..",
            "WWWWWWWWW",
            "..MWWWM..",
            "...MWM...",
            "....W....",
            "....W....",
    };

    static final String[] SPARKLE_MID = {
            "...W...",
            "...W...",
            "..MWM..",
            "WWWWWWW",
            "..MWM..",
            "...W...",
            "...W...",
    };

    static final String[] SPARKLE_SMALL = {
            "..W..",
            ".MWM.",
            "WWWWW",
            ".MWM.",
            "..W..",
    };

    static final String[] SPARKLE_DOT = {
            ".M.",
            "MWM",
            ".M.",
    };

    static final String[] PETAL_SAKURA = {
            ".M..M..",
            "MWMMWM.",
            "MWWWWWM",
            "MWWWWWM",
            ".MWWWLM",
            ".MWWLM.",
            "..MLLM.",
            "...MM..",
    };

    static final String[] PETAL_ROUND = {
            "..MMM..",
            ".MWWWM.",
            "MWWWWLM",
            "MWWWWLM",
            "MWWWLLM",
            ".MWLLM.",
            "..MMM..",
    };

    static final String[] PETAL_SMALL = {
            ".MM..",
            "MWWM.",
            "MWWLM",
            ".MLLM",
            "..MM.",
    };

    static final String[] PETAL_LONG = {
            "..MM...",
            ".MWWM..",
            ".MWWWM.",
            "MWWWWM.",
            "MWWWWLM",
            ".MWWLLM",
            "..MLLM.",
            "...MM..",
    };

    static final String[] FLOWER_FIVE = {
            "...MWM...",
            "..MWWWM..",
            ".MMWWWMM.",
            "MWWMSMWWM",
            "WWWSDSWWW",
            "MWWMSMWWM",
            ".MWWMWWM.",
            ".MWM.MWM.",
            "..M...M..",
    };

    static final String[] FLOWER_FOUR = {
            "..MWM..",
            "..WWW..",
            "MWMSMWM",
            "WWSDSWW",
            "MWMSMWM",
            "..WWW..",
            "..MWM..",
    };

    static final String[] FLOWER_TINY = {
            ".W.W.",
            "WWSWW",
            ".SDS.",
            "WWSWW",
            ".W.W.",
    };

    static final String[] BOW = {
            ".OOO.......OOO.",
            "OWWWOO...OOWWWO",
            "OWWWWWO.OWWWWWO",
            "OWWWWWWOWWWWWWO",
            "OWWWWSOKOSWWWWO",
            "OWWWSSOKOSSWWWO",
            "OWSSO.OOO.OSSWO",
            ".OO..OWSWO..OO.",
            "....OWSOSWO....",
            "....OSO.OSO....",
            ".....O...O.....",
    };

    static final String[] MINI_HEART = {
            ".OO.OO.",
            "OWWOWWO",
            "OWWWWSO",
            ".OWWSO.",
            "..OSO..",
            "...O...",
    };

    // ------------------------------------------------------------------ tinted sprites

    static Img tinted(String[] rows) {
        Img img = new Img(rows[0].length(), rows.length);
        img.stamp(rows, 0, 0, pal('W', W, 'L', L, 'M', M, 'S', S, 'D', D, 'O', rgb(0x9A9A9A), 'K', rgb(0xC8C8C8)));
        return img;
    }

    static Img bokeh(int size) {
        Img img = new Img(size, size);
        double c = size / 2.0;
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++) {
                double d = Math.hypot(x + 0.5 - c, y + 0.5 - c) / c;
                if (d >= 1) continue;
                double a = Math.pow(1 - d, 1.6);
                // a soft bright rim makes the circles read as out-of-focus light
                a = Math.min(1, a * 0.85 + Math.max(0, 1 - Math.abs(d - 0.82) * 9) * 0.25);
                img.set(x, y, argb((int) (a * 255), 0xFFFFFF));
            }
        return img;
    }

    static Img cloud(long seed) {
        Img img = new Img(48, 20);
        java.util.Random r = new java.util.Random(seed);
        double[][] puffs = {{9, 13, 6}, {17, 9, 7.5}, {27, 8, 8}, {36, 11, 6.5}, {42, 14, 4.5}, {23, 14, 6}};
        for (double[] pf : puffs) img.disc(pf[0] + r.nextDouble() - 0.5, pf[1], pf[2], W);
        img.fill(5, 14, 46, 19, W);
        // round the flat bottom corners
        for (int y = 14; y < 20; y++) {
            for (int x = 0; x < 48; x++) {
                if (x < 7 - (19 - y) / 2 || x > 44 + (19 - y) / 3) img.set(x, y, 0);
            }
        }
        for (int x = 0; x < 48; x++) img.set(x, 19, 0);
        // soft shading on the underside
        for (int y = 0; y < img.h; y++)
            for (int x = 0; x < img.w; x++) {
                if ((img.get(x, y) >>> 24) == 0) continue;
                boolean bottom = (img.get(x, y + 1) >>> 24) == 0 || (img.get(x, y + 2) >>> 24) == 0;
                if (bottom) img.set(x, y, M);
                else if (y > 13) img.set(x, y, L);
            }
        return img;
    }

    static Img logoBase(boolean overlay) {
        // Bubble-letter "Pinkified". Each glyph uses rows 0..8, baseline at row 8.
        String[][] glyphs = {
                {"#####.", "##..##", "##..##", "##..##", "#####.", "##....", "##....", "##....", "##...."},
                {"..", "..", "..", "##", "##", "##", "##", "##", "##"},
                {"......", "......", "......", "#####.", "##..##", "##..##", "##..##", "##..##", "##..##"},
                {"##....", "##....", "##....", "##..##", "##.##.", "####..", "####..", "##.##.", "##..##"},
                {"..", "..", "..", "##", "##", "##", "##", "##", "##"},
                {"..###", ".##..", ".##..", "#####", ".##..", ".##..", ".##..", ".##..", ".##.."},
                {"..", "..", "..", "##", "##", "##", "##", "##", "##"},
                {"......", "......", "......", ".####.", "##..##", "######", "##....", "##...#", ".####."},
                {"....##", "....##", "....##", ".#####", "##..##", "##..##", "##..##", "##..##", ".#####"},
        };
        boolean[] dotted = {false, true, false, false, true, false, true, false, false};
        int k = 2, top = 7, side = 3;
        int textW = 0;
        for (String[] g : glyphs) textW += g[0].length() + 1;
        textW -= 1;
        Img img = new Img(textW * k + side * 2, 9 * k + top + 4);
        Img mask = new Img(img.w, img.h);
        int cx = side;
        int[] dotX = new int[glyphs.length];
        for (int gi = 0; gi < glyphs.length; gi++) {
            String[] g = glyphs[gi];
            for (int y = 0; y < 9; y++)
                for (int x = 0; x < g[y].length(); x++)
                    if (g[y].charAt(x) == '#') mask.fill(cx + x * k, top + y * k, cx + x * k + k, top + y * k + k, W);
            dotX[gi] = cx + g[0].length() * k / 2;
            cx += (g[0].length() + 1) * k;
        }
        // soften the outer corners of each letter so they read as bubbly
        int[] copy = mask.p.clone();
        for (int y = 0; y < mask.h; y++)
            for (int x = 0; x < mask.w; x++) {
                if ((copy[y * mask.w + x] >>> 24) == 0) continue;
                boolean up = (y > 0 && (copy[(y - 1) * mask.w + x] >>> 24) != 0);
                boolean dn = (y < mask.h - 1 && (copy[(y + 1) * mask.w + x] >>> 24) != 0);
                boolean lf = (x > 0 && (copy[y * mask.w + x - 1] >>> 24) != 0);
                boolean rt = (x < mask.w - 1 && (copy[y * mask.w + x + 1] >>> 24) != 0);
                int n = (up ? 1 : 0) + (dn ? 1 : 0) + (lf ? 1 : 0) + (rt ? 1 : 0);
                if (n == 2 && ((up && lf) || (up && rt) || (dn && lf) || (dn && rt)) && !(up && dn) && !(lf && rt))
                    mask.set(x, y, 0);
            }
        if (!overlay) {
            // body: vertical gradient, darker toward the baseline
            for (int y = 0; y < img.h; y++)
                for (int x = 0; x < img.w; x++)
                    if ((mask.get(x, y) >>> 24) != 0) {
                        double t = (y - top) / (double) (9 * k);
                        img.set(x, y, lerp(rgb(0xFFFFFF), rgb(0xC9C9C9), t));
                    }
            // drop shadow + outline
            Img shadow = new Img(img.w, img.h);
            for (int y = 0; y < img.h; y++)
                for (int x = 0; x < img.w; x++)
                    if ((mask.get(x, y - 2) >>> 24) != 0 || (mask.get(x, y - 1) >>> 24) != 0)
                        shadow.set(x, y, rgb(0x5E5E5E));
            img.outline(rgb(0x6E6E6E), true);
            Img merged = new Img(img.w, img.h);
            merged.draw(shadow, 0, 0);
            merged.outline(rgb(0x5E5E5E), true);
            merged.draw(img, 0, 0);
            for (int i = 0; i < img.p.length; i++) img.p[i] = merged.p[i];
        } else {
            // white gloss on the upper part of each stroke + heart tittles
            for (int y = 0; y < img.h; y++)
                for (int x = 0; x < img.w; x++)
                    if ((mask.get(x, y) >>> 24) != 0 && (mask.get(x, y - 1) >>> 24) == 0 && (mask.get(x, y + 2) >>> 24) != 0)
                        img.set(x, y + 1, argb(200, 0xFFFFFF));
            String[] heart = {".##..##.", "########", "########", ".######.", "..####..", "...##..."};
            for (int gi = 0; gi < glyphs.length; gi++) {
                if (!dotted[gi]) continue;
                Img h = new Img(10, 8);
                h.stamp(heart, 1, 1, pal('#', rgb(0xFF7FAA)));
                h.set(3, 2, rgb(0xFFFFFF));
                h.outline(rgb(0x7A2E52), true);
                img.draw(h, dotX[gi] - 5, 0);
            }
        }
        return img;
    }

    // ------------------------------------------------------------------ coloured icons

    static Img strawberryIcon() {
        Img img = new Img(16, 16);
        img.stamp(new String[]{
                "................",
                "................",
                "......g..g......",
                ".....gGGGGg.....",
                "....RRGgGGRR....",
                "...RhRRRRRRRR...",
                "...RhRsRRRsRR...",
                "...RRRRRRRRRR...",
                "...RRsRRRsRRr...",
                "....RRRRRRRRr...",
                "....RRsRRsRr....",
                ".....RRRRRrr....",
                "......RRsrr.....",
                ".......Rrr......",
                "................",
                "................",
        }, 0, 0, pal('R', rgb(0xF25E83), 'r', rgb(0xD14468), 'h', rgb(0xFFB3C6), 's', rgb(0xFFF0B8),
                'G', rgb(0x7FD17D), 'g', rgb(0x55A85C)));
        img.outline(rgb(0x6B2440));
        return img;
    }

    static Img lavenderIcon() {
        Img img = new Img(16, 16);
        img.line(4, 14, 9, 6, rgb(0x6DB46F));
        img.line(5, 14, 10, 6, rgb(0x58995C));
        img.ellipse(5.5, 11.5, 2.2, 1.0, -0.9, rgb(0x7FCB7E));
        int[][] buds = {{10, 2}, {12, 3}, {9, 4}, {11, 5}, {8, 6}, {10, 7}, {7, 8}};
        for (int[] b : buds) {
            img.disc(b[0] + 0.5, b[1] + 0.5, 1.3, rgb(0xB18AE6));
            img.set(b[0], b[1], rgb(0xD9C4FA));
        }
        img.outline(rgb(0x4A2E6E));
        return img;
    }

    static Img bowIcon(int main, int shade, int knot, int outline) {
        Img img = new Img(17, 13);
        img.stamp(BOW, 1, 1, pal('W', main, 'S', shade, 'K', knot, 'O', outline));
        return img.padded(17, 16);
    }

    static Img sakuraIcon() {
        Img img = new Img(16, 16);
        for (int i = 0; i < 5; i++) {
            double a = -Math.PI / 2 + i * Math.PI * 2 / 5;
            img.ellipse(8 + Math.cos(a) * 3.6, 8 + Math.sin(a) * 3.6, 3.2, 2.4, a, rgb(0xFBC4D6));
        }
        for (int i = 0; i < 5; i++) {
            double a = -Math.PI / 2 + i * Math.PI * 2 / 5;
            img.set((int) Math.round(7.5 + Math.cos(a) * 6.2), (int) Math.round(7.5 + Math.sin(a) * 6.2), 0);
            img.ellipse(8 + Math.cos(a) * 3.0, 8 + Math.sin(a) * 3.0, 1.4, 0.8, a, rgb(0xFFE3EC));
        }
        img.disc(8, 8, 1.9, rgb(0xF27BA3));
        img.set(7, 7, rgb(0xFFE07A));
        img.set(8, 8, rgb(0xFFE07A));
        img.outline(rgb(0x8E3E5E));
        return img;
    }

    static Img angelIcon() {
        Img img = new Img(16, 16);
        // halo
        img.ellipse(8, 3, 4.5, 1.7, 0, rgb(0xFFE08A));
        img.ellipse(8, 3, 2.6, 0.7, 0, 0);
        // wings
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 4; i++) {
                img.ellipse(8 + side * (2.5 + i * 1.2), 9 + i * 0.6, 2.2 - i * 0.25, 3.4 - i * 0.5, side * 0.5, rgb(0xFFFFFF));
            }
            img.ellipse(8 + side * 3.2, 10.5, 1.4, 2.0, side * 0.5, rgb(0xE8EEF8));
        }
        img.disc(8, 10, 1.6, rgb(0xFFD3E2));
        img.outline(rgb(0x8C7A55));
        return img;
    }

    static Img crownIcon() {
        Img img = new Img(16, 16);
        img.stamp(new String[]{
                "................",
                "................",
                ".......p........",
                "..y....Y....y...",
                "..YY..YYY..YY...",
                "..YYY.YYY.YYY...",
                "..YYYYYYYYYYY...",
                "..YYYYYYYYYYY...",
                "..YYbYYgYYbYY...",
                "..YYYYYYYYYYY...",
                "..ooooooooooo...",
                "................",
        }, 0, 2, pal('Y', rgb(0xFFD66E), 'y', rgb(0xFFF1B8), 'o', rgb(0xE8B14A), 'p', rgb(0xF7A8D0),
                'b', rgb(0x9FD4F5), 'g', rgb(0xF48BB8)));
        img.outline(rgb(0x7A5A2A));
        return img;
    }

    static Img paletteIcon() {
        Img img = new Img(16, 16);
        img.ellipse(8, 8.5, 6.5, 5.5, -0.2, rgb(0xFFF3E6));
        img.disc(10.5, 10.5, 1.4, 0);
        int[] dots = {0xF47BA0, 0xB18AE6, 0x8FD3F0, 0x9EDC93, 0xFFD66E};
        int[][] pos = {{4, 7}, {6, 4}, {9, 4}, {12, 6}, {5, 10}};
        for (int i = 0; i < dots.length; i++) img.disc(pos[i][0] + 0.5, pos[i][1] + 0.5, 1.2, rgb(dots[i]));
        img.outline(rgb(0x7A5060));
        return img;
    }

    static Img heartIcon(int main, int shade, int outline) {
        Img img = new Img(16, 16);
        img.stamp(new String[]{
                "..WWW...WWW..",
                ".WhhWW.WWWWW.",
                "WhhWWWWWWWWWS",
                "WhWWWWWWWWWWS",
                "WWWWWWWWWWWSS",
                ".WWWWWWWWWSS.",
                "..WWWWWWWSS..",
                "...WWWWWSS...",
                "....WWWSS....",
                ".....WSS.....",
                "......S......",
        }, 1, 2, pal('W', main, 'S', shade, 'h', rgb(0xFFFFFF)));
        img.outline(outline);
        return img;
    }

    static Img windowIcon() {
        Img img = new Img(16, 16);
        img.fill(2, 3, 14, 13, rgb(0xFFF3F7));
        img.fill(2, 3, 14, 5, rgb(0xF48FB1));
        img.set(12, 4, rgb(0xFFFFFF));
        img.set(10, 4, rgb(0xFFFFFF));
        img.fill(4, 7, 8, 11, rgb(0xF9C6D8));
        img.fill(9, 7, 12, 8, rgb(0xD6B8F2));
        img.fill(9, 9, 12, 10, rgb(0xD6B8F2));
        img.set(2, 3, 0);
        img.set(13, 3, 0);
        img.set(2, 12, 0);
        img.set(13, 12, 0);
        img.outline(rgb(0x7A2E52));
        return img;
    }

    static Img sparklesIcon() {
        Img img = new Img(16, 16);
        Img big = tintedColor(SPARKLE_BIG, rgb(0xFFE08A), rgb(0xFFF6D0));
        Img small = tintedColor(SPARKLE_SMALL, rgb(0xF7A8D0), rgb(0xFFE6F1));
        big.outline(rgb(0x8C6A2A));
        small.outline(rgb(0x8E3E5E));
        img.draw(big, 1, 1);
        img.draw(small, 9, 8);
        return img;
    }

    static Img tintedColor(String[] rows, int main, int light) {
        Img img = new Img(rows[0].length() + 2, rows.length + 2);
        img.stamp(rows, 1, 1, pal('W', main, 'M', light));
        return img;
    }

    static Img motionIcon() {
        Img img = new Img(16, 16);
        img.stamp(new String[]{
                "......Y......",
                ".....YYY.....",
                "YYYYYYhYYYYYY",
                ".YYYYhYYYYYY.",
                "..YYYYYYYYY..",
                "...YYYYYYY...",
                "..YYYY.YYYY..",
                "..YYY...YYY..",
                ".YY.......YY.",
        }, 3, 4, pal('Y', rgb(0xF7A8D0), 'h', rgb(0xFFFFFF)));
        img.line(0, 6, 2, 6, rgb(0xD6B8F2));
        img.line(0, 9, 3, 9, rgb(0xD6B8F2));
        img.line(1, 12, 3, 12, rgb(0xD6B8F2));
        img.outline(rgb(0x7A2E52));
        return img;
    }

    static Img noteIcon() {
        Img img = new Img(16, 16);
        img.fill(6, 3, 8, 12, rgb(0xC9A6F0));
        img.fill(8, 3, 12, 5, rgb(0xC9A6F0));
        img.fill(10, 5, 12, 7, rgb(0xC9A6F0));
        img.ellipse(5, 12, 2.6, 2.0, -0.4, rgb(0xF48FB1));
        img.set(4, 11, rgb(0xFFFFFF));
        img.outline(rgb(0x5A3A7A));
        return img;
    }

    static Img globeIcon() {
        Img img = new Img(16, 16);
        img.disc(8, 8, 6.2, rgb(0xA9D8F5));
        // continents
        img.ellipse(6, 6, 2.6, 1.8, 0.4, rgb(0x9EDC93));
        img.ellipse(10.5, 10.5, 2.2, 1.6, -0.5, rgb(0x9EDC93));
        img.ellipse(11, 5, 1.2, 0.8, 0, rgb(0x9EDC93));
        img.set(5, 4, rgb(0xFFFFFF));
        img.set(4, 5, rgb(0xFFFFFF));
        img.outline(rgb(0x3E5A7A));
        return img;
    }

    static Img accessibilityIcon() {
        Img img = new Img(16, 16);
        img.disc(8, 8, 6.4, rgb(0xFFF3F7));
        img.disc(8, 4.6, 1.3, rgb(0xE0608E));
        img.fill(4, 6, 12, 7, rgb(0xE0608E));
        img.fill(7, 7, 9, 10, rgb(0xE0608E));
        img.line(7, 10, 5, 12, rgb(0xE0608E));
        img.line(8, 10, 10, 12, rgb(0xE0608E));
        img.outline(rgb(0x7A2E52));
        return img;
    }

    // ------------------------------------------------------------------ mod list banner

    static final Map<Character, String[]> SMALL_FONT = new HashMap<>();

    static {
        SMALL_FONT.put('S', new String[]{"###", "#..", "###", "..#", "###"});
        SMALL_FONT.put('U', new String[]{"#.#", "#.#", "#.#", "#.#", "###"});
        SMALL_FONT.put('G', new String[]{"###", "#..", "#.#", "#.#", "###"});
        SMALL_FONT.put('A', new String[]{"###", "#.#", "###", "#.#", "#.#"});
        SMALL_FONT.put('R', new String[]{"##.", "#.#", "##.", "#.#", "#.#"});
        SMALL_FONT.put('P', new String[]{"##.", "#.#", "##.", "#..", "#.."});
        SMALL_FONT.put('L', new String[]{"#..", "#..", "#..", "#..", "###"});
        SMALL_FONT.put('M', new String[]{"#...#", "##.##", "#.#.#", "#...#", "#...#"});
        SMALL_FONT.put('T', new String[]{"###", ".#.", ".#.", ".#.", ".#."});
        SMALL_FONT.put('D', new String[]{"##.", "#.#", "#.#", "#.#", "##."});
        SMALL_FONT.put('I', new String[]{"###", ".#.", ".#.", ".#.", "###"});
        SMALL_FONT.put('O', new String[]{"###", "#.#", "#.#", "#.#", "###"});
        SMALL_FONT.put(' ', new String[]{"..", "..", "..", "..", ".."});
    }

    static int smallTextWidth(String s) {
        int w = 0;
        for (char c : s.toCharArray()) w += SMALL_FONT.get(c)[0].length() + 1;
        return w - 1;
    }

    static void smallText(Img img, String s, int x, int y, int c) {
        for (char ch : s.toCharArray()) {
            String[] g = SMALL_FONT.get(ch);
            for (int gy = 0; gy < g.length; gy++)
                for (int gx = 0; gx < g[gy].length(); gx++)
                    if (g[gy].charAt(gx) == '#') img.set(x + gx, y + gy, c);
            x += g[0].length() + 1;
        }
    }

    static Img banner() {
        int w = 192, h = 64;
        Img img = new Img(w, h);
        int r = 6;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int dx = Math.max(0, Math.max(r - x, x - (w - 1 - r)));
                int dy = Math.max(0, Math.max(r - y, y - (h - 1 - r)));
                if (dx * dx + dy * dy > r * r) continue;
                double t = (x / (double) w) * 0.6 + (y / (double) h) * 0.4;
                int c = lerp(rgb(0xFAC6DA), rgb(0xD9C2F4), t);
                // subtle gingham
                if (((x / 8) + (y / 8)) % 2 == 0) c = lerp(c, rgb(0xFFFFFF), 0.12);
                img.set(x, y, c);
            }
        Img frame = new Img(w, h);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if ((img.get(x, y) >>> 24) != 0) {
                    boolean edge = (img.get(x - 1, y) >>> 24) == 0 || (img.get(x + 1, y) >>> 24) == 0
                            || (img.get(x, y - 1) >>> 24) == 0 || (img.get(x, y + 1) >>> 24) == 0;
                    if (edge) frame.set(x, y, rgb(0xB0507E));
                }
        img.draw(frame, 0, 0);

        Img word = logoBase(false);
        Img gloss = logoBase(true);
        // tint the grayscale word pink for the static banner
        for (int i = 0; i < word.p.length; i++) {
            int c = word.p[i];
            if ((c >>> 24) == 0) continue;
            double v = (c & 255) / 255.0;
            int tint = v > 0.6 ? lerp(rgb(0xC0446F), rgb(0xFF8DB4), (v - 0.6) / 0.4) : lerp(rgb(0x4A1A30), rgb(0xC0446F), v / 0.6);
            word.p[i] = (c & 0xFF000000) | (tint & 0xFFFFFF);
        }
        int wx = (w - word.w) / 2, wy = 9;
        img.draw(word, wx, wy);
        img.draw(gloss, wx, wy);

        String sub = "SUGARPLUM STUDIOS";
        int sw = smallTextWidth(sub);
        int sx = (w - sw) / 2, sy = 48;
        Img subImg = new Img(w, h);
        smallText(subImg, sub, sx, sy, rgb(0xFFFFFF));
        subImg.outline(rgb(0x9A4A78), true);
        img.draw(subImg, 0, 0);

        Img heart = new Img(9, 8);
        heart.stamp(MINI_HEART, 1, 1, pal('W', rgb(0xFF8DB4), 'S', rgb(0xE0608E), 'O', rgb(0x7A2E52)));
        img.draw(heart, sx - 13, sy - 1);
        img.draw(heart, sx + sw + 4, sy - 1);

        Img sp = tintedColor(SPARKLE_MID, rgb(0xFFFFFF), rgb(0xFFE6F1));
        Img sps = tintedColor(SPARKLE_SMALL, rgb(0xFFFFFF), rgb(0xFFE6F1));
        img.draw(sp, 12, 10);
        img.draw(sps, 26, 36);
        img.draw(sp, w - 22, 30);
        img.draw(sps, w - 34, 8);
        img.draw(sps, 165, 46);

        Img bow = bowIcon(rgb(0xFF9EC2), rgb(0xE0709A), rgb(0xFFC9DC), rgb(0x7A2E52));
        img.draw(bow, 6, 40);
        return img.scaled(2);
    }

    // ------------------------------------------------------------------ main

    public static void main(String[] args) throws IOException {
        root = new File(args.length > 0 ? args[0] : "1.20.1/common/src/main/resources");
        String tex = "assets/pinkified/textures/";
        System.out.println("Pinkified asset generator -> " + root.getAbsolutePath());

        // menu decorations (tinted at runtime)
        save(tinted(PETAL_SAKURA), tex + "gui/menu/petal_0.png");
        save(tinted(PETAL_ROUND), tex + "gui/menu/petal_1.png");
        save(tinted(PETAL_SMALL), tex + "gui/menu/petal_2.png");
        save(tinted(PETAL_LONG), tex + "gui/menu/petal_3.png");
        save(tinted(SPARKLE_BIG), tex + "gui/menu/sparkle_big.png");
        save(tinted(SPARKLE_SMALL), tex + "gui/menu/sparkle_small.png");
        save(tinted(HEART), tex + "gui/menu/heart.png");
        save(tinted(BOW), tex + "gui/menu/bow.png");
        save(bokeh(32), tex + "gui/menu/bokeh.png");
        save(cloud(7), tex + "gui/menu/cloud_0.png");
        save(cloud(42), tex + "gui/menu/cloud_1.png");
        save(logoBase(false), tex + "gui/logo/pinkified_base.png");
        save(logoBase(true), tex + "gui/logo/pinkified_gloss.png");

        // particles (tinted per theme through the particle colour)
        save(tinted(HEART).padded(9, 9), tex + "particle/heart.png");
        save(tinted(MINI_HEART).padded(7, 7), tex + "particle/heart_small.png");
        save(tinted(SPARKLE_DOT).padded(9, 9), tex + "particle/sparkle_0.png");
        save(tinted(SPARKLE_SMALL).padded(9, 9), tex + "particle/sparkle_1.png");
        save(tinted(SPARKLE_MID).padded(9, 9), tex + "particle/sparkle_2.png");
        save(tinted(SPARKLE_BIG), tex + "particle/sparkle_3.png");
        save(tinted(PETAL_SAKURA).padded(8, 8), tex + "particle/petal_0.png");
        save(tinted(PETAL_ROUND).padded(8, 8), tex + "particle/petal_1.png");
        save(tinted(PETAL_SMALL).padded(8, 8), tex + "particle/petal_2.png");
        save(tinted(PETAL_LONG).padded(8, 8), tex + "particle/petal_3.png");
        save(tinted(FLOWER_FIVE), tex + "particle/flower_0.png");
        save(tinted(FLOWER_FOUR).padded(9, 9), tex + "particle/flower_1.png");
        save(tinted(FLOWER_TINY).padded(9, 9), tex + "particle/flower_2.png");

        // theme icons (full colour)
        save(strawberryIcon(), tex + "gui/icons/theme_strawberry_milk.png");
        save(lavenderIcon(), tex + "gui/icons/theme_lavender_dreams.png");
        save(bowIcon(rgb(0xFF9EC2), rgb(0xE0709A), rgb(0xFFC9DC), rgb(0x7A2E52)), tex + "gui/icons/theme_pink_coquette.png");
        save(sakuraIcon(), tex + "gui/icons/theme_cherry_blossom.png");
        save(angelIcon(), tex + "gui/icons/theme_angelcore.png");
        save(crownIcon(), tex + "gui/icons/theme_pastel_princess.png");
        save(paletteIcon(), tex + "gui/icons/theme_custom.png");

        // settings tab icons
        save(paletteIcon(), tex + "gui/icons/tab_theme.png");
        save(windowIcon(), tex + "gui/icons/tab_interface.png");
        save(heartIcon(rgb(0xFF8DB4), rgb(0xE0608E), rgb(0x7A2E52)), tex + "gui/icons/tab_hud.png");
        save(sparklesIcon(), tex + "gui/icons/tab_effects.png");
        save(motionIcon(), tex + "gui/icons/tab_motion.png");
        save(noteIcon(), tex + "gui/icons/tab_audio.png");
        save(heartIcon(rgb(0xFFFFFF), rgb(0xE6D6E0), rgb(0x7A2E52)), tex + "gui/icons/heart_button.png");
        save(globeIcon(), tex + "gui/icons/language.png");
        save(accessibilityIcon(), tex + "gui/icons/accessibility.png");

        // mod list logo (root of the jar, referenced by mods.toml)
        save(banner(), "pinkified_logo.png");
        System.out.println("Done.");
    }
}
