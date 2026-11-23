import com.sugarplumstudios.pinkified.client.texture.Canvas;
import com.sugarplumstudios.pinkified.client.texture.HudSprites;
import com.sugarplumstudios.pinkified.client.texture.ThemedArt;
import com.sugarplumstudios.pinkified.client.theme.CrosshairStyle;
import com.sugarplumstudios.pinkified.client.theme.FoodStyle;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.ThemeId;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Developer tool: renders Pinkified's runtime texture pipeline offline so themes can be
 * reviewed without launching the game.
 *
 *   javac -d build/preview 1.20.1/common/src/main/java/.../util/Colors.java ... tools/preview/ThemePreview.java
 *   java -cp build/preview ThemePreview <extracted vanilla assets/minecraft dir> <out dir>
 *
 * Vanilla textures are read from a local copy of the game's own assets and never committed.
 */
public final class ThemePreview {
    public static void main(String[] args) throws Exception {
        File mc = new File(args[0]);
        File out = new File(args[1]);
        out.mkdirs();
        ThemedArt.Options opts = new ThemedArt.Options(true, true, true, true, true, FoodStyle.AUTO, true, true, true, true, true, true);
        String[] sheet = {"textures/gui/container/inventory.png", "textures/gui/container/furnace.png",
                "textures/gui/container/enchanting_table.png", "textures/gui/container/anvil.png"};
        for (ThemeId id : ThemeId.values()) {
            Palette p = Palette.of(id, 0xFFF48FB1, 0xFFB39DDB);
            // contact sheet: 4 containers + widgets + icons, all at 2x
            int W = 1100, H = 900;
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            fill(img, 0xFF2B2B2B);
            int x = 10, y = 10;
            for (String s : sheet) {
                Canvas c = load(new File(mc, s));
                Canvas t = ThemedArt.process(target(s), c, p, opts);
                draw(img, t, x, y, 176, 166, 0, 0, 2, p.skyTop);
                x += 176 * 2 + 10;
                if (x + 352 > W) {
                    x = 10;
                    y += 166 * 2 + 10;
                }
            }
            Canvas w = ThemedArt.process(target("textures/gui/widgets.png"), load(new File(mc, "textures/gui/widgets.png")), p, opts);
            draw(img, w, 730, 10, 200, 110, 0, 0, 1, p.skyTop);
            draw(img, w, 730, 130, 200, 106, 0, 0, 1, 0xFF1A1A1A);
            Canvas ic = ThemedArt.process(target("textures/gui/icons.png"), load(new File(mc, "textures/gui/icons.png")), p, opts);
            draw(img, ic, 10, 690, 200, 95, 0, 0, 2, 0xFF6A8A5A);
            draw(img, ic, 430, 690, 200, 95, 0, 0, 2, 0xFF1A1A1A);
            Canvas logo = ThemedArt.process(target("textures/gui/title/minecraft.png"), load(new File(mc, "textures/gui/title/minecraft.png")), p, opts);
            draw(img, logo, 730, 250, 256, 44, 0, 0, 1, p.skyTop);
            int cx = 730;
            for (CrosshairStyle cs : CrosshairStyle.values()) {
                if (cs == CrosshairStyle.VANILLA) continue;
                draw(img, HudSprites.crosshair(cs, p), cx, 310, 15, 15, 0, 0, 3, 0xFF7FB0E0);
                cx += 50;
            }
            Canvas bg = ThemedArt.process(target("textures/gui/options_background.png"), load(new File(mc, "textures/gui/options_background.png")), p, opts);
            draw(img, bg, 730, 380, bg.width, bg.height, 0, 0, 4, 0);
            Canvas glint = ThemedArt.process(target("textures/misc/enchanted_glint_item.png"), load(new File(mc, "textures/misc/enchanted_glint_item.png")), p, opts);
            draw(img, glint, 820, 380, 64, 64, 0, 0, 2, 0xFF000000);
            ImageIO.write(img, "png", new File(out, "sheet_" + id.id() + ".png"));
            System.out.println("wrote sheet_" + id.id());
            if (args.length > 2) {
                for (ThemedArt.Target t : ThemedArt.TARGETS) {
                    File f = new File(mc, t.path());
                    if (!f.exists()) continue;
                    Canvas r = ThemedArt.process(t, load(f), p, opts);
                    if (r == null) continue;
                    BufferedImage bi = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB);
                    bi.setRGB(0, 0, r.width, r.height, r.px, 0, r.width);
                    File dst = new File(out, id.id() + "/" + t.path());
                    dst.getParentFile().mkdirs();
                    ImageIO.write(bi, "png", dst);
                }
            }
        }
    }

    static ThemedArt.Target target(String path) {
        for (ThemedArt.Target t : ThemedArt.TARGETS) if (t.path().equals(path)) return t;
        throw new IllegalArgumentException(path);
    }

    static Canvas load(File f) throws Exception {
        BufferedImage b = ImageIO.read(f);
        int[] px = b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0, b.getWidth());
        return new Canvas(b.getWidth(), b.getHeight(), px);
    }

    static void fill(BufferedImage img, int c) {
        for (int y = 0; y < img.getHeight(); y++) for (int x = 0; x < img.getWidth(); x++) img.setRGB(x, y, c);
    }

    static void draw(BufferedImage img, Canvas c, int ox, int oy, int w, int h, int u, int v, int k, int bg) {
        for (int y = 0; y < h * k; y++)
            for (int x = 0; x < w * k; x++) {
                int sx = u + x / k, sy = v + y / k;
                int px = (sx < c.width && sy < c.height) ? c.px[sy * c.width + sx] : 0;
                int a = px >>> 24;
                int base = bg == 0 ? 0xFF000000 : bg;
                float f = a / 255f;
                int r = (int) (((px >> 16) & 255) * f + ((base >> 16) & 255) * (1 - f));
                int g = (int) (((px >> 8) & 255) * f + ((base >> 8) & 255) * (1 - f));
                int b = (int) ((px & 255) * f + (base & 255) * (1 - f));
                int X = ox + x, Y = oy + y;
                if (X < img.getWidth() && Y < img.getHeight()) img.setRGB(X, Y, 0xFF000000 | (r << 16) | (g << 8) | b);
            }
    }
}
