package com.sugarplumstudios.pinkified.client.texture;

import com.sugarplumstudios.pinkified.client.theme.FoodStyle;
import com.sugarplumstudios.pinkified.client.theme.Palette;

import java.util.HashMap;
import java.util.Map;

/**
 * Decides how each vanilla GUI atlas sprite (Minecraft 1.20.2 and later split the old GUI sheets into sprites) is
 * themed. HUD sprites are cut out of a "virtual icons.png" painted by {@link HudSprites} in the old sheet's layout,
 * so the pixel art is the same as on 1.20.1. Pure Java (no Minecraft classes).
 */
public final class SpriteArt {
    private SpriteArt() {
    }

    /** A cell of the old icons.png/widgets.png layout, in template pixels. */
    private record Cell(int x, int y, int w, int h, Kind kind) {
    }

    private enum Kind {HEARTS, FOOD, ARMOR, XP, CROSSHAIR, SELECTION}

    private static final Map<String, Cell> CELLS = new HashMap<>();

    private static void cell(String path, int x, int y, Kind kind) {
        CELLS.put(path, new Cell(x, y, 9, 9, kind));
    }

    private static void hearts(String type, int x, boolean blink) {
        String h = "hud/heart/";
        cell(h + type + "_full", x, 0, Kind.HEARTS);
        cell(h + type + "_half", x + 9, 0, Kind.HEARTS);
        cell(h + type + "_hardcore_full", x, 45, Kind.HEARTS);
        cell(h + type + "_hardcore_half", x + 9, 45, Kind.HEARTS);
        int bx = blink ? x + 18 : x;
        cell(h + type + "_full_blinking", bx, 0, Kind.HEARTS);
        cell(h + type + "_half_blinking", bx + 9, 0, Kind.HEARTS);
        cell(h + type + "_hardcore_full_blinking", bx, 45, Kind.HEARTS);
        cell(h + type + "_hardcore_half_blinking", bx + 9, 45, Kind.HEARTS);
    }

    static {
        String h = "hud/heart/";
        cell(h + "container", 16, 0, Kind.HEARTS);
        cell(h + "container_blinking", 25, 0, Kind.HEARTS);
        cell(h + "container_hardcore", 16, 45, Kind.HEARTS);
        cell(h + "container_hardcore_blinking", 25, 45, Kind.HEARTS);
        cell(h + "full", 52, 0, Kind.HEARTS);
        cell(h + "half", 61, 0, Kind.HEARTS);
        cell(h + "full_blinking", 70, 0, Kind.HEARTS);
        cell(h + "half_blinking", 79, 0, Kind.HEARTS);
        cell(h + "hardcore_full", 52, 45, Kind.HEARTS);
        cell(h + "hardcore_half", 61, 45, Kind.HEARTS);
        cell(h + "hardcore_full_blinking", 70, 45, Kind.HEARTS);
        cell(h + "hardcore_half_blinking", 79, 45, Kind.HEARTS);
        hearts("poisoned", 88, true);
        hearts("withered", 124, true);
        hearts("absorbing", 160, false);
        hearts("frozen", 178, false);
        cell(h + "vehicle_container", 52, 9, Kind.HEARTS);
        cell(h + "vehicle_full", 88, 9, Kind.HEARTS);
        cell(h + "vehicle_half", 97, 9, Kind.HEARTS);
        cell("hud/air", 16, 18, Kind.HEARTS);
        cell("hud/air_bursting", 25, 18, Kind.HEARTS);
        cell("hud/armor_empty", 16, 9, Kind.ARMOR);
        cell("hud/armor_half", 25, 9, Kind.ARMOR);
        cell("hud/armor_full", 34, 9, Kind.ARMOR);
        cell("hud/food_empty", 16, 27, Kind.FOOD);
        cell("hud/food_empty_hunger", 133, 27, Kind.FOOD);
        cell("hud/food_full", 52, 27, Kind.FOOD);
        cell("hud/food_half", 61, 27, Kind.FOOD);
        cell("hud/food_full_hunger", 88, 27, Kind.FOOD);
        cell("hud/food_half_hunger", 97, 27, Kind.FOOD);
        CELLS.put("hud/experience_bar_background", new Cell(0, 64, 182, 5, Kind.XP));
        CELLS.put("hud/experience_bar_progress", new Cell(0, 69, 182, 5, Kind.XP));
        CELLS.put("hud/jump_bar_background", new Cell(0, 84, 182, 5, Kind.XP));
        CELLS.put("hud/jump_bar_progress", new Cell(0, 89, 182, 5, Kind.XP));
        CELLS.put("hud/crosshair", new Cell(0, 0, 15, 15, Kind.CROSSHAIR));
        CELLS.put("hud/hotbar_selection", new Cell(0, 22, 24, 24, Kind.SELECTION));
    }

    private static final String[] WIDGET_PREFIXES = {"widget/", "toast/", "spectator/", "social_interactions/", "server_list/",
            "world_list/", "transferable_list/", "icon/", "popup/", "pending_invite/", "notification/"};
    private static final String[] CONTAINER_PREFIXES = {"container/", "recipe_book/", "advancements/"};

    /** True for every sprite path this class may change (the others are never read). */
    public static boolean handles(String path) {
        if (CELLS.containsKey(path) || path.startsWith("hud/") || path.startsWith("mob_effect/")) return true;
        for (String prefix : WIDGET_PREFIXES) if (path.startsWith(prefix)) return true;
        for (String prefix : CONTAINER_PREFIXES) if (path.startsWith(prefix)) return true;
        return false;
    }

    /** Paints the virtual icons/widgets sheets once per scale during one apply pass. */
    public static final class Session {
        private final Palette p;
        private final ThemedArt.Options o;
        private final Map<Integer, Canvas> sheets = new HashMap<>();

        public Session(Palette p, ThemedArt.Options o) {
            this.p = p;
            this.o = o;
        }

        private Canvas sheet(int scale) {
            return sheets.computeIfAbsent(scale, s -> {
                Canvas c = new Canvas(256 * s, 256 * s);
                c.scale = s;
                if (o.hearts()) {
                    HudSprites.paintHearts(c, p);
                    HudSprites.paintAir(c, p);
                }
                if (o.food() && o.foodStyle() != FoodStyle.VANILLA) HudSprites.paintFood(c, p, o.foodStyle());
                if (o.armor()) HudSprites.paintArmor(c, p);
                if (o.xp()) {
                    HudSprites.paintXp(c, p);
                    HudSprites.paintJump(c, p);
                }
                if (o.hotbar()) HudSprites.paintSelection(c, p);
                return c;
            });
        }

        /** The themed sprite, or null to keep the original pixels. */
        public Canvas process(String path, Canvas src) {
            Cell cell = CELLS.get(path);
            if (cell != null) return cut(cell, src);
            if (path.equals("hud/hotbar") || path.startsWith("hud/hotbar_offhand")) {
                if (!o.hotbar()) return null;
                Canvas out = src.copy();
                Recolor.ramp(out, p, true);
                if (path.equals("hud/hotbar") && src.width % 182 == 0 && src.height == src.width / 182 * 22) {
                    out.scale = src.width / 182;
                    Recolor.roundCorners(out, 0, 0, 182, 22);
                }
                return out;
            }
            if (path.contains("effect_background")) {
                if (!o.effectFrames()) return null;
                Canvas out = src.copy();
                Recolor.ramp(out, p, true);
                return out;
            }
            if (path.startsWith("mob_effect/")) {
                if (!o.effectFrames()) return null;
                Canvas out = src.copy();
                Recolor.effectIcon(out, p);
                return out;
            }
            if (path.startsWith("hud/")) return null;
            for (String prefix : CONTAINER_PREFIXES) {
                if (path.startsWith(prefix)) {
                    if (!o.containers()) return null;
                    Canvas out = src.copy();
                    Recolor.ramp(out, p, true);
                    return out;
                }
            }
            if (!o.widgets()) return null;
            Canvas out = src.copy();
            boolean highlighted = path.contains("highlighted") || path.endsWith("_hover");
            Recolor.ramp(out, p, true, 0, 0, out.width, out.height, highlighted);
            if (path.startsWith("widget/button") && src.width % 200 == 0 && src.height == src.width / 200 * 20) {
                out.scale = src.width / 200;
                Recolor.roundCorners(out, 0, 0, 200, 20);
            }
            return out;
        }

        private Canvas cut(Cell cell, Canvas src) {
            boolean on = switch (cell.kind()) {
                case HEARTS -> o.hearts();
                case FOOD -> o.food() && o.foodStyle() != FoodStyle.VANILLA;
                case ARMOR -> o.armor();
                case XP -> o.xp();
                case CROSSHAIR -> o.customCrosshair();
                case SELECTION -> o.hotbar();
            };
            if (!on) return null;
            int s = src.width / cell.w();
            // HD packs: only whole multiples of the vanilla size get the painted art
            if (s < 1 || src.width != s * cell.w() || src.height > s * cell.h() || src.height < s * (cell.h() - 1)) return null;
            if (cell.kind() == Kind.CROSSHAIR) return new Canvas(src.width, src.height); // our overlay draws it
            Canvas sheet = sheet(s);
            Canvas out = new Canvas(src.width, src.height);
            for (int y = 0; y < src.height; y++)
                System.arraycopy(sheet.px, (cell.y() * s + y) * sheet.width + cell.x() * s, out.px, y * out.width, src.width);
            return out;
        }

        /** A status-effect icon from the mob-effect atlas. */
        public Canvas effectIcon(Canvas src) {
            if (!o.effectFrames()) return null;
            Canvas out = src.copy();
            Recolor.effectIcon(out, p);
            return out;
        }
    }
}
