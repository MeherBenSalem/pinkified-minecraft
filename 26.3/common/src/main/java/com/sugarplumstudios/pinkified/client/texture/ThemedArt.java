package com.sugarplumstudios.pinkified.client.texture;

import com.sugarplumstudios.pinkified.client.theme.FoodStyle;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.util.Colors;

import java.util.List;

/**
 * Decides how each themed vanilla texture is produced from its original pixels.
 * Paths are relative to the {@code minecraft} namespace. Pure Java (used by the preview tool).
 */
public final class ThemedArt {
    private ThemedArt() {
    }

    public enum Group {CONTAINERS, WIDGETS, BACKGROUND, LIST_BACKGROUND, HUD_ICONS, HUD_WIDGETS, GLINT}

    public record Target(String path, Group group) {
    }

    /** User toggles that affect texture generation. */
    public record Options(boolean containers, boolean widgets, boolean backgrounds, boolean hearts, boolean food,
                          FoodStyle foodStyle, boolean armor, boolean xp, boolean hotbar, boolean customCrosshair,
                          boolean glint, boolean effectFrames) {
    }

    private static final String C = "textures/gui/container/";

    public static final List<Target> TARGETS = List.of(
            // containers
            new Target(C + "anvil.png", Group.CONTAINERS),
            new Target(C + "beacon.png", Group.CONTAINERS),
            new Target(C + "blast_furnace.png", Group.CONTAINERS),
            new Target(C + "brewing_stand.png", Group.CONTAINERS),
            new Target(C + "cartography_table.png", Group.CONTAINERS),
            new Target(C + "crafting_table.png", Group.CONTAINERS),
            new Target(C + "dispenser.png", Group.CONTAINERS),
            new Target(C + "enchanting_table.png", Group.CONTAINERS),
            new Target(C + "furnace.png", Group.CONTAINERS),
            new Target(C + "generic_54.png", Group.CONTAINERS),
            new Target(C + "grindstone.png", Group.CONTAINERS),
            new Target(C + "hopper.png", Group.CONTAINERS),
            new Target(C + "horse.png", Group.CONTAINERS),
            new Target(C + "inventory.png", Group.CONTAINERS),
            new Target(C + "loom.png", Group.CONTAINERS),
            new Target(C + "shulker_box.png", Group.CONTAINERS),
            new Target(C + "smithing.png", Group.CONTAINERS),
            new Target(C + "smoker.png", Group.CONTAINERS),
            new Target(C + "stonecutter.png", Group.CONTAINERS),
            new Target(C + "villager.png", Group.CONTAINERS),
            new Target(C + "crafter.png", Group.CONTAINERS),
            new Target(C + "creative_inventory/tab_inventory.png", Group.CONTAINERS),
            new Target(C + "creative_inventory/tab_item_search.png", Group.CONTAINERS),
            new Target(C + "creative_inventory/tab_items.png", Group.CONTAINERS),
            new Target("textures/gui/recipe_book.png", Group.CONTAINERS),
            new Target("textures/gui/advancements/window.png", Group.CONTAINERS),
            // widgets
            new Target("textures/gui/header_separator.png", Group.WIDGETS),
            new Target("textures/gui/footer_separator.png", Group.WIDGETS),
            new Target("textures/gui/title/minecraft.png", Group.WIDGETS),
            new Target("textures/gui/title/minceraft.png", Group.WIDGETS),
            new Target("textures/gui/tab_header_background.png", Group.WIDGETS),
            new Target("textures/gui/inworld_header_separator.png", Group.WIDGETS),
            new Target("textures/gui/inworld_footer_separator.png", Group.WIDGETS),
            // list backgrounds: Pinkified draws its own rounded panel behind lists
            new Target("textures/gui/menu_list_background.png", Group.LIST_BACKGROUND),
            new Target("textures/gui/inworld_menu_list_background.png", Group.LIST_BACKGROUND),
            // HUD sprites live in the GUI atlas since 1.20.2 (see SpriteArt)
            // glint
            new Target("textures/misc/enchanted_glint_item.png", Group.GLINT),
            new Target("textures/misc/enchanted_glint_entity.png", Group.GLINT)
    );

    /**
     * Produces the themed version of {@code src}. Returns {@code null} when the texture should be
     * left exactly as the resource pack provides it.
     */
    public static Canvas process(Target target, Canvas src, Palette p, Options o) {
        String path = target.path();
        Canvas out = src.copy();
        int scale = src.width % 256 == 0 && src.width >= 256 ? src.width / 256 : 0;
        switch (target.group()) {
            case CONTAINERS -> {
                if (path.endsWith("inventory.png") && !path.contains("creative")) {
                    if (!o.containers() && !o.effectFrames()) return null;
                    Recolor.ramp(out, p, true);
                    if (scale > 0) {
                        // the status effect frames live below y=166 and follow the HUD toggle
                        int split = 166 * scale;
                        if (!o.effectFrames()) restoreRows(out, src, split, out.height);
                        if (!o.containers()) restoreRows(out, src, 0, split);
                    }
                    return out;
                }
                if (!o.containers()) return null;
                Recolor.ramp(out, p, true);
                return out;
            }
            case WIDGETS -> {
                if (!o.widgets()) return null;
                if (path.contains("title/")) {
                    logo(out, p);
                } else {
                    Recolor.ramp(out, p, true);
                }
                return out;
            }
            case BACKGROUND -> {
                if (!o.backgrounds()) return null;
                return Patterns.background(src.width, src.height, p, path.contains("light"));
            }
            case LIST_BACKGROUND -> {
                if (!o.backgrounds()) return null;
                return new Canvas(src.width, src.height);
            }
            case HUD_ICONS -> {
                if (scale == 0) return null;
                boolean any = o.hearts() || o.food() || o.armor() || o.xp() || o.customCrosshair();
                if (!any) return null;
                out.scale = scale;
                if (o.hearts()) {
                    HudSprites.paintHearts(out, p);
                    HudSprites.paintAir(out, p);
                }
                if (o.food() && o.foodStyle() != FoodStyle.VANILLA) HudSprites.paintFood(out, p, o.foodStyle());
                if (o.armor()) HudSprites.paintArmor(out, p);
                if (o.xp()) {
                    HudSprites.paintXp(out, p);
                    HudSprites.paintJump(out, p);
                }
                // our own overlay draws the crosshair, so the inverted vanilla one is cleared
                if (o.customCrosshair()) out.clear(0, 0, 15, 15);
                return out;
            }
            case GLINT -> {
                if (!o.glint()) return null;
                Recolor.glint(out, p);
                return out;
            }
            default -> {
                return null;
            }
        }
    }

    private static Canvas widgets(Canvas src, Canvas out, Palette p, Options o, int scale) {
        if (!o.widgets() && !o.hotbar()) return null;
        if (scale == 0) {
            Recolor.ramp(out, p, true);
            return out;
        }
        int s = scale;
        // hover buttons use the accent ramp so they glow in the accent colour
        Recolor.ramp(out, p, true, 0, 0, out.width, 86 * s, false);
        Recolor.ramp(out, p, true, 0, 86 * s, 200 * s, 20 * s, true);
        Recolor.ramp(out, p, true, 0, 106 * s, out.width, out.height - 106 * s, false);
        out.scale = s;
        for (int y : new int[]{46, 66, 86}) Recolor.roundCorners(out, 0, y, 200, 20);
        Recolor.roundCorners(out, 0, 0, 182, 22);
        HudSprites.paintSelection(out, p);
        if (!o.hotbar()) restoreRows(out, src, 0, 46 * s);
        if (!o.widgets()) restoreRows(out, src, 46 * s, out.height);
        return out;
    }

    /** The title logo gets a glossier ramp than flat panels. */
    private static void logo(Canvas c, Palette p) {
        int[] ramp = {p.ink, p.deep, p.shadow, Colors.lerp(p.slot, p.heart, 0.35f), p.base, p.light};
        for (int i = 0; i < c.px.length; i++) {
            int src = c.px[i];
            int a = src >>> 24;
            if (a == 0) continue;
            float l = Colors.luma(src);
            c.px[i] = Colors.withAlpha(Recolor.sample(ramp, Math.min(1f, l * 1.08f)), a);
        }
    }

    private static void restoreRows(Canvas out, Canvas src, int fromRow, int toRow) {
        for (int y = Math.max(0, fromRow); y < Math.min(out.height, toRow); y++)
            System.arraycopy(src.px, y * src.width, out.px, y * out.width, out.width);
    }
}
