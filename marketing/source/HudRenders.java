import com.sugarplumstudios.pinkified.client.texture.Canvas;
import com.sugarplumstudios.pinkified.client.texture.HudSprites;
import com.sugarplumstudios.pinkified.client.theme.CrosshairStyle;
import com.sugarplumstudios.pinkified.client.theme.FoodStyle;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.ThemeId;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Paints the mod's own HUD pixel art (HudSprites) for every theme, with no Minecraft and no vanilla textures, and
 * writes each sprite as a PNG into marketing/source/renders/. These are the sprites the mod paints into icons.png and
 * widgets.png at runtime. See marketing/README.md for the command.
 */
public final class HudRenders {
    public static void main(String[] args) throws Exception {
        File out = new File(args[0]);
        out.mkdirs();
        for (ThemeId id : ThemeId.values()) {
            Palette p = Palette.of(id, 0xFFF48FB1, 0xFFB39DDB);
            String t = id.id();
            Canvas icons = new Canvas(256, 256);
            HudSprites.paintHearts(icons, p);
            HudSprites.paintArmor(icons, p);
            HudSprites.paintAir(icons, p);
            HudSprites.paintFood(icons, p, FoodStyle.AUTO);
            HudSprites.paintXp(icons, p);
            write(icons, 52, 0, 9, 9, new File(out, t + "_heart.png"));
            write(icons, 61, 0, 9, 9, new File(out, t + "_heart_half.png"));
            write(icons, 16, 0, 9, 9, new File(out, t + "_heart_empty.png"));
            write(icons, 52, 45, 9, 9, new File(out, t + "_heart_hardcore.png"));
            write(icons, 160, 0, 9, 9, new File(out, t + "_heart_absorption.png"));
            write(icons, 88, 0, 9, 9, new File(out, t + "_heart_poison.png"));
            write(icons, 178, 0, 9, 9, new File(out, t + "_heart_frozen.png"));
            write(icons, 34, 9, 9, 9, new File(out, t + "_armor.png"));
            write(icons, 16, 18, 9, 9, new File(out, t + "_bubble.png"));
            write(icons, 52, 27, 9, 9, new File(out, t + "_food.png"));
            write(icons, 0, 69, 182, 5, new File(out, t + "_xp.png"));
            Canvas widgets = new Canvas(256, 256);
            HudSprites.paintSelection(widgets, p);
            write(widgets, 0, 22, 24, 24, new File(out, t + "_selection.png"));
            for (CrosshairStyle cs : CrosshairStyle.values()) {
                if (cs == CrosshairStyle.VANILLA) continue;
                Canvas c = HudSprites.crosshair(cs, p);
                write(c, 0, 0, 15, 15, new File(out, t + "_crosshair_" + cs.name().toLowerCase() + ".png"));
            }
            // every treat in this theme's colours, for the "pick any treat" art
            for (FoodStyle f : FoodStyle.values()) {
                if (f == FoodStyle.AUTO || f == FoodStyle.VANILLA) continue;
                Canvas food = new Canvas(256, 256);
                HudSprites.paintFood(food, p, f);
                write(food, 52, 27, 9, 9, new File(out, t + "_treat_" + f.name().toLowerCase() + ".png"));
            }
        }
        System.out.println("wrote HUD renders to " + out);
    }

    private static void write(Canvas c, int x, int y, int w, int h, File f) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int j = 0; j < h; j++) for (int i = 0; i < w; i++) img.setRGB(i, j, c.get(x + i, y + j));
        ImageIO.write(img, "png", f);
    }
}
